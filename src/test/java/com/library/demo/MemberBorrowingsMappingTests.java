package com.library.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Date;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.library.demo.dao.BorrowingRepository;
import com.library.demo.entity.Book;
import com.library.demo.entity.Borrowing;
import com.library.demo.entity.Member;
import com.library.demo.entity.MembershipCard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@Transactional
class MemberBorrowingsMappingTests {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Test
    void memberBorrowingsLoadWhenAccessed() {
        Member member = createMember();
        Book book = createBook();

        Borrowing borrowing = new Borrowing(
                member,
                book,
                Date.valueOf("2025-01-10"),
                Date.valueOf("2025-01-24")
        );
        entityManager.persist(borrowing);
        entityManager.flush();

        int memberId = member.getId();
        int borrowingId = borrowing.getId();
        entityManager.clear();

        Member loadedMember = entityManager.find(Member.class, memberId);

        assertFalse(entityManagerFactory.getPersistenceUnitUtil()
                .isLoaded(loadedMember, "borrowings"));

        assertEquals(1, loadedMember.getBorrowings().size());
        assertEquals(borrowingId, loadedMember.getBorrowings().get(0).getId());

        assertTrue(entityManagerFactory.getPersistenceUnitUtil()
                .isLoaded(loadedMember, "borrowings"));
    }

    @Test
    void findByIdWithDetailsReturnsMemberAndItem() {
        Member member = createMember();
        Book book = createBook();

        Borrowing borrowing = new Borrowing(
                member,
                book,
                Date.valueOf("2025-02-10"),
                Date.valueOf("2025-02-24")
        );
        entityManager.persist(borrowing);
        entityManager.flush();

        int borrowingId = borrowing.getId();
        int memberId = member.getId();
        int bookId = book.getId();
        entityManager.clear();

        Borrowing withDetails = borrowingRepository
                .findByIdWithDetails(borrowingId)
                .orElseThrow();

        assertEquals(memberId, withDetails.getMember().getId());
        assertEquals(bookId, withDetails.getItem().getId());
    }

    private Member createMember() {
        Member member = new Member(
                "Test Member",
                "0599000003",
                "borrow-test@example.com"
        );

        MembershipCard card = new MembershipCard();
        card.setCardNumber("TEST-" + UUID.randomUUID());
        member.setMembershipCard(card);

        entityManager.persist(member);
        return member;
    }

    private Book createBook() {
        Book book = new Book(
                "Test Book",
                "Test Author",
                2020,
                true,
                "9780132350884",
                100
        );

        entityManager.persist(book);
        return book;
    }


    @Test
    void deletingBorrowingKeepsMemberAndBook() {
        Member member = createMember();
        Book book = createBook();

        Borrowing borrowing = new Borrowing(
                member,
                book,
                Date.valueOf("2025-03-10"),
                Date.valueOf("2025-03-24")
        );
        entityManager.persist(borrowing);
        entityManager.flush();

        int borrowingId = borrowing.getId();
        int memberId = member.getId();
        int bookId = book.getId();

        entityManager.remove(borrowing);
        entityManager.flush();
        entityManager.clear();

        assertNull(entityManager.find(Borrowing.class, borrowingId));
        assertNotNull(entityManager.find(Member.class, memberId));
        assertNotNull(entityManager.find(Book.class, bookId));
        assertTrue(entityManager.find(Member.class, memberId).getBorrowings().isEmpty());
    }
}