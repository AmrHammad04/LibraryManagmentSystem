package com.library.demo.controller;

import com.library.demo.entity.Member;
import com.library.demo.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class MemberPageController {

    private final MemberService memberService;

    public MemberPageController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members")
    public String listMembers(Model model, Authentication authentication) {
        model.addAttribute("members", memberService.findAll());

        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority()));

        model.addAttribute("admin", admin);
        return "members/list";
    }

    @GetMapping("/members/new")
    public String showMemberForm(Model model) {
        model.addAttribute("member", new Member());
        return "members/form";
    }

    @PostMapping("/members")
    public String saveMember(
            @Valid @ModelAttribute("member") Member member,
            BindingResult result) {

        if (result.hasErrors()) {
            return "members/form";
        }

        member.setId(0);
        memberService.save(member);
        return "redirect:/members";
    }

    @GetMapping("/members/{id}/edit")
    public String showEditMemberForm(@PathVariable int id, Model model) {
        Member saved = memberService.findById(id);

        Member formMember = new Member(
                saved.getName(),
                saved.getPhone(),
                saved.getEmail()
        );
        formMember.setId(saved.getId());

        model.addAttribute("member", formMember);
        return "members/form";
    }

    @PostMapping("/members/{id}")
    public String updateMember(
            @PathVariable int id,
            @Valid @ModelAttribute("member") Member formMember,
            BindingResult result) {

        if (id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Member not found: " + id);
        }

        formMember.setId(id);

        if (result.hasErrors()) {
            return "members/form";
        }

        memberService.save(formMember);
        return "redirect:/members";
    }

    @PostMapping("/members/{id}/delete")
    public String deleteMember(
            @PathVariable int id,
            RedirectAttributes redirectAttributes) {

        try {
            memberService.deleteById(id);
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode().value() != 409) {
                throw exception;
            }

            redirectAttributes.addFlashAttribute(
                    "error", exception.getReason());
        }

        return "redirect:/members";
    }
}