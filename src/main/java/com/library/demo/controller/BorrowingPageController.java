package com.library.demo.controller;


import com.library.demo.dto.BorrowingForm;
import com.library.demo.dto.DueDateForm;
import com.library.demo.entity.Borrowing;
import com.library.demo.service.BorrowingService;
import com.library.demo.service.LibraryItemService;
import com.library.demo.service.MemberService;

import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;


@Controller
public class BorrowingPageController {

    private final BorrowingService borrowingService;
    private final MemberService memberService;
    private final LibraryItemService libraryItemService;

    public BorrowingPageController(
            BorrowingService borrowingService,
            MemberService memberService,
            LibraryItemService libraryItemService) {

        this.borrowingService = borrowingService;
        this.memberService = memberService;
        this.libraryItemService = libraryItemService;
    }

    @GetMapping("/borrowings")
    public String listBorrowings(
            @RequestParam(defaultValue = "0") int page,
            Model model,
            Authentication authentication) {

        int pageNumber = Math.max(page, 0);
        Page<Borrowing> borrowingPage = borrowingService.findPageWithDetails(
                PageRequest.of(pageNumber, 20));

        if (pageNumber > 0 && pageNumber >= borrowingPage.getTotalPages()) {
            int lastPage = Math.max(borrowingPage.getTotalPages() - 1, 0);
            return "redirect:/borrowings?page=" + lastPage;
        }

        model.addAttribute("borrowings", borrowingPage.getContent());
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", borrowingPage.getTotalPages());

        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        "ROLE_ADMIN".equals(authority.getAuthority()));

        model.addAttribute("admin", admin);
        return "borrowings/list";
    }

    @GetMapping("/borrowings/new")
    public String showBorrowingForm(Model model) {
        model.addAttribute("borrowingForm", new BorrowingForm());
        model.addAttribute("members", memberService.findAll());
        model.addAttribute("availableItems", libraryItemService.findAvailable());
        return "borrowings/form";
    }

    @PostMapping("/borrowings")
    public String borrowItem(
            @Valid @ModelAttribute("borrowingForm") BorrowingForm borrowingForm,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("members", memberService.findAll());
            model.addAttribute("availableItems", libraryItemService.findAvailable());
            return "borrowings/form";
        }

        borrowingService.borrowItem(
                borrowingForm.getMemberId(),
                borrowingForm.getItemId(),
                borrowingForm.getDurationDays());

        return "redirect:/borrowings";
    }

    @GetMapping("/borrowings/{id}/edit")
    public String showEditDueDateForm(@PathVariable int id, Model model) {
        Borrowing borrowing = borrowingService.findById(id);

        if (borrowing.getReturnDate() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A returned borrowing cannot be edited");
        }

        DueDateForm form = new DueDateForm();
        form.setDueDate(borrowing.getDueDate().toLocalDate());

        model.addAttribute("dueDateForm", form);
        model.addAttribute("borrowing", borrowing);
        return "borrowings/edit";
    }

    @PostMapping("/borrowings/{id}/edit")
    public String updateDueDate(
            @PathVariable int id,
            @Valid @ModelAttribute("dueDateForm") DueDateForm form,
            BindingResult result,
            Model model) {

        Borrowing borrowing = borrowingService.findById(id);

        if (borrowing.getReturnDate() != null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A returned borrowing cannot be edited");
        }

        if (form.getDueDate() != null
                && !form.getDueDate().isAfter(
                        borrowing.getBorrowingDate().toLocalDate())) {
            result.rejectValue(
                    "dueDate",
                    "invalidDueDate",
                    "Due date must be after the borrowing date");
        }

        if (result.hasErrors()) {
            model.addAttribute("borrowing", borrowing);
            return "borrowings/edit";
        }

        borrowingService.updateDueDate(id, form.getDueDate());
        return "redirect:/borrowings";
    }

    @PostMapping("/borrowings/{id}/delete")
    public String cancelBorrowing(
            @PathVariable int id,
            RedirectAttributes redirectAttributes) {

        try {
            borrowingService.cancelBorrowing(id);
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode().value() != 409) {
                throw exception;
            }

            redirectAttributes.addFlashAttribute(
                    "error", exception.getReason());
        }

        return "redirect:/borrowings";
    }

    @PostMapping("/borrowings/{id}/return")
    public String returnItem(@PathVariable int id) {
        borrowingService.returnItem(id);
        return "redirect:/borrowings";
    }

    @PostMapping("/borrowings/{id}/pay-fine")
    public String payFine(@PathVariable int id) {
        borrowingService.payFine(id);
        return "redirect:/borrowings";
    }

    @GetMapping("/members/{memberId}/borrowings")
    public String memberHistory(
            @PathVariable int memberId,
            @RequestParam(defaultValue = "false") boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        int pageNumber = Math.max(page, 0);

        Page<Borrowing> history = borrowingService.findMemberHistory(
                memberId,
                activeOnly,
                PageRequest.of(pageNumber, 20, Sort.by("id").descending())
        );

        if (pageNumber > 0 && pageNumber >= history.getTotalPages()) {
            int lastPage = Math.max(history.getTotalPages() - 1, 0);
            return "redirect:/members/" + memberId
                    + "/borrowings?activeOnly=" + activeOnly
                    + "&page=" + lastPage;
        }

        model.addAttribute("memberId", memberId);
        model.addAttribute("borrowings", history.getContent());
        model.addAttribute("activeOnly", activeOnly);
        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("totalPages", history.getTotalPages());

        return "members/history";
    }
}