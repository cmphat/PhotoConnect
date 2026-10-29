package com.photoconnect.controller;

import com.photoconnect.dto.CustomerBankAccountDto;
import com.photoconnect.dto.CustomerBankAccountRequest;
import com.photoconnect.service.CustomerBankAccountService;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/customer/bank-accounts")
public class CustomerBankAccountController {

    private final CustomerBankAccountService bankAccountService;

    public CustomerBankAccountController(CustomerBankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping
    public String listBankAccounts(HttpSession session, Model model) {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        List<CustomerBankAccountDto> accounts = bankAccountService.listAccounts(userId);
        model.addAttribute("accounts", accounts);
        if (!model.containsAttribute("bankForm")) {
            model.addAttribute("bankForm", new CustomerBankAccountRequest());
        }
        model.addAttribute("activeNav", "bank-accounts");
        return "customer-bank-accounts";
    }

    @PostMapping
    public String addBankAccount(@Valid @ModelAttribute("bankForm") CustomerBankAccountRequest request,
                                 BindingResult bindingResult,
                                 HttpSession session,
                                 RedirectAttributes flash,
                                 Model model) {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            flash.addFlashAttribute("org.springframework.validation.BindingResult.bankForm", bindingResult);
            flash.addFlashAttribute("bankForm", request);
            flash.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/customer/bank-accounts";
        }

        try {
            bankAccountService.createAccount(userId, request);
            flash.addFlashAttribute("successMessage", "Đã lưu tài khoản ngân hàng thành công.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/customer/bank-accounts";
    }

    @PostMapping("/{id}/default")
    public String setDefault(@PathVariable Long id,
                             HttpSession session,
                             RedirectAttributes flash) {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            bankAccountService.setDefaultAccount(userId, id);
            flash.addFlashAttribute("successMessage", "Đã đặt tài khoản làm mặc định nhận hoàn tiền.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/customer/bank-accounts";
    }

    @PostMapping("/{id}/delete")
    public String deleteAccount(@PathVariable Long id,
                                HttpSession session,
                                RedirectAttributes flash) {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            bankAccountService.deleteAccount(userId, id);
            flash.addFlashAttribute("successMessage", "Đã xóa tài khoản ngân hàng.");
        } catch (Exception e) {
            flash.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/customer/bank-accounts";
    }
}
