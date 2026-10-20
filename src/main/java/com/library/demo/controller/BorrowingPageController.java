package com.library.demo.controller;

import com.library.demo.entity.Borrowing;
import com.library.demo.service.BorrowingService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.library.demo.service.MemberService;
import com.library.demo.service.LibraryItemService;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;


@Controller
public class BorrowingPageController {

    private final BorrowingService borrowingService;
    private final MemberService memberService;
    private final LibraryItemService libraryItemService;

    public BorrowingPageController(
            BorrowingService borrowingService,
            MemberService memberService,
            LibraryItemService libraryItemService) {

        this.borrowingService = borrowingService;
        this.memberService = memberService;
        this.libraryItemService = libraryItemService;
    }

    @GetMapping("/borrowings")
    public String listBorrowings(Model model) {
        model.addAttribute("borrowings", borrowingService.findAll());
        return "borrowings/list";
    }

    @GetMapping("/borrowings/new")
    public String showBorrowingForm(Model model) {
        model.addAttribute("members", memberService.findAll());
        model.addAttribute("availableItems", libraryItemService.findAvailable());
        return "borrowings/form";
    }

    @PostMapping("/borrowings")
    public String borrowItem(
            @RequestParam("memberId") int memberId,
            @RequestParam("itemId") int itemId,
            @RequestParam("durationDays") int durationDays) {

        borrowingService.borrowItem(memberId, itemId, durationDays);
        return "redirect:/borrowings";
    }

    @PostMapping("/borrowings/{id}/return")
    public String returnItem(@PathVariable int id) {
        borrowingService.returnItem(id);
        return "redirect:/borrowings";
    }

    @PostMapping("/borrowings/{id}/pay-fine")
    public String payFine(@PathVariable int id) {
        borrowingService.payFine(id);
        return "redirect:/borrowings";
    }

    @GetMapping("/members/{memberId}/borrowings")
    public String memberHistory(
            @PathVariable int memberId,
            @RequestParam(defaultValue = "false") boolean activeOnly,
            Model model) {

        List<Borrowing> borrowings;

        if (activeOnly) {
            borrowings = borrowingService.findActiveByMemberId(memberId);
        } else {
            borrowings = borrowingService.findByMemberId(memberId);
        }

        model.addAttribute("memberId", memberId);
        model.addAttribute("borrowings", borrowings);
        model.addAttribute("activeOnly", activeOnly);

        return "members/history";
    }
}