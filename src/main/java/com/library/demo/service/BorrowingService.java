package com.library.demo.service;

import java.time.LocalDate;
import java.util.List;

import com.library.demo.entity.Borrowing;

public interface BorrowingService {

    List<Borrowing> findAll();
    Borrowing findById(int id);
    List<Borrowing> findByMemberId(int memberId);
    List<Borrowing> findActiveByMemberId(int memberId);
    Borrowing borrowItem(int memberId, int itemId, int durationDays);
    Borrowing returnItem(int borrowingId);
    Borrowing payFine(int borrowingId);
    Borrowing updateDueDate(int borrowingId, LocalDate dueDate);
    void cancelBorrowing(int borrowingId);
}