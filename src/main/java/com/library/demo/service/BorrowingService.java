package com.library.demo.service;

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
}