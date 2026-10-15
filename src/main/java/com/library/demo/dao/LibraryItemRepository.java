package com.library.demo.dao;

import com.library.demo.entity.LibraryItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibraryItemRepository extends JpaRepository<LibraryItem, Integer> {
        
}