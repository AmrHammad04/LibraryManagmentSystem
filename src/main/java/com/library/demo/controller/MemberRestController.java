package com.library.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.library.demo.entity.Member;
import com.library.demo.service.MemberService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class MemberRestController {

    private final MemberService memberService;

    public MemberRestController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members")
    public List<Member> findAll() {
        return memberService.findAll();
    }

    @GetMapping("/members/{id}")
    public Member findById(@PathVariable int id) {
        return memberService.findById(id);
    }

    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    public Member addMember(@Valid @RequestBody Member member) {
        member.setId(0);
        return memberService.save(member);
    }

    @PutMapping("/members/{id}")
    public Member updateMember(
            @PathVariable int id,
            @Valid @RequestBody Member member) {

        if (id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Member not found: " + id);
        }

        member.setId(id);
        return memberService.save(member);
    }

    @DeleteMapping("/members/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMember(@PathVariable int id) {
        memberService.deleteById(id);
    }
}