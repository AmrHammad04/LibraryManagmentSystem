package com.library.demo.controller;

import java.util.List;

import com.library.demo.entity.LibraryItem;
import com.library.demo.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportPageController {

    private final ReportService reportService;

    public ReportPageController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/reports")
    public String showReports(Model model) {
        List<LibraryItem> popularItems = reportService.findPopularItems();

        String mostBorrowedTitle = "No borrowing records yet";
        if (!popularItems.isEmpty()) {
            mostBorrowedTitle = popularItems.get(0).getTitle();
        }

        model.addAttribute("mostBorrowedTitle", mostBorrowedTitle);
        model.addAttribute("popularItems", popularItems);
        model.addAttribute("topBorrowers", reportService.findTopBorrowers());
        model.addAttribute("bookCount", reportService.countBorrowingsByType("BOOK"));
        model.addAttribute("magazineCount", reportService.countBorrowingsByType("MAGAZINE"));
        model.addAttribute("newspaperCount", reportService.countBorrowingsByType("NEWSPAPER"));
        model.addAttribute("totalFines", reportService.calculateTotalFinesCollected());

        return "reports/index";
    }
}