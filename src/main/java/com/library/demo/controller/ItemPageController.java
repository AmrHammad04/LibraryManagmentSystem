package com.library.demo.controller;

import com.library.demo.service.LibraryItemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.library.demo.entity.Book;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import com.library.demo.entity.LibraryItem;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ItemPageController {

    private final LibraryItemService libraryItemService;

    public ItemPageController(LibraryItemService libraryItemService) {
        this.libraryItemService = libraryItemService;
    }

    @GetMapping("/items")
    public String listItems(
        Authentication authentication,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String author,
        @RequestParam(required = false) Integer year,
        Model model) {

        model.addAttribute("items", libraryItemService.search(title, author, year));
        model.addAttribute("title", title);
        model.addAttribute("author", author);
        model.addAttribute("year", year);
        boolean admin = false;
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                admin = true;
                break;
            }
        }

        model.addAttribute("admin", admin);
        return "items/list";
    }

    @GetMapping("/items/books/new")
    public String showBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "items/book-form";
    }

    @PostMapping("/items/books")
    public String saveBook(@ModelAttribute("book") Book book) {
        book.setId(0);
        book.setAvailability(true);
        libraryItemService.save(book);

        return "redirect:/items";
    }

    @GetMapping("/items/books/{id}/edit")
    public String showEditBookForm(@PathVariable int id, Model model) {
        LibraryItem item = libraryItemService.findById(id);

        if (!(item instanceof Book)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item is not a book");
        }

        model.addAttribute("book", item);
        return "items/edit-book";
    }

    @PostMapping("/items/books/{id}")
    public String updateBook(@PathVariable int id, @ModelAttribute Book formBook) {
        LibraryItem item = libraryItemService.findById(id);

        if (!(item instanceof Book)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item is not a book");
        }

        Book book = (Book) item;
        book.setTitle(formBook.getTitle());
        book.setAuthor(formBook.getAuthor());
        book.setYearOfPublication(formBook.getYearOfPublication());
        book.setIsbn(formBook.getIsbn());
        book.setNumberOfPages(formBook.getNumberOfPages());

        libraryItemService.save(book);
        return "redirect:/items";
    }

    @PostMapping("/items/{id}/delete")
    public String deleteItem(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            libraryItemService.deleteById(id);
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode().value() != 409) {
                throw exception;
            }

            redirectAttributes.addFlashAttribute("error", exception.getReason());
        }

        return "redirect:/items";
    }


}