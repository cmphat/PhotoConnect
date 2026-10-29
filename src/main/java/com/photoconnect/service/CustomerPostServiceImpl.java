package com.photoconnect.service;

import com.photoconnect.entity.CustomerPost;
import com.photoconnect.entity.PortfolioCategory;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.repository.CustomerPostRepository;
import com.photoconnect.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CustomerPostServiceImpl implements CustomerPostService {

    private static final Logger log = LoggerFactory.getLogger(CustomerPostServiceImpl.class);

    private final CustomerPostRepository customerPostRepository;
    private final UserRepository userRepository;
    private final CloudinaryStorageService cloudinaryStorageService;

    public CustomerPostServiceImpl(CustomerPostRepository customerPostRepository,
                                   UserRepository userRepository,
                                   CloudinaryStorageService cloudinaryStorageService) {
        this.customerPostRepository = customerPostRepository;
        this.userRepository = userRepository;
        this.cloudinaryStorageService = cloudinaryStorageService;
    }

    @Override
    @Transactional
    public CustomerPost createPost(Long customerUserId, String caption, PortfolioCategory category,
                                   BigDecimal budget, String location, MultipartFile imageFile, String imageUrl) {
        if (customerUserId == null) {
            throw new IllegalArgumentException("User ID is required to create a post.");
        }
        if (caption == null || caption.trim().isEmpty()) {
            throw new IllegalArgumentException("Post caption cannot be empty.");
        }

        User customer = userRepository.findById(customerUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + customerUserId));

        if (customer.getRole() != UserRole.CUSTOMER) {
            throw new IllegalStateException("Only customer accounts can publish customer shoot inquiries.");
        }

        String finalImageUrl = imageUrl != null ? imageUrl.trim() : null;
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                CloudinaryStorageService.CloudinaryUploadResult result =
                        cloudinaryStorageService.uploadImage(imageFile, "photoconnect/customer_posts");
                if (result != null && result.secureUrl() != null) {
                    finalImageUrl = result.secureUrl();
                }
            } catch (Exception e) {
                log.warn("Cloudinary upload failed for customer post, proceeding with fallback or provided URL: {}", e.getMessage());
            }
        }

        CustomerPost post = new CustomerPost();
        post.setCustomer(customer);
        post.setCaption(caption.trim());
        post.setCategory(category);
        post.setBudget(budget);
        post.setLocation(location != null && !location.isBlank() ? location.trim() : customer.getLocation());
        post.setImageUrl(finalImageUrl);

        CustomerPost saved = customerPostRepository.save(post);
        log.info("Customer [{}] created feed post [{}]", customerUserId, saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void deletePost(Long customerUserId, Long postId) {
        if (customerUserId == null || postId == null) return;
        CustomerPost post = customerPostRepository.findById(postId).orElse(null);
        if (post != null && post.getCustomer() != null && post.getCustomer().getId().equals(customerUserId)) {
            customerPostRepository.delete(post);
            log.info("Customer [{}] deleted feed post [{}]", customerUserId, postId);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerPost> getPostsByCustomer(Long customerUserId) {
        if (customerUserId == null) return List.of();
        return customerPostRepository.findByCustomerId(customerUserId);
    }
}
