package com.library.demo.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library.demo.entity.LibraryItem;

import jakarta.persistence.LockModeType;

public interface LibraryItemRepository extends JpaRepository<LibraryItem, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT item FROM LibraryItem item WHERE item.id = :id")
    Optional<LibraryItem> findByIdForUpdate(@Param("id") int id);

    List<LibraryItem> findByAvailabilityTrue();

    @Query("""
            SELECT item
            FROM LibraryItem item
            WHERE (:title IS NULL
                   OR LOWER(item.title) LIKE CONCAT('%', LOWER(:title), '%'))
              AND (:author IS NULL
                   OR LOWER(item.author) LIKE CONCAT('%', LOWER(:author), '%'))
              AND (:year IS NULL
                   OR item.yearOfPublication = :year)
            """)
    List<LibraryItem> search(
            @Param("title") String title,
            @Param("author") String author,
            @Param("year") Integer year);

    @Query("""
            SELECT item
            FROM LibraryItem item
            WHERE TYPE(item) = :itemClass
            """)
    List<LibraryItem> findByItemClass(
            @Param("itemClass") Class<? extends LibraryItem> itemClass);

    @Query("""
            SELECT DISTINCT item
            FROM LibraryItem item
            LEFT JOIN FETCH item.tags
            WHERE item.id = :id
            """)
    Optional<LibraryItem> findByIdWithTags(@Param("id") int id);

    @Query("""
            SELECT item.id, tag
            FROM LibraryItem item
            JOIN item.tags tag
            WHERE item.id IN :itemIds
            ORDER BY item.id, tag.name
            """)
    List<Object[]> findTagsForItemIds(
            @Param("itemIds") List<Integer> itemIds);

    Page<LibraryItem> findDistinctByTags_Id(int tagId, Pageable pageable);

    boolean existsByTags_Id(int tagId);

    @Query("""
            SELECT item
            FROM LibraryItem item
            WHERE (:title IS NULL
                   OR LOWER(item.title) LIKE CONCAT('%', LOWER(:title), '%'))
              AND (:author IS NULL
                   OR LOWER(item.author) LIKE CONCAT('%', LOWER(:author), '%'))
              AND (:year IS NULL
                   OR item.yearOfPublication = :year)
              AND (:availableOnly = false OR item.availability = true)
              AND TYPE(item) IN :allowedClasses
            """)
    Page<LibraryItem> searchPage(
            @Param("title") String title,
            @Param("author") String author,
            @Param("year") Integer year,
            @Param("availableOnly") boolean availableOnly,
            @Param("allowedClasses")
            List<Class<? extends LibraryItem>> allowedClasses,
            Pageable pageable);
}