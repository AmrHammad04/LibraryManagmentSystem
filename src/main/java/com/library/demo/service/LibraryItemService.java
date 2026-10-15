package com.library.demo.service;

import java.util.List;

import com.library.demo.entity.LibraryItem;

public interface LibraryItemService {

    List<LibraryItem> findAll();
    LibraryItem findById(int id);
    LibraryItem save(LibraryItem item);
    void deleteById(int id);
    List<LibraryItem> findAvailable();
    List<LibraryItem> search(String title, String author, Integer year);
    List<LibraryItem> findByType(String type);
}