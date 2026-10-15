package com.library.demo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.library.demo.dao.BorrowingRepository;
import com.library.demo.entity.Book;
import com.library.demo.entity.Borrowing;
import com.library.demo.entity.LibraryItem;
import com.library.demo.entity.Magazine;
import com.library.demo.entity.Member;
import com.library.demo.entity.Newspaper;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final BorrowingRepository borrowingRepository;

    public ReportServiceImpl(BorrowingRepository borrowingRepository) {
        this.borrowingRepository = borrowingRepository;
    }

    @Override
    public LibraryItem findMostBorrowedItem() {
        List<LibraryItem> items = findPopularItems();

        if (items.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"No borrowing records found");
        }

        return items.get(0);
    }

    @Override
    public List<LibraryItem> findPopularItems() {
        List<Borrowing> borrowings = borrowingRepository.findAll();
        List<LibraryItem> items = new ArrayList<>();

        for (Borrowing borrowing : borrowings) {
            LibraryItem item = borrowing.getItem();
            boolean found = false;

            for (LibraryItem existingItem : items) {
                if (existingItem.getId() == item.getId()) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                items.add(item);
            }
        }

        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {

                int firstCount = countItemBorrowings(
                        items.get(i).getId(), borrowings);

                int secondCount = countItemBorrowings(
                        items.get(j).getId(), borrowings);

                if (secondCount > firstCount) {
                    LibraryItem temporary = items.get(i);
                    items.set(i, items.get(j));
                    items.set(j, temporary);
                }
            }
        }

        return items;
    }

    @Override
    public long countBorrowingsByType(String type) {

        if (type == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Item type is required");
        }

        type = type.trim();

        if (!type.equalsIgnoreCase("BOOK")
                && !type.equalsIgnoreCase("MAGAZINE")
                && !type.equalsIgnoreCase("NEWSPAPER")) {

            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Type must be BOOK, MAGAZINE, or NEWSPAPER");
        }

        List<Borrowing> borrowings = borrowingRepository.findAll();
        long count = 0;

        for (Borrowing borrowing : borrowings) {
            LibraryItem item = borrowing.getItem();

            if (type.equalsIgnoreCase("BOOK")
                    && item instanceof Book) {
                count++;
            } else if (type.equalsIgnoreCase("MAGAZINE")
                    && item instanceof Magazine) {
                count++;
            } else if (type.equalsIgnoreCase("NEWSPAPER")
                    && item instanceof Newspaper) {
                count++;
            }
        }

        return count;
    }

    @Override
    public List<Member> findTopBorrowers() {
        List<Borrowing> borrowings = borrowingRepository.findAll();
        List<Member> members = new ArrayList<>();

        for (Borrowing borrowing : borrowings) {
            Member member = borrowing.getMember();
            boolean found = false;

            for (Member existingMember : members) {
                if (existingMember.getId() == member.getId()) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                members.add(member);
            }
        }

        for (int i = 0; i < members.size(); i++) {
            for (int j = i + 1; j < members.size(); j++) {

                int firstCount = countMemberBorrowings(
                        members.get(i).getId(), borrowings);

                int secondCount = countMemberBorrowings(
                        members.get(j).getId(), borrowings);

                if (secondCount > firstCount) {
                    Member temporary = members.get(i);
                    members.set(i, members.get(j));
                    members.set(j, temporary);
                }
            }
        }

        return members;
    }

    @Override
    public BigDecimal calculateTotalFinesCollected() {
        List<Borrowing> borrowings = borrowingRepository.findAll();
        BigDecimal total = BigDecimal.ZERO;

        for (Borrowing borrowing : borrowings) {
            if (borrowing.isFinePaid()
                    && borrowing.getFineAmount() != null) {

                total = total.add(borrowing.getFineAmount());
            }
        }

        return total;
    }

    private int countItemBorrowings(
            int itemId, List<Borrowing> borrowings) {

        int count = 0;

        for (Borrowing borrowing : borrowings) {
            if (borrowing.getItem().getId() == itemId) {
                count++;
            }
        }

        return count;
    }

    private int countMemberBorrowings(
            int memberId, List<Borrowing> borrowings) {

        int count = 0;

        for (Borrowing borrowing : borrowings) {
            if (borrowing.getMember().getId() == memberId) {
                count++;
            }
        }

        return count;
    }
}