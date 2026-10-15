package com.library.demo.entity;

import java.math.BigDecimal;
import java.sql.Date;

import jakarta.persistence.*;

@Entity
@Table(name = "borrowing")
public class Borrowing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private LibraryItem item;

    @Column(name = "borrowing_date", nullable = false)
    private Date borrowingDate;

    @Column(name = "due_date", nullable = false)
    private Date dueDate;

    @Column(name = "return_date")
    private Date returnDate;

    @Column(name = "fine_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    @Column(name = "fine_paid", nullable = false)
    private boolean finePaid = false;

    public boolean isFinePaid() {
        return finePaid;
    }

    public void setFinePaid(boolean finePaid) {
        this.finePaid = finePaid;
    }

    public Borrowing() {
    }

    public Borrowing(Member member, LibraryItem item,
                     Date borrowingDate, Date dueDate) {
        this.member = member;
        this.item = item;
        this.borrowingDate = borrowingDate;
        this.dueDate = dueDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public LibraryItem getItem() {
        return item;
    }

    public void setItem(LibraryItem item) {
        this.item = item;
    }

    public Date getBorrowingDate() {
        return borrowingDate;
    }

    public void setBorrowingDate(Date borrowingDate) {
        this.borrowingDate = borrowingDate;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(Date returnDate) {
        this.returnDate = returnDate;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }

    @Override
    public String toString() {
        return "Borrowing{" +
                "id=" + id +
                ", borrowingDate=" + borrowingDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", fineAmount=" + fineAmount +
                '}';
    }
}