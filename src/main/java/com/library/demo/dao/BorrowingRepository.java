package com.library.demo.dao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library.demo.entity.Borrowing;
import com.library.demo.entity.LibraryItem;

import jakarta.persistence.LockModeType;

public interface BorrowingRepository extends JpaRepository<Borrowing, Integer> {

    List<Borrowing> findByMember_Id(int memberId);

    List<Borrowing> findByMember_IdAndReturnDateIsNull(int memberId);

    List<Borrowing> findByReturnDateIsNull();

    boolean existsByItem_IdAndReturnDateIsNull(int itemId);

    boolean existsByMember_Id(int memberId);

    boolean existsByItem_Id(int itemId);

    @Query("""
            SELECT b.item.id
            FROM Borrowing b
            WHERE b.id = :id
            """)
    Optional<Integer> findItemIdById(@Param("id") int id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b
            FROM Borrowing b
            WHERE b.id = :id
            """)
    Optional<Borrowing> findByIdForUpdate(@Param("id") int id);

    @Query("""
            SELECT b
            FROM Borrowing b
            JOIN FETCH b.member
            JOIN FETCH b.item
            WHERE b.id = :id
            """)
    Optional<Borrowing> findByIdWithDetails(@Param("id") int id);

    @Query(
            value = """
                    SELECT b
                    FROM Borrowing b
                    JOIN FETCH b.member
                    JOIN FETCH b.item
                    ORDER BY b.id DESC
                    """,
            countQuery = "SELECT COUNT(b) FROM Borrowing b"
    )
    Page<Borrowing> findPageWithDetails(Pageable pageable);

    @EntityGraph(attributePaths = {"member", "item"})
    Page<Borrowing> findByMember_Id(int memberId, Pageable pageable);

    @EntityGraph(attributePaths = {"member", "item"})
    Page<Borrowing> findByMember_IdAndReturnDateIsNull(
            int memberId, Pageable pageable);

    @Query("""
            SELECT b.item.id
            FROM Borrowing b
            GROUP BY b.item.id
            ORDER BY COUNT(b) DESC, b.item.id ASC
            """)
    List<Integer> findPopularItemIds(Pageable pageable);

    @Query("""
            SELECT b.member.id
            FROM Borrowing b
            GROUP BY b.member.id
            ORDER BY COUNT(b) DESC, b.member.id ASC
            """)
    List<Integer> findTopBorrowerIds(Pageable pageable);

    @Query("""
            SELECT COUNT(b)
            FROM Borrowing b
            WHERE TYPE(b.item) = :itemClass
            """)
    long countBorrowingsByItemType(
            @Param("itemClass") Class<? extends LibraryItem> itemClass);

    @Query("""
            SELECT SUM(b.fineAmount)
            FROM Borrowing b
            WHERE b.finePaid = true
            """)
    BigDecimal sumCollectedFines();
}