package com.library.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.library.demo.dao.BorrowingRepository;
import com.library.demo.dao.LibraryItemRepository;
import com.library.demo.dao.TagRepository;
import com.library.demo.entity.Book;
import com.library.demo.entity.LibraryItem;
import com.library.demo.entity.Magazine;
import com.library.demo.entity.Newspaper;
import com.library.demo.entity.Tag;

@Service
public class LibraryItemServiceImpl implements LibraryItemService {

    private final LibraryItemRepository libraryItemRepository;
    private final BorrowingRepository borrowingRepository;
    private final TagRepository tagRepository;

    public LibraryItemServiceImpl(
            LibraryItemRepository libraryItemRepository,
            BorrowingRepository borrowingRepository,
            TagRepository tagRepository) {

        this.libraryItemRepository = libraryItemRepository;
        this.borrowingRepository = borrowingRepository;
        this.tagRepository = tagRepository;
    }

    @Override
    public List<LibraryItem> findAll() {
        return libraryItemRepository.findAll();
    }

    @Override
    public LibraryItem findById(int id) {
        return libraryItemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Library item not found: " + id));
    }

    @Override
    public LibraryItem save(LibraryItem item) {
        if (item instanceof Newspaper newspaper
                && newspaper.getPublicationDate() != null
                && newspaper.getYearOfPublication() != null
                && newspaper.getYearOfPublication().intValue()
                        != newspaper.getPublicationDate()
                                .toLocalDate().getYear()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Year must match the publication date");
        }

        return libraryItemRepository.save(item);
    }

    @Override
    @Transactional
    public LibraryItem saveWithTags(LibraryItem item, String tagNames) {
        return saveWithTagSelection(item, List.of(), tagNames);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Tag> findAllTags() {
        return tagRepository.findAll(Sort.by("name"));
    }

    @Override
    @Transactional
    public LibraryItem saveWithTagSelection(
            LibraryItem item,
            List<Integer> tagIds,
            String newTagNames) {

        List<String> names = parseTagNames(newTagNames);

        LinkedHashSet<Integer> uniqueIds = new LinkedHashSet<>(
                tagIds == null ? List.of() : tagIds);

        List<Tag> selectedTags = tagRepository.findAllById(uniqueIds);

        if (selectedTags.size() != uniqueIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "One of the selected tags does not exist");
        }

        LibraryItem saved = save(item);

        // Replace this item's previous tags with the submitted selection.
        saved.getTags().clear();
        saved.getTags().addAll(selectedTags);

        for (String name : names) {
            Tag tag = tagRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> tagRepository.save(new Tag(name)));

            saved.getTags().add(tag);
        }

        return saved;
    }

    @Override
    public void deleteById(int id) {
        LibraryItem item = findById(id);

        if (borrowingRepository.existsByItem_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete an item with borrowing history");
        }

        libraryItemRepository.delete(item);
    }

    @Override
    public List<LibraryItem> findAvailable() {
        return libraryItemRepository.findByAvailabilityTrue();
    }

    @Override
    public List<LibraryItem> search(
            String title,
            String author,
            Integer year) {

        return libraryItemRepository.search(
                blankToNull(title),
                blankToNull(author),
                year);
    }

    @Override
    public List<LibraryItem> findByType(String type) {
        if (type == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Item type is required");
        }

        Class<? extends LibraryItem> itemClass;

        switch (type.trim().toUpperCase(Locale.ROOT)) {
            case "BOOK":
                itemClass = Book.class;
                break;

            case "MAGAZINE":
                itemClass = Magazine.class;
                break;

            case "NEWSPAPER":
                itemClass = Newspaper.class;
                break;

            default:
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Type must be BOOK, MAGAZINE, or NEWSPAPER");
        }

        return libraryItemRepository.findByItemClass(itemClass);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Integer, List<Tag>> findTagsForItems(List<Integer> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Map.of();
        }

        Map<Integer, List<Tag>> tagsByItem = new HashMap<>();

        for (Integer itemId : itemIds) {
            tagsByItem.put(itemId, new ArrayList<>());
        }

        for (Object[] row :
                libraryItemRepository.findTagsForItemIds(itemIds)) {

            int itemId = ((Number) row[0]).intValue();
            Tag tag = (Tag) row[1];

            tagsByItem.get(itemId).add(tag);
        }

        return tagsByItem;
    }

    @Override
    @Transactional
    public void addTag(int itemId, String tagName) {
        String name = tagName == null ? "" : tagName.trim();

        if (name.isBlank() || name.length() > 80) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tag name must be between 1 and 80 characters");
        }

        LibraryItem item = findById(itemId);

        Tag tag = tagRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> tagRepository.save(new Tag(name)));

        item.getTags().add(tag);
    }

    @Override
    @Transactional
    public void removeTag(int itemId, int tagId) {
        LibraryItem item = libraryItemRepository.findByIdWithTags(itemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Library item not found: " + itemId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tag not found: " + tagId));

        item.getTags().remove(tag);
    }

    @Override
    @Transactional
    public void deleteUnusedTag(int tagId) {
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tag not found: " + tagId));

        if (libraryItemRepository.existsByTags_Id(tagId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Remove the tag from its items before deleting it");
        }

        tagRepository.delete(tag);
    }

    private List<String> parseTagNames(String tagNames) {
        if (tagNames == null || tagNames.isBlank()) {
            return List.of();
        }

        Map<String, String> uniqueNames = new LinkedHashMap<>();

        for (String part : tagNames.split(",")) {
            String name = part.trim();

            if (name.isEmpty()) {
                continue;
            }

            if (name.length() > 80) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Each tag name must be at most 80 characters");
            }

            uniqueNames.putIfAbsent(
                    name.toLowerCase(Locale.ROOT),
                    name);
        }

        return new ArrayList<>(uniqueNames.values());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}