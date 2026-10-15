package com.library.demo.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.library.demo.entity.LibraryItem;
import com.library.demo.entity.Member;
import com.library.demo.service.ReportService;

@RestController
@RequestMapping("/api/reports")
public class ReportRestController {

    private final ReportService reportService;

    public ReportRestController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/most-borrowed-item")
    public LibraryItem findMostBorrowedItem() {
        return reportService.findMostBorrowedItem();
    }

    @GetMapping("/popular-items")
    public List<LibraryItem> findPopularItems() {
        return reportService.findPopularItems();
    }

    @GetMapping("/borrowings-by-type/{type}")
    public long countBorrowingsByType(
            @PathVariable("type") String type) {

        return reportService.countBorrowingsByType(type);
    }

    @GetMapping("/top-borrowers")
    public List<Member> findTopBorrowers() {
        return reportService.findTopBorrowers();
    }

    @GetMapping("/total-fines-collected")
    public BigDecimal calculateTotalFinesCollected() {
        return reportService.calculateTotalFinesCollected();
    }
}