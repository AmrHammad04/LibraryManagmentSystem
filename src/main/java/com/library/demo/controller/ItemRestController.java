package com.library.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.library.demo.entity.Book;
import com.library.demo.entity.LibraryItem;
import com.library.demo.entity.Magazine;
import com.library.demo.entity.Newspaper;
import com.library.demo.service.LibraryItemService;

@RestController
@RequestMapping("/api")
public class ItemRestController {

    private final LibraryItemService libraryItemService;

    public ItemRestController(LibraryItemService libraryItemService) {
        this.libraryItemService = libraryItemService;
    }

    @GetMapping("/items")
    public List<LibraryItem> findAll() {
        return libraryItemService.findAll();
    }

    @GetMapping("/items/{id}")
    public LibraryItem findById(@PathVariable("id") int id) {
        return libraryItemService.findById(id);
    }

    @GetMapping("/items/available")
    public List<LibraryItem> findAvailable() {
        return libraryItemService.findAvailable();
    }

    @GetMapping("/items/search")
    public List<LibraryItem> search(
            @RequestParam(name = "title", required = false) String title,
            @RequestParam(name = "author", required = false) String author,
            @RequestParam(name = "year", required = false) Integer year) {

        return libraryItemService.search(title, author, year);
    }

    @GetMapping("/items/type/{type}")
    public List<LibraryItem> findByType(@PathVariable("type") String type) {

        return libraryItemService.findByType(type);
    }

    @PostMapping("/items/books")
    @ResponseStatus(HttpStatus.CREATED)
    public LibraryItem addBook(@RequestBody Book book) {
        book.setId(0);
        book.setAvailability(true);

        return libraryItemService.save(book);
    }

    @PostMapping("/items/magazines")
    @ResponseStatus(HttpStatus.CREATED)
    public LibraryItem addMagazine(@RequestBody Magazine magazine) {
        magazine.setId(0);
        magazine.setAvailability(true);

        return libraryItemService.save(magazine);
    }

    @PostMapping("/items/newspapers")
    @ResponseStatus(HttpStatus.CREATED)
    public LibraryItem addNewspaper(@RequestBody Newspaper newspaper) {
        newspaper.setId(0);
        newspaper.setAvailability(true);

        return libraryItemService.save(newspaper);
    }

    @PutMapping("/items/books/{id}")
    public LibraryItem updateBook(
            @PathVariable("id") int id,
            @RequestBody Book book) {

        LibraryItem existingItem = libraryItemService.findById(id);

        if (!(existingItem instanceof Book)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Item is not a book");
        }

        book.setId(id);
        book.setAvailability(existingItem.getAvailability());

        return libraryItemService.save(book);
    }

    @PutMapping("/items/magazines/{id}")
    public LibraryItem updateMagazine(
            @PathVariable("id") int id,
            @RequestBody Magazine magazine) {

        LibraryItem existingItem = libraryItemService.findById(id);

        if (!(existingItem instanceof Magazine)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Item is not a magazine");
        }

        magazine.setId(id);
        magazine.setAvailability(existingItem.getAvailability());

        return libraryItemService.save(magazine);
    }

    @PutMapping("/items/newspapers/{id}")
    public LibraryItem updateNewspaper(
            @PathVariable("id") int id,
            @RequestBody Newspaper newspaper) {

        LibraryItem existingItem = libraryItemService.findById(id);

        if (!(existingItem instanceof Newspaper)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Item is not a newspaper");
        }

        newspaper.setId(id);
        newspaper.setAvailability(existingItem.getAvailability());

        return libraryItemService.save(newspaper);
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable("id") int id) {
        libraryItemService.deleteById(id);
    }
}