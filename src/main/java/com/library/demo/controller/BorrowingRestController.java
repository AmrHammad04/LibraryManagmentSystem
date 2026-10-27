package com.library.demo.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.library.demo.entity.Borrowing;
import com.library.demo.service.BorrowingService;

@RestController
@RequestMapping("/api")
public class BorrowingRestController {

    private static final int PAGE_SIZE = 20;

    private final BorrowingService borrowingService;

    public BorrowingRestController(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }

    @GetMapping("/borrowings")
    public List<Borrowing> findAll(
            @RequestParam(defaultValue = "0") int page) {

        return borrowingService
                .findPageWithDetails(pageRequest(page))
                .getContent();
    }

    @GetMapping("/borrowings/{id}")
    public Borrowing findById(@PathVariable int id) {
        return borrowingService.findById(id);
    }

    @GetMapping("/members/{memberId}/borrowings")
    public List<Borrowing> findByMemberId(
            @PathVariable int memberId,
            @RequestParam(defaultValue = "0") int page) {

        return borrowingService
                .findMemberHistory(memberId, false, pageRequest(page))
                .getContent();
    }

    @GetMapping("/members/{memberId}/borrowings/active")
    public List<Borrowing> findActiveByMemberId(
            @PathVariable int memberId,
            @RequestParam(defaultValue = "0") int page) {

        return borrowingService
                .findMemberHistory(memberId, true, pageRequest(page))
                .getContent();
    }

    @PostMapping("/borrowings")
    @ResponseStatus(HttpStatus.CREATED)
    public Borrowing borrowItem(
            @RequestParam int memberId,
            @RequestParam int itemId,
            @RequestParam int durationDays) {

        return borrowingService.borrowItem(memberId, itemId, durationDays);
    }

    @PutMapping("/borrowings/{id}/returned")
    public Borrowing returnItem(@PathVariable int id) {
        return borrowingService.returnItem(id);
    }

    @PutMapping("/borrowings/{id}/pay-fine")
    public Borrowing payFine(@PathVariable int id) {
        return borrowingService.payFine(id);
    }

    private Pageable pageRequest(int page) {
        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page number cannot be negative");
        }

        return PageRequest.of(
                page,
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "id"));
    }
}