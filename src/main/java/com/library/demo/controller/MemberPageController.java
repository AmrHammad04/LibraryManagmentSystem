package com.library.demo.controller;

import com.library.demo.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import com.library.demo.entity.Member;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
public class MemberPageController {

    private final MemberService memberService;

    public MemberPageController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members")
    public String listMembers(Model model) {
        model.addAttribute("members", memberService.findAll());
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
        model.addAttribute("member", memberService.findById(id));
        return "members/form";
    }

    @PostMapping("/members/{id}")
    public String updateMember(
            @PathVariable int id,
            @Valid @ModelAttribute("member") Member formMember,
            BindingResult result) {

        if (result.hasErrors()) {
            formMember.setId(id);
            return "members/form";
        }

        Member member = memberService.findById(id);
        member.setName(formMember.getName());
        member.setPhone(formMember.getPhone());
        member.setEmail(formMember.getEmail());

        memberService.save(member);
        return "redirect:/members";
    }

    @PostMapping("/members/{id}/delete")
    public String deleteMember(@PathVariable int id,RedirectAttributes redirectAttributes) {

        try {
            memberService.deleteById(id);
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode().value() != 409) {
                throw exception;
            }

            redirectAttributes.addFlashAttribute("error", exception.getReason());
        }

        return "redirect:/members";
    }

}