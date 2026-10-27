package com.library.demo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.library.demo.dao.BorrowingRepository;
import com.library.demo.dao.LibraryItemRepository;
import com.library.demo.dao.MemberRepository;
import com.library.demo.entity.Book;
import com.library.demo.entity.LibraryItem;
import com.library.demo.entity.Magazine;
import com.library.demo.entity.Member;
import com.library.demo.entity.Newspaper;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final int TOP_RESULTS = 10;

    private final BorrowingRepository borrowingRepository;
    private final LibraryItemRepository libraryItemRepository;
    private final MemberRepository memberRepository;

    public ReportServiceImpl(
            BorrowingRepository borrowingRepository,
            LibraryItemRepository libraryItemRepository,
            MemberRepository memberRepository) {

        this.borrowingRepository = borrowingRepository;
        this.libraryItemRepository = libraryItemRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public LibraryItem findMostBorrowedItem() {
        List<Integer> ids = borrowingRepository.findPopularItemIds(
                PageRequest.of(0, 1));

        if (ids.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No borrowing records found");
        }

        return libraryItemRepository.findById(ids.get(0))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Library item not found: " + ids.get(0)));
    }

    @Override
    public List<LibraryItem> findPopularItems() {
        List<Integer> ids = borrowingRepository.findPopularItemIds(
                PageRequest.of(0, TOP_RESULTS));

        if (ids.isEmpty()) {
            return List.of();
        }

        Map<Integer, LibraryItem> itemsById = new HashMap<>();

        for (LibraryItem item : libraryItemRepository.findAllById(ids)) {
            itemsById.put(item.getId(), item);
        }

        List<LibraryItem> popularItems = new ArrayList<>();

        for (Integer id : ids) {
            LibraryItem item = itemsById.get(id);

            if (item != null) {
                popularItems.add(item);
            }
        }

        return popularItems;
    }

    @Override
    public long countBorrowingsByType(String type) {
        if (type == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Item type is required");
        }

        Class<? extends LibraryItem> itemClass;

        switch (type.trim().toUpperCase(Locale.ROOT)) {
            case "BOOK":
                itemClass = Book.class;
                break;
            case "MAGAZINE":
                itemClass = Magazine.class;
                break;
            case "NEWSPAPER":
                itemClass = Newspaper.class;
                break;
            default:
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Type must be BOOK, MAGAZINE, or NEWSPAPER");
        }

        return borrowingRepository.countBorrowingsByItemType(itemClass);
    }

    @Override
    public List<Member> findTopBorrowers() {
        List<Integer> ids = borrowingRepository.findTopBorrowerIds(
                PageRequest.of(0, TOP_RESULTS));

        if (ids.isEmpty()) {
            return List.of();
        }

        Map<Integer, Member> membersById = new HashMap<>();

        for (Member member : memberRepository.findAllById(ids)) {
            membersById.put(member.getId(), member);
        }

        List<Member> topBorrowers = new ArrayList<>();

        for (Integer id : ids) {
            Member member = membersById.get(id);

            if (member != null) {
                topBorrowers.add(member);
            }
        }

        return topBorrowers;
    }

    @Override
    public BigDecimal calculateTotalFinesCollected() {
        BigDecimal total = borrowingRepository.sumCollectedFines();
        return total == null ? BigDecimal.ZERO : total;
    }
}