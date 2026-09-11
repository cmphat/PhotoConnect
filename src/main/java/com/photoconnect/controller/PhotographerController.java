package com.photoconnect.controller;

import com.photoconnect.dto.PhotographerPublicDto;
import com.photoconnect.dto.PhotographerSearchRequest;
import com.photoconnect.dto.PortfolioImagePublicDto;
import com.photoconnect.service.PortfolioService;
import com.photoconnect.service.PublicPhotographerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Public photographer marketplace controller.
 *
 * WHY no session guard:
 *   These are fully public routes — guest access is intentional.
 *   The APPROVED-only rule is enforced by PublicPhotographerService
 *   and the repository JPQL query, not by restricting who can visit.
 *
 * WHY we redirect instead of 404 for non-approved profiles:
 *   Consistent with the project pattern (admin controller does the same).
 *   Avoids revealing the distinction between "id not found" vs "id exists but
 *   not approved" — both redirect cleanly without leaking internal state.
 */
@Controller
@RequestMapping("/photographers")
public class PhotographerController {

    private final PublicPhotographerService publicPhotographerService;
    private final PortfolioService portfolioService;
    private final com.photoconnect.service.ReviewService reviewService;

    public PhotographerController(PublicPhotographerService publicPhotographerService,
                                  PortfolioService portfolioService,
                                  com.photoconnect.service.ReviewService reviewService) {
        this.publicPhotographerService = publicPhotographerService;
        this.portfolioService = portfolioService;
        this.reviewService = reviewService;
    }

    // ── GET /photographers ────────────────────────────────────────────────

    /**
     * Public photographer listing and search — no login required.
     * Supports filtering by keyword, city, price range, and experience.
     * Returns only APPROVED profiles via the service layer.
     */
    @GetMapping
    public String listPhotographers(@ModelAttribute("searchRequest") PhotographerSearchRequest searchRequest,
                                    Model model) {
        List<PhotographerPublicDto> photographers;

        if (searchRequest != null && !searchRequest.isValid()) {
            model.addAttribute("errorMessage", searchRequest.getValidationError());
            photographers = publicPhotographerService.listApprovedPhotographers();
        } else {
            photographers = publicPhotographerService.searchPhotographers(searchRequest);
        }

        model.addAttribute("photographers", photographers);
        model.addAttribute("resultCount", photographers.size());
        model.addAttribute("hasFilters", searchRequest != null && searchRequest.hasFilters());
        return "photographers";
    }

    // ── GET /photographers/{id} ───────────────────────────────────────────

    /**
     * Public photographer detail — no login required.
     * If the profile does not exist or is not APPROVED, redirects to the listing.
     * Also loads the public portfolio gallery (publicId excluded).
     */
    @GetMapping("/{id}")
    public String photographerDetail(@PathVariable Long id,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {
        try {
            PhotographerPublicDto photographer =
                    publicPhotographerService.getApprovedPhotographerById(id);
            model.addAttribute("photographer", photographer);

            // Load portfolio images — safe public DTOs (no publicId)
            List<PortfolioImagePublicDto> portfolioImages =
                    portfolioService.getPublicPortfolioForProfile(id);
            model.addAttribute("portfolioImages", portfolioImages);

            // Load reviews — safe public DTOs
            List<com.photoconnect.dto.ReviewDto> reviews =
                    reviewService.getReviewsForPhotographer(id);
            model.addAttribute("reviews", reviews);

            return "photographer-detail";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "The photographer profile you requested is not available.");
            return "redirect:/photographers";
        }
    }
}
