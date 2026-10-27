package com.library.demo.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library.demo.entity.Tag;

public interface TagRepository extends JpaRepository<Tag, Integer> {

    Optional<Tag> findByNameIgnoreCase(String name);

    @Query("""
            SELECT DISTINCT tag
            FROM Tag tag
            LEFT JOIN FETCH tag.items
            WHERE tag.id = :id
            """)
    Optional<Tag> findByIdWithItems(@Param("id") int id);
}