package com.photoconnect.service;

import com.photoconnect.dto.FeedCreatorDto;
import com.photoconnect.dto.FeedPostDto;
import com.photoconnect.entity.PhotographerProfile;
import com.photoconnect.entity.PhotographerVerificationStatus;
import com.photoconnect.entity.PortfolioCategory;
import com.photoconnect.entity.PortfolioImage;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.PortfolioImageRepository;
import com.photoconnect.repository.SavedPhotographerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FeedServiceImpl implements FeedService {

    private final PortfolioImageRepository portfolioImageRepository;
    private final PhotographerProfileRepository photographerProfileRepository;
    private final SavedPhotographerRepository savedPhotographerRepository;

    public FeedServiceImpl(PortfolioImageRepository portfolioImageRepository,
                           PhotographerProfileRepository photographerProfileRepository,
                           SavedPhotographerRepository savedPhotographerRepository) {
        this.portfolioImageRepository = portfolioImageRepository;
        this.photographerProfileRepository = photographerProfileRepository;
        this.savedPhotographerRepository = savedPhotographerRepository;
    }

    private static final String[] CREATOR_AVATARS = {
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=240&h=240&q=80",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=240&h=240&q=80",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=240&h=240&q=80",
        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=240&h=240&q=80",
        "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=240&h=240&q=80",
        "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=240&h=240&q=80",
        "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=240&h=240&q=80",
        "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=240&h=240&q=80"
    };

    private String getPhotographerAvatar(Long id) {
        if (id == null) return CREATOR_AVATARS[0];
        return CREATOR_AVATARS[(int) (Math.abs(id) % CREATOR_AVATARS.length)];
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedPostDto> getFeedPosts(String category, Long currentUserId, int limit) {
        PortfolioCategory filterCategory = null;
        if (category != null && !category.isBlank() && !"ALL".equalsIgnoreCase(category)) {
            try {
                filterCategory = PortfolioCategory.fromFormValue(category);
            } catch (Exception ignored) {
                // Ignore unrecognized categories
            }
        }

        int fetchLimit = Math.max(1, Math.min(limit, 50));
        List<PortfolioImage> images = portfolioImageRepository.findFeedImages(filterCategory, PageRequest.of(0, fetchLimit));

        Set<Long> savedProfileIds = new HashSet<>();
        if (currentUserId != null && savedPhotographerRepository != null) {
            List<Long> ids = savedPhotographerRepository.findSavedPhotographerProfileIdsByCustomerId(currentUserId);
            if (ids != null) {
                savedProfileIds.addAll(ids);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        List<FeedPostDto> posts = new ArrayList<>();

        for (PortfolioImage img : images) {
            PhotographerProfile profile = img.getPhotographerProfile();
            if (profile == null) continue;

            PortfolioCategory cat = img.getCategory();
            String catName = cat != null ? cat.name() : null;
            String catDisplayName = cat != null ? cat.getDisplayName() : "Nhiếp ảnh";

            String timeAgo = formatTimeAgo(img.getCreatedAt(), now);
            int seedLikes = (int) ((img.getId() * 37) % 210) + 24;
            int seedComments = (int) ((img.getId() * 11) % 18) + 3;
            boolean isSaved = savedProfileIds.contains(profile.getId());
            String avatarUrl = getPhotographerAvatar(profile.getId());

            posts.add(new FeedPostDto(
                    img.getId(),
                    img.getImageUrl(),
                    img.getCaption() != null && !img.getCaption().isBlank() ? img.getCaption() : "Tác phẩm nhiếp ảnh nghệ thuật bắt trọn khoảnh khắc chân thực.",
                    catName,
                    catDisplayName,
                    img.getCreatedAt(),
                    timeAgo,
                    profile.getId(),
                    profile.getDisplayName(),
                    avatarUrl,
                    profile.getCity() != null ? profile.getCity() : "Toàn quốc",
                    profile.getPriceFrom(),
                    profile.getAverageRating(),
                    profile.getReviewCount() != null ? profile.getReviewCount() : 0,
                    seedLikes,
                    isSaved,
                    seedComments
            ));
        }

        return posts;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedCreatorDto> getFeaturedCreators() {
        List<PhotographerProfile> approved = photographerProfileRepository
                .findByVerificationStatusWithUser(PhotographerVerificationStatus.APPROVED);

        List<FeedCreatorDto> creators = new ArrayList<>();
        int count = 0;
        for (PhotographerProfile p : approved) {
            if (count++ >= 12) break;

            String avatarUrl = getPhotographerAvatar(p.getId());

            creators.add(new FeedCreatorDto(
                    p.getId(),
                    p.getDisplayName(),
                    avatarUrl,
                    p.getCity(),
                    p.getAverageRating()
            ));
        }
        return creators;
    }

    private String formatTimeAgo(LocalDateTime past, LocalDateTime now) {
        if (past == null) return "Just now";
        Duration d = Duration.between(past, now);
        long minutes = Math.abs(d.toMinutes());
        if (minutes < 5) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        long hours = d.toHours();
        if (hours < 24) return hours + "h ago";
        long days = d.toDays();
        if (days == 1) return "Yesterday";
        if (days < 30) return days + "d ago";
        return (days / 30) + "mo ago";
    }
}
