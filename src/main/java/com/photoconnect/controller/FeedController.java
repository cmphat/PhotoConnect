package com.photoconnect.controller;

import com.photoconnect.dto.FeedCreatorDto;
import com.photoconnect.dto.FeedPostDto;
import com.photoconnect.entity.PortfolioCategory;
import com.photoconnect.service.FeedService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping("/feed")
    public String showFeed(@RequestParam(value = "category", required = false) String category,
                           @RequestParam(value = "authorType", required = false) String authorType,
                           HttpSession session,
                           Model model) {
        Long currentUserId = null;
        Object sessionUid = session.getAttribute("userId");
        if (sessionUid instanceof Number num) {
            currentUserId = num.longValue();
        }

        Object roleObj = session.getAttribute("userRole");
        if (roleObj != null && "MANAGER".equalsIgnoreCase(roleObj.toString())) {
            return "redirect:/manager/dashboard";
        }
        boolean isPhotographer = roleObj != null && "PHOTOGRAPHER".equalsIgnoreCase(roleObj.toString());
        boolean isCustomer = roleObj != null && "CUSTOMER".equalsIgnoreCase(roleObj.toString());

        // Feed displays works from photographers and shoot requests from customers.
        // Users can filter by authorType ("ALL", "PHOTOGRAPHER", "CUSTOMER"). Default is "ALL".
        String activeAuthorType = (authorType != null && !authorType.isBlank()) ? authorType.trim().toUpperCase() : "ALL";

        List<FeedPostDto> posts = feedService.getFeedPosts(category, activeAuthorType, currentUserId, isPhotographer, 30);
        List<FeedCreatorDto> featuredCreators = feedService.getFeaturedCreators();

        model.addAttribute("posts", posts);
        model.addAttribute("featuredCreators", featuredCreators);
        model.addAttribute("categories", PortfolioCategory.values());
        model.addAttribute("activeCategory", category != null && !category.isBlank() ? category.toUpperCase() : "ALL");
        model.addAttribute("activeAuthorType", activeAuthorType);
        model.addAttribute("isPhotographer", isPhotographer);
        model.addAttribute("isCustomer", isCustomer);

        return "feed";
    }
}
