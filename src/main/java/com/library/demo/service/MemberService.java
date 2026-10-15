package com.library.demo.service;

import java.util.List;
import com.library.demo.entity.Member;

public interface MemberService {

    List<Member> findAll();
    Member findById(int id);
    Member save(Member member);
    void deleteById(int id);
}