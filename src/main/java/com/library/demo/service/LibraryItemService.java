package com.library.demo.service;

import java.util.List;
import java.util.Map;

import com.library.demo.entity.LibraryItem;
import com.library.demo.entity.Tag;

public interface LibraryItemService {

    List<LibraryItem> findAll();
    LibraryItem findById(int id);
    LibraryItem save(LibraryItem item);

    LibraryItem saveWithTags(LibraryItem item, String tagNames);
    LibraryItem saveWithTagSelection(
            LibraryItem item,
            List<Integer> tagIds,
            String newTagNames);

    List<Tag> findAllTags();
    Map<Integer, List<Tag>> findTagsForItems(List<Integer> itemIds);

    void deleteById(int id);
    List<LibraryItem> findAvailable();
    List<LibraryItem> search(String title, String author, Integer year);
    List<LibraryItem> findByType(String type);

    void addTag(int itemId, String tagName);
    void removeTag(int itemId, int tagId);
    void deleteUnusedTag(int tagId);
}