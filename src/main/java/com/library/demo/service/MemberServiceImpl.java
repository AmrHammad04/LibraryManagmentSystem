package com.library.demo.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import jakarta.persistence.EntityManager;
import com.library.demo.dao.BorrowingRepository;
import com.library.demo.dao.MemberRepository;
import com.library.demo.entity.Member;
import com.library.demo.entity.MembershipCard;

@Service
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final BorrowingRepository borrowingRepository;
    private final EntityManager entityManager;

    public MemberServiceImpl(
            MemberRepository memberRepository,
            BorrowingRepository borrowingRepository,
            EntityManager entityManager
        ) {

        this.memberRepository = memberRepository;
        this.borrowingRepository = borrowingRepository;
        this.entityManager = entityManager;
    }

    @Override
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @Override
    public Member findById(int id) {
        return memberRepository.findByIdWithCard(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Member not found: " + id));
    }

    @Override
    @Transactional
    public Member save(Member submitted) {
        if (submitted.getId() == 0) {
            submitted.setMembershipCard(null);

            Member saved = memberRepository.saveAndFlush(submitted);
            String number = Long.toString((long) saved.getId() + 1000);

            MembershipCard card = new MembershipCard(number);
            saved.setMembershipCard(card);
            entityManager.persist(card);

            return saved;
        }

        Member saved = memberRepository.findByIdWithCard(submitted.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Member not found: " + submitted.getId()));

        saved.setName(submitted.getName());
        saved.setPhone(submitted.getPhone());
        saved.setEmail(submitted.getEmail());

        String number = Long.toString((long) saved.getId() + 1000);
        if (saved.getMembershipCard() == null) {
            MembershipCard card = new MembershipCard(number);
            saved.setMembershipCard(card);
            entityManager.persist(card);
        } else {
            saved.getMembershipCard().setCardNumber(number);
        }

        return saved;
    }

    @Override
    @Transactional
    public void deleteById(int id) {
        Member member = findById(id);

        if (borrowingRepository.existsByMember_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete a member with borrowing history");
        }

        memberRepository.delete(member);
    }
}