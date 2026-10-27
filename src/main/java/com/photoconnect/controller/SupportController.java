package com.photoconnect.controller;

import com.photoconnect.entity.SupportTicketCategory;
import com.photoconnect.service.SupportTicketService;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/support")
public class SupportController {
    private final SupportTicketService support;

    public SupportController(SupportTicketService support) { this.support = support; }

    @GetMapping
    public String list(@RequestParam(value = "page", defaultValue = "0") int requestedPage,
                       HttpSession session, Model model) {
        Long userId = SessionSecurityUtils.userId(session); if (userId == null) return "redirect:/login";
        var tickets = support.listOwn(userId);
        int pageSize = 20;
        int totalPages = Math.max(1, (tickets.size() + pageSize - 1) / pageSize);
        int page = Math.max(0, Math.min(requestedPage, totalPages - 1));
        int from = Math.min(page * pageSize, tickets.size());
        model.addAttribute("tickets", tickets.subList(from, Math.min(from + pageSize, tickets.size())));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("categories", SupportTicketCategory.values());
        return "support-tickets";
    }

    @PostMapping
    public String create(@RequestParam("category") String category, @RequestParam("subject") String subject,
                         @RequestParam("description") String description, HttpSession session,
                         RedirectAttributes flash) {
        Long userId = SessionSecurityUtils.userId(session); if (userId == null) return "redirect:/login";
        try {
            var ticket = support.create(userId, SupportTicketCategory.valueOf(category.toUpperCase()), subject, description);
            flash.addFlashAttribute("successMessage", "Yêu cầu hỗ trợ đã được tạo.");
            return "redirect:/support/" + ticket.getId();
        } catch (RuntimeException ex) {
            flash.addFlashAttribute("errorMessage", ex.getMessage()); return "redirect:/support";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, HttpSession session, Model model) {
        Long userId = SessionSecurityUtils.userId(session); if (userId == null) return "redirect:/login";
        model.addAttribute("ticket", support.getForRequester(id, userId));
        model.addAttribute("messages", support.messagesForRequester(id, userId));
        return "support-ticket-detail";
    }

    @PostMapping("/{id}/reply")
    public String reply(@PathVariable Long id, @RequestParam("message") String message,
                        HttpSession session, RedirectAttributes flash) {
        return action(id, session, flash, () -> support.requesterReply(id, SessionSecurityUtils.userId(session), message), "Đã gửi phản hồi.");
    }

    @PostMapping("/{id}/reopen")
    public String reopen(@PathVariable Long id, @RequestParam("message") String message,
                         HttpSession session, RedirectAttributes flash) {
        return action(id, session, flash, () -> support.reopen(id, SessionSecurityUtils.userId(session), message), "Yêu cầu đã được mở lại.");
    }

    private String action(Long id, HttpSession session, RedirectAttributes flash, Runnable op, String success) {
        if (SessionSecurityUtils.userId(session) == null) return "redirect:/login";
        try { op.run(); flash.addFlashAttribute("successMessage", success); }
        catch (RuntimeException ex) { flash.addFlashAttribute("errorMessage", ex.getMessage()); }
        return "redirect:/support/" + id;
    }
}
