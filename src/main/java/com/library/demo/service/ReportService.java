package com.library.demo.service;

import java.math.BigDecimal;
import java.util.List;

import com.library.demo.entity.LibraryItem;
import com.library.demo.entity.Member;

public interface ReportService {

    LibraryItem findMostBorrowedItem();
    List<LibraryItem> findPopularItems();
    long countBorrowingsByType(String type);
    List<Member> findTopBorrowers();
    BigDecimal calculateTotalFinesCollected();
}