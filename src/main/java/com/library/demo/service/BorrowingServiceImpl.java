package com.library.demo.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.library.demo.dao.BorrowingRepository;
import com.library.demo.dao.LibraryItemRepository;
import com.library.demo.dao.MemberRepository;
import com.library.demo.entity.Borrowing;
import com.library.demo.entity.LibraryItem;
import com.library.demo.entity.Member;

@Service
@Transactional(readOnly = true)
public class BorrowingServiceImpl implements BorrowingService {

    private static final BigDecimal DAILY_FINE = new BigDecimal("1.00");

    private final BorrowingRepository borrowingRepository;
    private final MemberRepository memberRepository;
    private final LibraryItemRepository libraryItemRepository;

    public BorrowingServiceImpl(
            BorrowingRepository borrowingRepository,
            MemberRepository memberRepository,
            LibraryItemRepository libraryItemRepository) {

        this.borrowingRepository = borrowingRepository;
        this.memberRepository = memberRepository;
        this.libraryItemRepository = libraryItemRepository;
    }

    @Override
    public List<Borrowing> findAll() {
        return borrowingRepository.findAll();
    }

    @Override
    public Borrowing findById(int id) {
        Borrowing borrowing = borrowingRepository.findById(id).orElse(null);

        if (borrowing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Borrowing not found: " + id);
        }

        return borrowing;
    }

    @Override
    public List<Borrowing> findByMemberId(int memberId) {
        requireMember(memberId);

        List<Borrowing> borrowings = borrowingRepository.findAll();
        List<Borrowing> results = new ArrayList<>();

        for (Borrowing borrowing : borrowings) {
            if (borrowing.getMember().getId() == memberId) {
                results.add(borrowing);
            }
        }

        return results;
    }

    @Override
    public List<Borrowing> findActiveByMemberId(int memberId) {
        List<Borrowing> borrowings = findByMemberId(memberId);
        List<Borrowing> results = new ArrayList<>();

        for (Borrowing borrowing : borrowings) {
            if (borrowing.getReturnDate() == null) {
                results.add(borrowing);
            }
        }

        return results;
    }

    @Override
    @Transactional
    public Borrowing borrowItem(
            int memberId, int itemId, int durationDays) {

        if (durationDays <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Borrowing duration must be greater than zero");
        }

        Member member = requireMember(memberId);

        LibraryItem item = libraryItemRepository.findById(itemId).orElse(null);

        if (item == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Library item not found: " + itemId);
        }

        if (item.getAvailability() == null || item.getAvailability() == false) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Item is unavailable");
        }

        List<Borrowing> borrowings = borrowingRepository.findAll();

        for (Borrowing existingBorrowing : borrowings) {
            if (existingBorrowing.getItem().getId() == itemId && existingBorrowing.getReturnDate() == null) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,"Item is already borrowed");
            }
        }

        LocalDate today = LocalDate.now();

        Date borrowingDate = Date.valueOf(today);
        Date dueDate = Date.valueOf(today.plusDays(durationDays));

        Borrowing borrowing = new Borrowing(
                member,
                item,
                borrowingDate,
                dueDate);

        item.setAvailability(false);
        libraryItemRepository.save(item);

        return borrowingRepository.save(borrowing);
    }

    @Override
    @Transactional
    public Borrowing returnItem(int borrowingId) {
        Borrowing borrowing = findById(borrowingId);

        if (borrowing.getReturnDate() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Item has already been returned");
        }

        LocalDate today = LocalDate.now();

        long overdueDays = ChronoUnit.DAYS.between(borrowing.getDueDate().toLocalDate(),today);

        if (overdueDays < 0) {
            overdueDays = 0;
        }

        BigDecimal fine = DAILY_FINE.multiply(
                BigDecimal.valueOf(overdueDays));

        borrowing.setReturnDate(Date.valueOf(today));
        borrowing.setFineAmount(fine);

        LibraryItem item = borrowing.getItem();
        item.setAvailability(true);

        libraryItemRepository.save(item);

        return borrowingRepository.save(borrowing);
    }

    private Member requireMember(int memberId) {
        Member member = memberRepository.findById(memberId).orElse(null);

        if (member == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Member not found: " + memberId);
        }

        return member;
    }

    @Override
    @Transactional
    public Borrowing payFine(int borrowingId) {
        Borrowing borrowing = findById(borrowingId);

        if (borrowing.getReturnDate() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Return the item before paying the fine");
        }

        if (borrowing.getFineAmount() == null|| borrowing.getFineAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"There is no fine to pay");
        }

        if (borrowing.isFinePaid()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Fine has already been paid");
        }

        borrowing.setFinePaid(true);

        return borrowingRepository.save(borrowing);
    }
}