package com.library.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.library.demo.dao.BorrowingRepository;
import com.library.demo.dao.LibraryItemRepository;
import com.library.demo.entity.Book;
import com.library.demo.entity.LibraryItem;
import com.library.demo.entity.Magazine;
import com.library.demo.entity.Newspaper;

@Service
public class LibraryItemServiceImpl implements LibraryItemService {

    private final LibraryItemRepository libraryItemRepository;
    private final BorrowingRepository borrowingRepository;

    public LibraryItemServiceImpl(
            LibraryItemRepository libraryItemRepository,
            BorrowingRepository borrowingRepository) {

        this.libraryItemRepository = libraryItemRepository;
        this.borrowingRepository = borrowingRepository;
    }

    @Override
    public List<LibraryItem> findAll() {
        return libraryItemRepository.findAll();
    }

    @Override
    public LibraryItem findById(int id) {
        LibraryItem item = libraryItemRepository.findById(id).orElse(null);

        if (item == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Library item not found: " + id);
        }

        return item;
    }

    @Override
    public LibraryItem save(LibraryItem item) {
        if (item instanceof Newspaper newspaper
                && newspaper.getPublicationDate() != null
                && newspaper.getYearOfPublication() != null
                && newspaper.getYearOfPublication().intValue()
                        != newspaper.getPublicationDate().toLocalDate().getYear()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Year must match the publication date");
        }

        return libraryItemRepository.save(item);
    }

    @Override
    public void deleteById(int id) {
        LibraryItem item = findById(id);

        if (borrowingRepository.existsByItem_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Cannot delete an item with borrowing history");
        }

        libraryItemRepository.delete(item);
    }

    @Override
    public List<LibraryItem> findAvailable() {
        List<LibraryItem> availableItems = new ArrayList<>();

        for (LibraryItem item : findAll()) {
            if (Boolean.TRUE.equals(item.getAvailability())) {
                availableItems.add(item);
            }
        }

        return availableItems;
    }

    @Override
    public List<LibraryItem> search(String title, String author, Integer year) {
        List<LibraryItem> results = new ArrayList<>();

        for (LibraryItem item : findAll()) {
            if (title != null && !title.isBlank()
                    && !containsIgnoreCase(item.getTitle(), title)) {
                continue;
            }

            if (author != null && !author.isBlank()
                    && !containsIgnoreCase(item.getAuthor(), author)) {
                continue;
            }

            if (year != null && !year.equals(item.getYearOfPublication())) {
                continue;
            }

            results.add(item);
        }

        return results;
    }

    @Override
    public List<LibraryItem> findByType(String type) {
        if (type == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Item type is required");
        }

        Class<? extends LibraryItem> itemClass;

        switch (type.trim().toUpperCase(java.util.Locale.ROOT)) {
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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Type must be BOOK, MAGAZINE, or NEWSPAPER");
        }

        List<LibraryItem> results = new ArrayList<>();

        for (LibraryItem item : findAll()) {
            if (itemClass.isInstance(item)) {
                results.add(item);
            }
        }

        return results;    
    }

    private boolean containsIgnoreCase(String value, String searchText) {
    if (value == null || searchText == null) {
        return false;
        }

    String lowerValue = value.toLowerCase(java.util.Locale.ROOT);
    String lowerSearchText = searchText.trim()
            .toLowerCase(java.util.Locale.ROOT);

    return lowerValue.contains(lowerSearchText);
    }
}