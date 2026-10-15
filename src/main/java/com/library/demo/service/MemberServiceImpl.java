package com.library.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.library.demo.dao.MemberRepository;
import com.library.demo.entity.Member;

import com.library.demo.dao.BorrowingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final BorrowingRepository borrowingRepository;

    public MemberServiceImpl(
            MemberRepository memberRepository,
            BorrowingRepository borrowingRepository) {

        this.memberRepository = memberRepository;
        this.borrowingRepository = borrowingRepository;
    }

    @Override
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @Override
    public Member findById(int id) {
        Member member = memberRepository.findById(id).orElse(null);

        if (member == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Member not found: " + id);
        }

        return member;
    }

    @Override
    public Member save(Member member) {
        return memberRepository.save(member);
    }

    @Override
    public void deleteById(int id) {
        Member member = findById(id);

        if (borrowingRepository.existsByMember_Id(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"Cannot delete a member with borrowing history");
        }

        memberRepository.delete(member);
    }
}