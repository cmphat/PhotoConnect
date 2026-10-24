package com.photoconnect.controller;

import com.photoconnect.dto.DeliveryAlbumDto;
import com.photoconnect.service.DeliveryService;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/bookings/{bookingId}/delivery")
public class CustomerDeliveryController {

    private static final Logger log = LoggerFactory.getLogger(CustomerDeliveryController.class);

    private final DeliveryService deliveryService;

    public CustomerDeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping
    public String viewDeliveryGallery(@PathVariable Long bookingId,
                                      HttpSession session,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            DeliveryAlbumDto album = deliveryService.getAlbumForCustomer(bookingId, userId);
            model.addAttribute("album", album);
            model.addAttribute("bookingId", bookingId);
            return "customer-delivery";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/bookings/" + bookingId;
        } catch (Exception e) {
            log.error("Failed to load delivery gallery for customer [{}] booking [{}]: {}",
                    userId, bookingId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/bookings/" + bookingId;
        }
    }

    @PostMapping("/confirm")
    public String confirmReceipt(@PathVariable Long bookingId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            deliveryService.confirmDeliveryReceipt(bookingId, userId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Delivery confirmed! Thank you for completing your photoshoot booking.");
        } catch (Exception e) {
            log.error("Failed to confirm delivery for booking [{}]: {}", bookingId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Confirmation failed: " + e.getMessage());
        }
        return "redirect:/bookings/" + bookingId + "/delivery";
    }

    @PostMapping("/request-revision")
    public String requestRevision(@PathVariable Long bookingId,
                                  @RequestParam("revisionNotes") String revisionNotes,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            deliveryService.requestRevision(bookingId, userId, revisionNotes);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Revision request submitted. Your photographer has been notified.");
        } catch (Exception e) {
            log.error("Failed to request revision for booking [{}]: {}", bookingId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Request failed: " + e.getMessage());
        }
        return "redirect:/bookings/" + bookingId + "/delivery";
    }

    @GetMapping("/photos/{photoId}/download")
    public String downloadPhoto(@PathVariable Long bookingId,
                                @PathVariable Long photoId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            String downloadUrl = deliveryService.getAuthorizedDownloadUrl(bookingId, photoId, userId);
            return "redirect:" + downloadUrl;
        } catch (com.photoconnect.exception.UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to download photo [{}] for booking [{}]: {}", photoId, bookingId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Download failed: " + e.getMessage());
            return "redirect:/bookings/" + bookingId + "/delivery";
        }
    }

    @GetMapping("/photos/{photoId}/preview")
    public String previewPhoto(@PathVariable Long bookingId,
                               @PathVariable Long photoId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            String previewUrl = deliveryService.getAuthorizedPreviewUrl(bookingId, photoId, userId);
            return "redirect:" + previewUrl;
        } catch (com.photoconnect.exception.UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to preview photo [{}] for booking [{}]: {}", photoId, bookingId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Preview failed: " + e.getMessage());
            return "redirect:/bookings/" + bookingId + "/delivery";
        }
    }

    @GetMapping("/photos/{photoId}/thumbnail")
    public String thumbnailPhoto(@PathVariable Long bookingId,
                                 @PathVariable Long photoId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Long userId = SessionSecurityUtils.userId(session);
        if (userId == null) {
            return "redirect:/login";
        }

        try {
            String thumbUrl = deliveryService.getAuthorizedThumbnailUrl(bookingId, photoId, userId);
            return "redirect:" + thumbUrl;
        } catch (com.photoconnect.exception.UnauthorizedException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to load thumbnail for photo [{}] booking [{}]: {}", photoId, bookingId, e.getMessage());
            return "redirect:/bookings/" + bookingId + "/delivery";
        }
    }
}
