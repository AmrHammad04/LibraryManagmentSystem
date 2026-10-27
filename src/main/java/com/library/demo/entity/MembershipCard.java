package com.library.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "membership_card")
public class MembershipCard {

    @Id
    @Column(name = "id")
    private int id;

    @JsonIgnore
    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id", nullable = false)
    private Member member;

    @NotBlank(message = "Card number is required")
    @Column(name = "card_number", nullable = false, unique = true)
    private String cardNumber;


    public MembershipCard() {
    }

    public MembershipCard(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public int getId() {
        return id;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public Member getMember() {
        return member;
    }

    void setMember(Member member) {
        this.member = member;
    }
}