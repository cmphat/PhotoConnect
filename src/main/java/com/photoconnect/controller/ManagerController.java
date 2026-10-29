package com.photoconnect.controller;

import com.photoconnect.entity.*;
import com.photoconnect.service.ManagerOperationsService;
import com.photoconnect.service.OperationalAuditService;
import com.photoconnect.service.SupportTicketService;
import com.photoconnect.util.ManagerSecurityUtils;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/manager")
public class ManagerController {
    private final ManagerOperationsService operations;
    private final SupportTicketService support;
    private com.photoconnect.service.PhotographerEvaluationService evaluationService = new com.photoconnect.service.PhotographerEvaluationServiceImpl();

    public ManagerController(ManagerOperationsService operations,
                             SupportTicketService support) {
        this.operations = operations;
        this.support = support;
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setEvaluationService(com.photoconnect.service.PhotographerEvaluationService evaluationService) {
        if (evaluationService != null) {
            this.evaluationService = evaluationService;
        }
    }

    @GetMapping({"", "/dashboard"})
    public String dashboard(HttpSession session, Model model) {
        String denied = ManagerSecurityUtils.requireManager(session); if (denied != null) return denied;
        Long id = SessionSecurityUtils.userId(session);
        model.addAttribute("stats", operations.dashboard(id));
        model.addAttribute("activity", operations.recentActivity(id, 10));
        model.addAttribute("activeTab", "dashboard");
        return "manager-dashboard";
    }

    @GetMapping("/photographers")
    public String photographers(@RequestParam(value = "status", required = false) String status,
                                @RequestParam(value = "page", defaultValue = "0") int page,
                                HttpSession session, Model model) {
        String denied = ManagerSecurityUtils.requireManager(session); if (denied != null) return denied;
        PhotographerVerificationStatus parsed = parseEnum(status, PhotographerVerificationStatus.class);
        addPage(model, "profiles", operations.photographerApplications(SessionSecurityUtils.userId(session), parsed), page);
        model.addAttribute("selectedStatus", parsed != null ? parsed.name() : "ALL");
        model.addAttribute("statuses", PhotographerVerificationStatus.values());
        model.addAttribute("activeTab", "photographers");
        return "manager-photographers";
    }

    @GetMapping("/photographers/{id}")
    public String photographer(@PathVariable Long id, HttpSession session, Model model) {
        String denied = ManagerSecurityUtils.requireManager(session); if (denied != null) return denied;
        PhotographerProfile profile = operations.photographerApplication(SessionSecurityUtils.userId(session), id);
        model.addAttribute("profile", profile);
        model.addAttribute("evaluation", evaluationService.evaluate(profile));
        model.addAttribute("activeTab", "photographers");
        return "manager-photographer-detail";
    }

    @PostMapping("/photographers/{id}/approve")
    public String approvePhotographer(@PathVariable Long id,
                                      @RequestParam(value = "notes", required = false) String notes,
                                      HttpSession session, RedirectAttributes flash) {
        return action(session, flash, "/manager/photographers/" + id,
                () -> operations.approvePhotographer(SessionSecurityUtils.userId(session), id, notes),
                "Đã phê duyệt hồ sơ nhiếp ảnh gia.");
    }

    @PostMapping("/photographers/{id}/reject")
    public String rejectPhotographer(@PathVariable Long id, @RequestParam("reason") String reason,
                                     @RequestParam(value = "notes", required = false) String notes,
                                     HttpSession session, RedirectAttributes flash) {
        return action(session, flash, "/manager/photographers/" + id,
                () -> operations.rejectPhotographer(SessionSecurityUtils.userId(session), id, reason, notes),
                "Đã từ chối hồ sơ và ghi nhận lý do.");
    }

    @PostMapping("/photographers/{id}/suspend")
    public String suspendPhotographer(@PathVariable Long id, @RequestParam("reason") String reason,
                                      @RequestParam(value = "notes", required = false) String notes,
                                      HttpSession session, RedirectAttributes flash) {
        return action(session, flash, "/manager/photographers/" + id,
                () -> operations.suspendPhotographer(SessionSecurityUtils.userId(session), id, reason, notes),
                "Đã đình chỉ hồ sơ nhiếp ảnh gia.");
    }

    @GetMapping("/reports")
    public String reports(@RequestParam(value = "status", required = false) String status,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          HttpSession session, Model model) {
        String denied = ManagerSecurityUtils.requireManager(session); if (denied != null) return denied;
        ReportStatus parsed = parseEnum(status, ReportStatus.class);
        addPage(model, "reports", operations.reports(SessionSecurityUtils.userId(session), parsed), page);
        model.addAttribute("selectedStatus", parsed != null ? parsed.name() : "ALL");
        model.addAttribute("statuses", ReportStatus.values());
        model.addAttribute("activeTab", "reports");
        return "manager-reports";
    }

    @GetMapping("/reports/{id}")
    public String report(@PathVariable Long id, HttpSession session, Model model) {
        String denied = ManagerSecurityUtils.requireManager(session); if (denied != null) return denied;
        model.addAttribute("report", operations.report(SessionSecurityUtils.userId(session), id));
        model.addAttribute("activeTab", "reports");
        return "manager-report-detail";
    }

    @PostMapping("/reports/{id}/investigate")
    public String investigate(@PathVariable Long id, @RequestParam("notes") String notes,
                              HttpSession session, RedirectAttributes flash) {
        return action(session, flash, "/manager/reports/" + id,
                () -> operations.beginReportInvestigation(SessionSecurityUtils.userId(session), id, notes),
                "Báo cáo đã được nhận xử lý.");
    }

    @PostMapping("/reports/{id}/resolve")
    public String resolveReport(@PathVariable Long id, @RequestParam("outcome") String outcome,
                                @RequestParam("reason") String reason,
                                @RequestParam(value = "notes", required = false) String notes,
                                HttpSession session, RedirectAttributes flash) {
        return action(session, flash, "/manager/reports/" + id,
                () -> operations.resolveReport(SessionSecurityUtils.userId(session), id,
                        ReportStatus.valueOf(outcome.toUpperCase()), reason, notes),
                "Đã hoàn tất xử lý báo cáo.");
    }

    @GetMapping("/bookings")
    public String bookings(@RequestParam(value = "status", required = false) String status,
                           @RequestParam(value = "page", defaultValue = "0") int page,
                           HttpSession session, Model model) {
        String denied = ManagerSecurityUtils.requireManager(session); if (denied != null) return denied;
        BookingStatus parsed = parseEnum(status, BookingStatus.class);
        addPage(model, "bookings", operations.bookings(SessionSecurityUtils.userId(session), parsed), page);
        model.addAttribute("selectedStatus", parsed != null ? parsed.name() : "ALL");
        model.addAttribute("statuses", BookingStatus.values());
        model.addAttribute("activeTab", "bookings");
        return "manager-bookings";
    }

    @GetMapping("/bookings/{id}")
    public String booking(@PathVariable Long id, HttpSession session, Model model) {
        String denied = ManagerSecurityUtils.requireManager(session); if (denied != null) return denied;
        model.addAttribute("booking", operations.booking(SessionSecurityUtils.userId(session), id));
        model.addAttribute("activeTab", "bookings");
        return "manager-booking-detail";
    }

    @PostMapping("/bookings/{id}/resolve-dispute")
    public String resolveDispute(@PathVariable Long id, @RequestParam("resolution") String resolution,
                                 @RequestParam("notes") String notes,
                                 HttpSession session, RedirectAttributes flash) {
        return action(session, flash, "/manager/bookings/" + id,
                () -> operations.resolveDispute(SessionSecurityUtils.userId(session), id, resolution, notes),
                "Tranh chấp đã được giải quyết theo chính sách.");
    }

    @GetMapping("/support")
    public String support(@RequestParam(value = "status", required = false) String status,
                          @RequestParam(value = "page", defaultValue = "0") int page,
                          HttpSession session, Model model) {
        String denied = ManagerSecurityUtils.requireManager(session); if (denied != null) return denied;
        SupportTicketStatus parsed = parseEnum(status, SupportTicketStatus.class);
        addPage(model, "tickets", support.listForManager(SessionSecurityUtils.userId(session), parsed), page);
        model.addAttribute("statuses", SupportTicketStatus.values());
        model.addAttribute("selectedStatus", parsed != null ? parsed.name() : "ALL");
        model.addAttribute("activeTab", "support");
        return "manager-support";
    }

    @GetMapping("/support/{id}")
    public String supportDetail(@PathVariable Long id, HttpSession session, Model model) {
        String denied = ManagerSecurityUtils.requireManager(session); if (denied != null) return denied;
        Long managerId = SessionSecurityUtils.userId(session);
        model.addAttribute("ticket", support.getForManager(id, managerId));
        model.addAttribute("messages", support.messagesForManager(id, managerId));
        model.addAttribute("activeTab", "support");
        return "manager-support-detail";
    }

    @PostMapping("/support/{id}/assign")
    public String assign(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        return action(session, flash, "/manager/support/" + id,
                () -> support.assignToSelf(id, SessionSecurityUtils.userId(session)), "Yêu cầu đã được giao cho bạn.");
    }

    @PostMapping("/support/{id}/reply")
    public String supportReply(@PathVariable Long id, @RequestParam("message") String message,
                               HttpSession session, RedirectAttributes flash) {
        return action(session, flash, "/manager/support/" + id,
                () -> support.managerReply(id, SessionSecurityUtils.userId(session), message), "Đã gửi phản hồi.");
    }

    @PostMapping("/support/{id}/resolve")
    public String supportResolve(@PathVariable Long id, @RequestParam("message") String message,
                                 HttpSession session, RedirectAttributes flash) {
        return action(session, flash, "/manager/support/" + id,
                () -> support.resolve(id, SessionSecurityUtils.userId(session), message), "Yêu cầu đã được giải quyết.");
    }

    @GetMapping("/audit")
    public String audit(HttpSession session) {
        String denied = ManagerSecurityUtils.requireManager(session); if (denied != null) return denied;
        return "redirect:/manager/dashboard";
    }

    private String action(HttpSession session, RedirectAttributes flash, String redirect,
                          Runnable operation, String success) {
        String denied = ManagerSecurityUtils.requireManager(session); if (denied != null) return denied;
        try { operation.run(); flash.addFlashAttribute("successMessage", success); }
        catch (RuntimeException ex) { flash.addFlashAttribute("errorMessage", ex.getMessage()); }
        return "redirect:" + redirect;
    }

    private <E extends Enum<E>> E parseEnum(String value, Class<E> type) {
        if (value == null || value.isBlank() || "ALL".equalsIgnoreCase(value)) return null;
        try { return Enum.valueOf(type, value.trim().toUpperCase()); }
        catch (IllegalArgumentException ex) { return null; }
    }

    private void addPage(Model model, String attribute, List<?> items, int requestedPage) {
        int pageSize = 20;
        int totalPages = Math.max(1, (items.size() + pageSize - 1) / pageSize);
        int page = Math.max(0, Math.min(requestedPage, totalPages - 1));
        int from = Math.min(page * pageSize, items.size());
        int to = Math.min(from + pageSize, items.size());
        model.addAttribute(attribute, items.subList(from, to));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
    }
}
