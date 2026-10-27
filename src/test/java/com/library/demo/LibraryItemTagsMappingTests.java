package com.library.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.library.demo.dao.LibraryItemRepository;
import com.library.demo.dao.TagRepository;
import com.library.demo.entity.Book;
import com.library.demo.entity.Tag;
import com.library.demo.service.LibraryItemService;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Transactional
class LibraryItemTagsMappingTests {

    @Autowired
    private LibraryItemRepository itemRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private LibraryItemService itemService;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void twoItemsCanShareTagAndOneLinkCanBeRemoved() {
        Book first = itemRepository.saveAndFlush(new Book(
                "First Test Book", "Test Author", 2020,
                true, "9780132350884", 100));

        Book second = itemRepository.saveAndFlush(new Book(
                "Second Test Book", "Test Author", 2021,
                true, "9780134685991", 150));

        String tagName = "Test-" + UUID.randomUUID();

        itemService.addTag(first.getId(), tagName);
        itemService.addTag(second.getId(), tagName);
        entityManager.flush();
        entityManager.clear();

        Tag tag = tagRepository.findByNameIgnoreCase(tagName).orElseThrow();
        int tagId = tag.getId();

        assertEquals(2, tagRepository.findByIdWithItems(tagId)
                .orElseThrow().getItems().size());

        entityManager.clear();

        assertTrue(itemRepository.findByIdWithTags(first.getId())
                .orElseThrow().getTags().stream()
                .anyMatch(savedTag -> savedTag.getId() == tagId));

        entityManager.clear();

        itemService.removeTag(first.getId(), tagId);
        entityManager.flush();
        entityManager.clear();

        assertTrue(itemRepository.findByIdWithTags(first.getId())
                .orElseThrow().getTags().isEmpty());

        entityManager.clear();

        assertEquals(1, tagRepository.findByIdWithItems(tagId)
                .orElseThrow().getItems().size());
        assertTrue(tagRepository.existsById(tagId));
    }
}