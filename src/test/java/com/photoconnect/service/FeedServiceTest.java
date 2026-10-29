package com.photoconnect.service;

import com.photoconnect.dto.FeedPostDto;
import com.photoconnect.entity.*;
import com.photoconnect.repository.CustomerPostRepository;
import com.photoconnect.repository.PhotographerProfileRepository;
import com.photoconnect.repository.PortfolioImageRepository;
import com.photoconnect.repository.SavedPhotographerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeedServiceTest {

    @Mock
    private PortfolioImageRepository portfolioImageRepository;

    @Mock
    private PhotographerProfileRepository photographerProfileRepository;

    @Mock
    private SavedPhotographerRepository savedPhotographerRepository;

    @Mock
    private CustomerPostRepository customerPostRepository;

    private FeedService feedService;

    private PortfolioImage samplePhotoImage;
    private CustomerPost sampleCustomerPost;

    @BeforeEach
    void setUp() {
        feedService = new FeedServiceImpl(
                portfolioImageRepository,
                photographerProfileRepository,
                savedPhotographerRepository,
                customerPostRepository
        );

        User photoUser = new User("photographer@example.com", "pass", "Chau Minh Phat", "0901234567", UserRole.PHOTOGRAPHER, UserStatus.ACTIVE);
        photoUser.setId(101L);

        PhotographerProfile profile = new PhotographerProfile();
        profile.setId(55L);
        profile.setUser(photoUser);
        profile.setDisplayName("Chau Minh Phat");
        profile.setCity("Ho Chi Minh City");
        profile.setPriceFrom(new BigDecimal("1500000"));
        profile.setAverageRating(4.9);
        profile.setReviewCount(12);

        samplePhotoImage = new PortfolioImage();
        samplePhotoImage.setId(1L);
        samplePhotoImage.setPhotographerProfile(profile);
        samplePhotoImage.setImageUrl("https://images.unsplash.com/photo-1.jpg");
        samplePhotoImage.setCaption("Wedding in Da Lat");
        samplePhotoImage.setCategory(PortfolioCategory.WEDDING);
        samplePhotoImage.setCreatedAt(LocalDateTime.now().minusHours(2));

        User custUser = new User("cust@example.com", "pass", "Nguyen Van Customer", "0909999999", UserRole.CUSTOMER, UserStatus.ACTIVE);
        custUser.setId(202L);
        custUser.setLocation("Ha Noi");

        sampleCustomerPost = new CustomerPost();
        sampleCustomerPost.setId(10L);
        sampleCustomerPost.setCustomer(custUser);
        sampleCustomerPost.setCaption("Can tim tho chup phong su cuoi");
        sampleCustomerPost.setCategory(PortfolioCategory.WEDDING);
        sampleCustomerPost.setBudget(new BigDecimal("3000000"));
        sampleCustomerPost.setLocation("Ha Noi");
        sampleCustomerPost.setCreatedAt(LocalDateTime.now().minusHours(1));
    }

    @Test
    @DisplayName("When viewer is Customer (isPhotographer=false) and authorType=ALL, both photographer and customer posts are returned")
    void getFeedPosts_customerViewer_canViewAllPosts() {
        when(portfolioImageRepository.findFeedImages(isNull(), any(Pageable.class)))
                .thenReturn(List.of(samplePhotoImage));
        when(customerPostRepository.findFeedCustomerPosts(isNull(), any(Pageable.class)))
                .thenReturn(List.of(sampleCustomerPost));

        List<FeedPostDto> posts = feedService.getFeedPosts("ALL", "ALL", 202L, false, 30);

        assertThat(posts).hasSize(2);
        // sampleCustomerPost (1h ago) is newer than samplePhotoImage (2h ago)
        assertThat(posts.get(0).getAuthorType()).isEqualTo("CUSTOMER");
        assertThat(posts.get(0).getCaption()).isEqualTo("Can tim tho chup phong su cuoi");
        assertThat(posts.get(1).getAuthorType()).isEqualTo("PHOTOGRAPHER");
        assertThat(posts.get(1).getPhotographerName()).isEqualTo("Chau Minh Phat");
    }

    @Test
    @DisplayName("When viewer is Photographer and authorType=ALL, both photographer and customer posts are returned")
    void getFeedPosts_photographerViewer_allAuthorTypes() {
        when(portfolioImageRepository.findFeedImages(isNull(), any(Pageable.class)))
                .thenReturn(List.of(samplePhotoImage));
        when(customerPostRepository.findFeedCustomerPosts(isNull(), any(Pageable.class)))
                .thenReturn(List.of(sampleCustomerPost));

        List<FeedPostDto> posts = feedService.getFeedPosts("ALL", "ALL", 101L, true, 30);

        assertThat(posts).hasSize(2);
        // sampleCustomerPost (1h ago) is newer than samplePhotoImage (2h ago)
        assertThat(posts.get(0).getAuthorType()).isEqualTo("CUSTOMER");
        assertThat(posts.get(0).getCaption()).isEqualTo("Can tim tho chup phong su cuoi");
        assertThat(posts.get(1).getAuthorType()).isEqualTo("PHOTOGRAPHER");
        assertThat(posts.get(1).getPhotographerName()).isEqualTo("Chau Minh Phat");
    }

    @Test
    @DisplayName("When viewer is Photographer and authorType=CUSTOMER, only customer inquiry posts are returned")
    void getFeedPosts_photographerViewer_onlyCustomerPosts() {
        when(customerPostRepository.findFeedCustomerPosts(isNull(), any(Pageable.class)))
                .thenReturn(List.of(sampleCustomerPost));

        List<FeedPostDto> posts = feedService.getFeedPosts("ALL", "CUSTOMER", 101L, true, 30);

        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getAuthorType()).isEqualTo("CUSTOMER");
        assertThat(posts.get(0).isCustomerPost()).isTrue();
        assertThat(posts.get(0).getBudget()).isEqualByComparingTo("3000000");

        // Verify portfolio image repository is NOT queried
        verifyNoInteractions(portfolioImageRepository);
    }

    @Test
    @DisplayName("When viewer is Photographer and authorType=PHOTOGRAPHER, only photographer posts are returned")
    void getFeedPosts_photographerViewer_onlyPhotographerPosts() {
        when(portfolioImageRepository.findFeedImages(isNull(), any(Pageable.class)))
                .thenReturn(List.of(samplePhotoImage));

        List<FeedPostDto> posts = feedService.getFeedPosts("ALL", "PHOTOGRAPHER", 101L, true, 30);

        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getAuthorType()).isEqualTo("PHOTOGRAPHER");
        assertThat(posts.get(0).isPhotographerPost()).isTrue();

        // Verify customer repository is NOT queried
        verifyNoInteractions(customerPostRepository);
    }
}
