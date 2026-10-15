package com.library.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.library.demo.entity.Borrowing;
import com.library.demo.service.BorrowingService;

@RestController
@RequestMapping("/api")
public class BorrowingRestController {

    private final BorrowingService borrowingService;

    public BorrowingRestController(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    @GetMapping("/borrowings")
    public List<Borrowing> findAll() {
        return borrowingService.findAll();
    }

    @GetMapping("/borrowings/{id}")
    public Borrowing findById(@PathVariable("id") int id) {
        return borrowingService.findById(id);
    }

    @GetMapping("/members/{memberId}/borrowings")
    public List<Borrowing> findByMemberId(@PathVariable("memberId") int memberId) {
        return borrowingService.findByMemberId(memberId);
    }

    @GetMapping("/members/{memberId}/borrowings/active")
    public List<Borrowing> findActiveByMemberId(@PathVariable("memberId") int memberId) {
        return borrowingService.findActiveByMemberId(memberId);
    }

    @PostMapping("/borrowings")
    @ResponseStatus(HttpStatus.CREATED)
    public Borrowing borrowItem(
            @RequestParam("memberId") int memberId,
            @RequestParam("itemId") int itemId,
            @RequestParam("durationDays") int durationDays) {

                return borrowingService.borrowItem(memberId, itemId, durationDays);
    }

    @PutMapping("/borrowings/{id}/returned")
    public Borrowing returnItem(@PathVariable("id") int id) {
        return borrowingService.returnItem(id);
    }
    @PutMapping("/borrowings/{id}/pay-fine")
    public Borrowing payFine(@PathVariable("id") int id) {
        return borrowingService.payFine(id);
    }
}