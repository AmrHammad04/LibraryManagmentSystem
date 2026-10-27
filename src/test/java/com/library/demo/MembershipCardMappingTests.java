package com.library.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.library.demo.entity.Member;
import com.library.demo.entity.MembershipCard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Transactional
class MembershipCardMappingTests {

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    void savingMemberAlsoSavesCard() {
        Member member = new Member("Test Member", "0599000000", "card-test@example.com");
        MembershipCard card = new MembershipCard();
        card.setCardNumber("TEST-" + UUID.randomUUID());
        member.setMembershipCard(card);

        entityManager.persist(member);
        entityManager.flush();

        int memberId = member.getId();
        int cardId = card.getId();
        entityManager.clear();

        Member savedMember = entityManager.find(Member.class, memberId);

        assertEquals(cardId, savedMember.getMembershipCard().getId());
        assertEquals(memberId, savedMember.getMembershipCard().getMember().getId());
    }

    @Test
    void deletingMemberAlsoDeletesCard() {
        Member member = new Member("Test Member", "0599000001", "delete-test@example.com");
        MembershipCard card = new MembershipCard();
        card.setCardNumber("TEST-" + UUID.randomUUID());
        member.setMembershipCard(card);

        entityManager.persist(member);
        entityManager.flush();

        int memberId = member.getId();
        int cardId = card.getId();

        entityManager.remove(member);
        entityManager.flush();
        entityManager.clear();

        assertNull(entityManager.find(Member.class, memberId));
        assertNull(entityManager.find(MembershipCard.class, cardId));
    }

    @Test
    void unlinkingMemberDeletesCard() {
        Member member = new Member(
                "Test Member",
                "0599000002",
                "unlink-test@example.com"
        );
        MembershipCard card = new MembershipCard(
                "TEST-" + UUID.randomUUID()
        );
        member.setMembershipCard(card);

        entityManager.persist(member);
        entityManager.flush();

        int memberId = member.getId();
        int cardId = card.getId();

        member.setMembershipCard(null);
        entityManager.flush();
        entityManager.clear();

        assertNull(entityManager.find(MembershipCard.class, cardId));
        assertNull(entityManager.find(Member.class, memberId).getMembershipCard());
    }
}