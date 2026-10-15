package com.library.demo.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.library.demo.entity.Member;
import com.library.demo.service.MemberService;

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
    public Member findById(@PathVariable("id") int id) {
        return memberService.findById(id);
    }

    @PostMapping("/members")
    @ResponseStatus(HttpStatus.CREATED)
    public Member addMember(@RequestBody Member member) {
        member.setId(0);
        return memberService.save(member);
    }

    @PutMapping("/members/{id}")
    public Member updateMember(
            @PathVariable("id") int id,
            @RequestBody Member member) {

        memberService.findById(id);
        member.setId(id);

        return memberService.save(member);
    }

    @DeleteMapping("/members/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMember(@PathVariable("id") int id) {
        memberService.deleteById(id);
    }
}