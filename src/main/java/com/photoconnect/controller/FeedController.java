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
                           HttpSession session,
                           Model model) {
        Long currentUserId = null;
        Object sessionUid = session.getAttribute("userId");
        if (sessionUid instanceof Number num) {
            currentUserId = num.longValue();
        }

        List<FeedPostDto> posts = feedService.getFeedPosts(category, currentUserId, 30);
        List<FeedCreatorDto> featuredCreators = feedService.getFeaturedCreators();

        model.addAttribute("posts", posts);
        model.addAttribute("featuredCreators", featuredCreators);
        model.addAttribute("categories", PortfolioCategory.values());
        model.addAttribute("activeCategory", category != null && !category.isBlank() ? category.toUpperCase() : "ALL");

        return "feed";
    }
}
