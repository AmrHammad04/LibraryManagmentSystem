package com.library.demo.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.library.demo.entity.Borrowing;

public interface BorrowingRepository extends JpaRepository<Borrowing, Integer> {

    List<Borrowing> findByMember_Id(int memberId);
    List<Borrowing> findByMember_IdAndReturnDateIsNull(int memberId);
    List<Borrowing> findByReturnDateIsNull();
    boolean existsByItem_IdAndReturnDateIsNull(int itemId);
    boolean existsByMember_Id(int memberId);
    boolean existsByItem_Id(int itemId);
}