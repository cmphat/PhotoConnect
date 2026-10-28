package com.photoconnect.controller;

import com.photoconnect.service.PhotographerAiReviewService;
import com.photoconnect.util.ManagerSecurityUtils;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manager/photographers")
public class ManagerAiReviewController {
    private final PhotographerAiReviewService adviceService;

    public ManagerAiReviewController(PhotographerAiReviewService adviceService) {
        this.adviceService = adviceService;
    }

    @PostMapping("/{id}/ai-review")
    public String review(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        String denied = ManagerSecurityUtils.requireManager(session);
        if (denied != null) return denied;
        try {
            flash.addFlashAttribute("aiAdvice", adviceService.assess(SessionSecurityUtils.userId(session), id));
        } catch (RuntimeException ex) {
            flash.addFlashAttribute("errorMessage", "Không thể phân tích hồ sơ này; vui lòng kiểm tra quyền và trạng thái hồ sơ.");
        }
        return "redirect:/manager/photographers/" + id;
    }
}
