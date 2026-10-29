package com.photoconnect.service;

import com.photoconnect.entity.CustomerPost;
import com.photoconnect.entity.PortfolioCategory;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface CustomerPostService {
    CustomerPost createPost(Long customerUserId, String caption, PortfolioCategory category,
                            BigDecimal budget, String location, MultipartFile imageFile, String imageUrl);

    void deletePost(Long customerUserId, Long postId);

    List<CustomerPost> getPostsByCustomer(Long customerUserId);
}
