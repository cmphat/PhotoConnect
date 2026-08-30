package com.photoconnect.controller;

import com.photoconnect.dto.PhotographerPublicDto;
import com.photoconnect.service.PublicPhotographerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final PublicPhotographerService publicPhotographerService;

    @Autowired
    public HomeController(PublicPhotographerService publicPhotographerService) {
        this.publicPhotographerService = publicPhotographerService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<PhotographerPublicDto> allApproved = publicPhotographerService.listApprovedPhotographers();
        List<PhotographerPublicDto> featured = allApproved.stream().limit(3).collect(Collectors.toList());
        model.addAttribute("featuredPhotographers", featured);
        return "index";
    }
}
