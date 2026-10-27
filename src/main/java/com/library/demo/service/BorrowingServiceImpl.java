package com.library.demo.service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        return borrowingRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Borrowing not found: " + id));
    }

    @Override
    public List<Borrowing> findByMemberId(int memberId) {
        requireMember(memberId);
        return borrowingRepository.findByMember_Id(memberId);
    }

    @Override
    public List<Borrowing> findActiveByMemberId(int memberId) {
        requireMember(memberId);
        return borrowingRepository.findByMember_IdAndReturnDateIsNull(memberId);
    }

    @Override
    @Transactional
    public Borrowing borrowItem(int memberId, int itemId, int durationDays) {
        if (durationDays <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Borrowing duration must be greater than zero");
        }

        Member member = requireMember(memberId);

        LibraryItem item = libraryItemRepository.findByIdForUpdate(itemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Library item not found: " + itemId));

        if (!Boolean.TRUE.equals(item.getAvailability())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Item is unavailable");
        }

        if (borrowingRepository.existsByItem_IdAndReturnDateIsNull(itemId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Item is already borrowed");
        }

        LocalDate today = LocalDate.now();

        Borrowing borrowing = new Borrowing(
                member,
                item,
                Date.valueOf(today),
                Date.valueOf(today.plusDays(durationDays)));

        item.setAvailability(false);
        libraryItemRepository.save(item);

        return borrowingRepository.save(borrowing);
    }

    @Override
    @Transactional
    public Borrowing returnItem(int borrowingId) {
        Borrowing borrowing = findBorrowingWithItemLocked(borrowingId);

        if (borrowing.getReturnDate() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Item has already been returned");
        }

        LocalDate today = LocalDate.now();
        long overdueDays = ChronoUnit.DAYS.between(
                borrowing.getDueDate().toLocalDate(),
                today);

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

    @Override
    @Transactional
    public Borrowing payFine(int borrowingId) {
        Borrowing borrowing = findBorrowingForUpdate(borrowingId);

        if (borrowing.getReturnDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Return the item before paying the fine");
        }

        if (borrowing.getFineAmount() == null
                || borrowing.getFineAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "There is no fine to pay");
        }

        if (borrowing.isFinePaid()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Fine has already been paid");
        }

        borrowing.setFinePaid(true);
        return borrowingRepository.save(borrowing);
    }

    @Override
    @Transactional
    public Borrowing updateDueDate(int borrowingId, LocalDate dueDate) {
        Borrowing borrowing = findBorrowingForUpdate(borrowingId);
        if (borrowing.getReturnDate() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A returned borrowing cannot be edited");
        }

        if (dueDate == null
                || !dueDate.isAfter(borrowing.getBorrowingDate().toLocalDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Due date must be after the borrowing date");
        }

        borrowing.setDueDate(Date.valueOf(dueDate));
        return borrowingRepository.save(borrowing);
    }

    @Override
    @Transactional
    public void cancelBorrowing(int borrowingId) {
        Borrowing borrowing = findBorrowingWithItemLocked(borrowingId);

        if (borrowing.getReturnDate() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Returned borrowings must remain in the history");
        }

        LibraryItem item = borrowing.getItem();
        borrowingRepository.delete(borrowing);

        item.setAvailability(true);
        libraryItemRepository.save(item);
    }

    @Override
    public Page<Borrowing> findPageWithDetails(Pageable pageable) {
        return borrowingRepository.findPageWithDetails(pageable);
    }

    @Override
    public Page<Borrowing> findMemberHistory(
            int memberId,
            boolean activeOnly,
            Pageable pageable) {

        requireMember(memberId);

        if (activeOnly) {
            return borrowingRepository.findByMember_IdAndReturnDateIsNull(
                    memberId, pageable);
        }

        return borrowingRepository.findByMember_Id(memberId, pageable);
    }

    private Borrowing findBorrowingForUpdate(int borrowingId) {
        Borrowing borrowing = borrowingRepository.findByIdForUpdate(borrowingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Borrowing not found: " + borrowingId));

        // Keep member and item available to the MVC and REST responses.
        borrowingRepository.findByIdWithDetails(borrowingId);

        return borrowing;
    }

    private Borrowing findBorrowingWithItemLocked(int borrowingId) {
        int itemId = borrowingRepository.findItemIdById(borrowingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Borrowing not found: " + borrowingId));

        libraryItemRepository.findByIdForUpdate(itemId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Library item not found: " + itemId));


        borrowingRepository.findByIdWithDetails(borrowingId);

       return findBorrowingForUpdate(borrowingId);
    }

    private Member requireMember(int memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Member not found: " + memberId));
    }
}