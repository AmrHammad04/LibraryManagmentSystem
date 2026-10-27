package com.library.demo.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library.demo.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Integer> {

    @EntityGraph(attributePaths = {"membershipCard"})
    @Query("SELECT member FROM Member member WHERE member.id = :id")
    Optional<Member> findByIdWithCard(@Param("id") int id);
}