package com.photoconnect.controller;

import com.photoconnect.dto.DeliveryAlbumDto;
import com.photoconnect.entity.UserRole;
import com.photoconnect.service.DeliveryService;
import com.photoconnect.util.SessionSecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/photographer/bookings/{bookingId}/delivery")
public class PhotographerDeliveryController {

    private static final Logger log = LoggerFactory.getLogger(PhotographerDeliveryController.class);

    private final DeliveryService deliveryService;

    public PhotographerDeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping
    public String viewDeliveryStudio(@PathVariable Long bookingId,
                                     HttpSession session,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);

        try {
            DeliveryAlbumDto album = deliveryService.getOrCreateAlbumForPhotographer(bookingId, userId);
            model.addAttribute("album", album);
            model.addAttribute("bookingId", bookingId);
            return "photographer-delivery";
        } catch (Exception e) {
            log.error("Failed to load delivery studio for booking [{}]: {}", bookingId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/photographer/bookings/" + bookingId;
        }
    }

    @PostMapping("/upload")
    public String uploadPhoto(@PathVariable Long bookingId,
                              @RequestParam("photo") MultipartFile photo,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);

        try {
            deliveryService.addPhoto(bookingId, userId, photo);
            redirectAttributes.addFlashAttribute("successMessage", "Photograph uploaded successfully.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Failed to upload photo for booking [{}]: {}", bookingId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Upload failed: " + e.getMessage());
        }
        return "redirect:/photographer/bookings/" + bookingId + "/delivery";
    }

    @PostMapping("/photos/{photoId}/delete")
    public String deletePhoto(@PathVariable Long bookingId,
                              @PathVariable Long photoId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);

        try {
            deliveryService.removePhoto(bookingId, photoId, userId);
            redirectAttributes.addFlashAttribute("successMessage", "Photograph removed from album.");
        } catch (Exception e) {
            log.error("Failed to delete photo [{}] for booking [{}]: {}", photoId, bookingId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove photo: " + e.getMessage());
        }
        return "redirect:/photographer/bookings/" + bookingId + "/delivery";
    }

    @PostMapping("/deliver")
    public String deliverAlbum(@PathVariable Long bookingId,
                               @RequestParam(value = "notes", required = false) String notes,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);

        try {
            deliveryService.deliverAlbum(bookingId, userId, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Finished photographs delivered to client successfully.");
        } catch (Exception e) {
            log.error("Failed to deliver album for booking [{}]: {}", bookingId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Delivery failed: " + e.getMessage());
        }
        return "redirect:/photographer/bookings/" + bookingId + "/delivery";
    }

    @GetMapping("/photos/{photoId}/download")
    public String downloadPhoto(@PathVariable Long bookingId,
                                @PathVariable Long photoId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        String redirect = SessionSecurityUtils.requireRole(session, UserRole.PHOTOGRAPHER);
        if (redirect != null) return redirect;
        Long userId = SessionSecurityUtils.userId(session);

        try {
            String downloadUrl = deliveryService.getAuthorizedDownloadUrl(bookingId, photoId, userId);
            return "redirect:" + downloadUrl;
        } catch (Exception e) {
            log.error("Failed to download photo [{}] for booking [{}]: {}", photoId, bookingId, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Download failed: " + e.getMessage());
            return "redirect:/photographer/bookings/" + bookingId + "/delivery";
        }
    }
}
