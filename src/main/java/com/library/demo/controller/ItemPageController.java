package com.library.demo.controller;

import java.beans.PropertyEditorSupport;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.demo.entity.Book;
import com.library.demo.entity.LibraryItem;
import com.library.demo.entity.Magazine;
import com.library.demo.entity.Newspaper;
import com.library.demo.entity.Tag;
import com.library.demo.service.LibraryItemService;

import jakarta.validation.Valid;

@Controller
public class ItemPageController {

    private final LibraryItemService libraryItemService;
    private final List<String> itemTypes;
    private final List<String> sortOptions;

    public ItemPageController(
            LibraryItemService libraryItemService,
            @Value("${library.item-types}") String itemTypes,
            @Value("${library.sort-options}") String sortOptions) {

        this.libraryItemService = libraryItemService;
        this.itemTypes = List.of(itemTypes.split(","));
        this.sortOptions = List.of(sortOptions.split(","));
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/items";
    }

    @GetMapping("/items")
    public String listItems(
            Authentication authentication,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "false") boolean availableOnly,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) List<String> types,
            Model model) {

        List<LibraryItem> items =
                libraryItemService.search(title, author, year);

        model.addAttribute("title", title);
        model.addAttribute("author", author);
        model.addAttribute("year", year);

        boolean admin = false;
        boolean librarian = false;

        List<Integer> bookIds = new ArrayList<>();
        List<Integer> magazineIds = new ArrayList<>();
        List<Integer> newspaperIds = new ArrayList<>();

        for (LibraryItem item : items) {
            if (item instanceof Book) {
                bookIds.add(item.getId());
            } else if (item instanceof Magazine) {
                magazineIds.add(item.getId());
            } else if (item instanceof Newspaper) {
                newspaperIds.add(item.getId());
            }
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                admin = true;
            }

            if ("ROLE_LIBRARIAN".equals(authority.getAuthority())) {
                librarian = true;
            }
        }

        if (availableOnly) {
            List<LibraryItem> availableItems = new ArrayList<>();

            for (LibraryItem item : items) {
                if (Boolean.TRUE.equals(item.getAvailability())) {
                    availableItems.add(item);
                }
            }

            items = availableItems;
        }

        if (type != null && !type.isBlank()) {
            List<LibraryItem> matchingItems = new ArrayList<>();

            for (LibraryItem item : items) {
                if ((type.equals("BOOK") && item instanceof Book)
                        || (type.equals("MAGAZINE")
                                && item instanceof Magazine)
                        || (type.equals("NEWSPAPER")
                                && item instanceof Newspaper)) {

                    matchingItems.add(item);
                }
            }

            items = matchingItems;
        }

        if ("title".equals(sortBy)) {
            items.sort(Comparator.comparing(
                    LibraryItem::getTitle,
                    Comparator.nullsLast(
                            String.CASE_INSENSITIVE_ORDER)));

        } else if ("year".equals(sortBy)) {
            items.sort(Comparator.comparing(
                    LibraryItem::getYearOfPublication,
                    Comparator.nullsLast(
                            Comparator.naturalOrder())));

        } else {
            items.sort(Comparator.comparingInt(LibraryItem::getId));
            sortBy = "id";
        }

        if (types != null && !types.isEmpty()) {
            List<LibraryItem> filteredItems = new ArrayList<>();

            for (LibraryItem item : items) {
                boolean matches =
                        (item instanceof Book
                                && types.contains("BOOK"))
                        || (item instanceof Magazine
                                && types.contains("MAGAZINE"))
                        || (item instanceof Newspaper
                                && types.contains("NEWSPAPER"));

                if (matches) {
                    filteredItems.add(item);
                }
            }

            items = filteredItems;
        }

        List<Integer> visibleItemIds = items.stream()
                .map(LibraryItem::getId)
                .toList();

        model.addAttribute("items", items);
        model.addAttribute(
                "itemTags",
                libraryItemService.findTagsForItems(visibleItemIds));

        model.addAttribute("bookIds", bookIds);
        model.addAttribute("magazineIds", magazineIds);
        model.addAttribute("newspaperIds", newspaperIds);
        model.addAttribute("availableOnly", availableOnly);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("type", type);
        model.addAttribute("admin", admin);
        model.addAttribute("librarian", librarian);
        model.addAttribute("sortOptions", sortOptions);
        model.addAttribute("itemTypes", itemTypes);
        model.addAttribute(
                "selectedTypes",
                types == null ? List.of() : types);
        model.addAttribute("username", authentication.getName());

        String roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replaceFirst("^ROLE_", ""))
                .collect(Collectors.joining(", "));

        model.addAttribute("roles", roles);

        return "items/list";
    }

    @GetMapping("/items/books/new")
    public String showBookForm(Model model) {
        model.addAttribute("book", new Book());
        prepareTagForm(model, List.of(), "");
        return "items/book-form";
    }

    @PostMapping("/items/books")
    public String saveBook(
            @Valid @ModelAttribute("book") Book book,
            BindingResult result,
            @RequestParam(name = "tagIds", required = false)
            List<Integer> tagIds,
            @RequestParam(defaultValue = "") String newTagNames,
            Model model) {

        prepareTagForm(model, tagIds, newTagNames);
        validateTagNames(newTagNames, result);

        if (result.hasErrors()) {
            return "items/book-form";
        }

        book.setId(0);
        book.setAvailability(true);
        libraryItemService.saveWithTagSelection(
                book, tagIds, newTagNames);

        return "redirect:/items";
    }

    @GetMapping("/items/books/{id}/edit")
    public String showEditBookForm(
            @PathVariable int id,
            Model model) {

        LibraryItem item = libraryItemService.findById(id);

        if (!(item instanceof Book)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Item is not a book");
        }

        model.addAttribute("book", item);
        prepareTagForm(model, tagIdsForItem(id), "");

        return "items/edit-book";
    }

    @PostMapping("/items/books/{id}")
    public String updateBook(
            @PathVariable int id,
            @Valid @ModelAttribute("book") Book formBook,
            BindingResult result,
            @RequestParam(name = "tagIds", required = false)
            List<Integer> tagIds,
            @RequestParam(defaultValue = "") String newTagNames,
            Model model) {

        LibraryItem item = libraryItemService.findById(id);

        if (!(item instanceof Book)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Item is not a book");
        }

        prepareTagForm(model, tagIds, newTagNames);
        validateTagNames(newTagNames, result);

        if (result.hasErrors()) {
            formBook.setId(id);
            return "items/edit-book";
        }

        Book book = (Book) item;
        book.setTitle(formBook.getTitle());
        book.setAuthor(formBook.getAuthor());
        book.setYearOfPublication(formBook.getYearOfPublication());
        book.setIsbn(formBook.getIsbn());
        book.setNumberOfPages(formBook.getNumberOfPages());

        libraryItemService.saveWithTagSelection(
                book, tagIds, newTagNames);

        return "redirect:/items";
    }

    @PostMapping("/items/{id}/delete")
    public String deleteItem(
            @PathVariable int id,
            RedirectAttributes redirectAttributes) {

        try {
            libraryItemService.deleteById(id);

        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode().value() != 409) {
                throw exception;
            }

            redirectAttributes.addFlashAttribute(
                    "error",
                    exception.getReason());
        }

        return "redirect:/items";
    }

    @GetMapping("/items/magazines/new")
    public String showMagazineForm(Model model) {
        model.addAttribute("magazine", new Magazine());
        prepareTagForm(model, List.of(), "");
        return "items/magazine-form";
    }

    @PostMapping("/items/magazines")
    public String saveMagazine(
            @Valid @ModelAttribute("magazine") Magazine magazine,
            BindingResult result,
            @RequestParam(name = "tagIds", required = false)
            List<Integer> tagIds,
            @RequestParam(defaultValue = "") String newTagNames,
            Model model) {

        prepareTagForm(model, tagIds, newTagNames);
        validateTagNames(newTagNames, result);

        if (result.hasErrors()) {
            return "items/magazine-form";
        }

        magazine.setId(0);
        magazine.setAvailability(true);
        libraryItemService.saveWithTagSelection(
                magazine, tagIds, newTagNames);

        return "redirect:/items";
    }

    @GetMapping("/items/magazines/{id}/edit")
    public String showEditMagazineForm(
            @PathVariable int id,
            Model model) {

        LibraryItem item = libraryItemService.findById(id);

        if (!(item instanceof Magazine)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Item is not a magazine");
        }

        model.addAttribute("magazine", item);
        prepareTagForm(model, tagIdsForItem(id), "");

        return "items/edit-magazine";
    }

    @PostMapping("/items/magazines/{id}")
    public String updateMagazine(
            @PathVariable int id,
            @Valid @ModelAttribute("magazine") Magazine formMagazine,
            BindingResult result,
            @RequestParam(name = "tagIds", required = false)
            List<Integer> tagIds,
            @RequestParam(defaultValue = "") String newTagNames,
            Model model) {

        LibraryItem item = libraryItemService.findById(id);

        if (!(item instanceof Magazine)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Item is not a magazine");
        }

        prepareTagForm(model, tagIds, newTagNames);
        validateTagNames(newTagNames, result);

        if (result.hasErrors()) {
            formMagazine.setId(id);
            return "items/edit-magazine";
        }

        Magazine magazine = (Magazine) item;
        magazine.setTitle(formMagazine.getTitle());
        magazine.setAuthor(formMagazine.getAuthor());
        magazine.setYearOfPublication(
                formMagazine.getYearOfPublication());
        magazine.setIssueNumber(formMagazine.getIssueNumber());
        magazine.setCategory(formMagazine.getCategory());

        libraryItemService.saveWithTagSelection(
                magazine, tagIds, newTagNames);

        return "redirect:/items";
    }

    @GetMapping("/items/newspapers/new")
    public String showNewspaperForm(Model model) {
        model.addAttribute("newspaper", new Newspaper());
        prepareTagForm(model, List.of(), "");
        return "items/newspaper-form";
    }

    @PostMapping("/items/newspapers")
    public String saveNewspaper(
            @Valid @ModelAttribute("newspaper") Newspaper newspaper,
            BindingResult result,
            @RequestParam(name = "tagIds", required = false)
            List<Integer> tagIds,
            @RequestParam(defaultValue = "") String newTagNames,
            Model model) {

        prepareTagForm(model, tagIds, newTagNames);
        validateTagNames(newTagNames, result);
        validateNewspaperYear(newspaper, result);

        if (result.hasErrors()) {
            return "items/newspaper-form";
        }

        newspaper.setId(0);
        newspaper.setAvailability(true);
        libraryItemService.saveWithTagSelection(
                newspaper, tagIds, newTagNames);

        return "redirect:/items";
    }

    @GetMapping("/items/newspapers/{id}/edit")
    public String showEditNewspaperForm(
            @PathVariable int id,
            Model model) {

        LibraryItem item = libraryItemService.findById(id);

        if (!(item instanceof Newspaper)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Item is not a newspaper");
        }

        model.addAttribute("newspaper", item);
        prepareTagForm(model, tagIdsForItem(id), "");

        return "items/edit-newspaper";
    }

    @PostMapping("/items/newspapers/{id}")
    public String updateNewspaper(
            @PathVariable int id,
            @Valid @ModelAttribute("newspaper") Newspaper formNewspaper,
            BindingResult result,
            @RequestParam(name = "tagIds", required = false)
            List<Integer> tagIds,
            @RequestParam(defaultValue = "") String newTagNames,
            Model model) {

        LibraryItem item = libraryItemService.findById(id);

        if (!(item instanceof Newspaper)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Item is not a newspaper");
        }

        prepareTagForm(model, tagIds, newTagNames);
        validateTagNames(newTagNames, result);
        validateNewspaperYear(formNewspaper, result);

        if (result.hasErrors()) {
            formNewspaper.setId(id);
            return "items/edit-newspaper";
        }

        Newspaper newspaper = (Newspaper) item;
        newspaper.setTitle(formNewspaper.getTitle());
        newspaper.setAuthor(formNewspaper.getAuthor());
        newspaper.setYearOfPublication(
                formNewspaper.getYearOfPublication());
        newspaper.setPublicationDate(
                formNewspaper.getPublicationDate());
        newspaper.setRegion(formNewspaper.getRegion());

        libraryItemService.saveWithTagSelection(
                newspaper, tagIds, newTagNames);

        return "redirect:/items";
    }

    private List<Integer> tagIdsForItem(int itemId) {
        return libraryItemService
                .findTagsForItems(List.of(itemId))
                .getOrDefault(itemId, List.of())
                .stream()
                .map(Tag::getId)
                .toList();
    }

    private void prepareTagForm(
            Model model,
            List<Integer> tagIds,
            String newTagNames) {

        model.addAttribute(
                "availableTags",
                libraryItemService.findAllTags());

        model.addAttribute(
                "selectedTagIds",
                tagIds == null ? List.of() : tagIds);

        model.addAttribute("newTagNames", newTagNames);
    }

    private void validateTagNames(
            String tagNames,
            BindingResult result) {

        for (String part : tagNames.split(",")) {
            if (part.trim().length() > 80) {
                result.reject(
                        "invalidTags",
                        "Each tag name must be at most 80 characters");
                return;
            }
        }
    }

    private void validateNewspaperYear(
            Newspaper newspaper,
            BindingResult result) {

        if (newspaper.getPublicationDate() != null
                && newspaper.getYearOfPublication() != null
                && !newspaper.getYearOfPublication().equals(
                        newspaper.getPublicationDate()
                                .toLocalDate().getYear())) {

            result.rejectValue(
                    "yearOfPublication",
                    "yearMismatch",
                    "Year must match the publication date");
        }
    }

    @InitBinder("newspaper")
    public void bindNewspaperDate(WebDataBinder binder) {
        binder.registerCustomEditor(
                Date.class,
                "publicationDate",
                new PropertyEditorSupport() {
                    @Override
                    public void setAsText(String text) {
                        setValue(text == null || text.isBlank()
                                ? null
                                : Date.valueOf(text));
                    }

                    @Override
                    public String getAsText() {
                        Date date = (Date) getValue();
                        return date == null ? "" : date.toString();
                    }
                });
    }
}