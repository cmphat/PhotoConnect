package com.photoconnect.service;

import com.photoconnect.entity.CustomerPost;
import com.photoconnect.entity.PortfolioCategory;
import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.repository.CustomerPostRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerPostServiceTest {

    @Mock
    private CustomerPostRepository customerPostRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CloudinaryStorageService cloudinaryStorageService;

    private CustomerPostService customerPostService;

    @BeforeEach
    void setUp() {
        customerPostService = new CustomerPostServiceImpl(
                customerPostRepository,
                userRepository,
                cloudinaryStorageService
        );
    }

    @Test
    @DisplayName("createPost creates and persists post for customer")
    void createPost_success() {
        User customer = new User("cust@example.com", "pass", "Nguyen Van A", "0900000000", UserRole.CUSTOMER, UserStatus.ACTIVE);
        customer.setId(10L);
        customer.setLocation("Hà Nội");

        when(userRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(customerPostRepository.save(any(CustomerPost.class))).thenAnswer(inv -> {
            CustomerPost p = inv.getArgument(0);
            p.setId(100L);
            return p;
        });

        CustomerPost result = customerPostService.createPost(
                10L,
                "Cần tìm photographer chụp kỷ yếu nhóm 10 người",
                PortfolioCategory.PORTRAIT,
                new BigDecimal("2000000"),
                "Hà Nội",
                null,
                "https://example.com/sample.jpg"
        );

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getCaption()).isEqualTo("Cần tìm photographer chụp kỷ yếu nhóm 10 người");
        assertThat(result.getCategory()).isEqualTo(PortfolioCategory.PORTRAIT);
        assertThat(result.getBudget()).isEqualByComparingTo("2000000");

        ArgumentCaptor<CustomerPost> captor = ArgumentCaptor.forClass(CustomerPost.class);
        verify(customerPostRepository).save(captor.capture());
        assertThat(captor.getValue().getCustomer()).isEqualTo(customer);
    }

    @Test
    @DisplayName("createPost throws when user is not CUSTOMER role")
    void createPost_failsWhenNotCustomer() {
        User photographer = new User("photo@example.com", "pass", "Tran Photographer", "0900000001", UserRole.PHOTOGRAPHER, UserStatus.ACTIVE);
        photographer.setId(20L);

        when(userRepository.findById(20L)).thenReturn(Optional.of(photographer));

        assertThatThrownBy(() -> customerPostService.createPost(
                20L,
                "Looking for clients",
                PortfolioCategory.WEDDING,
                null,
                null,
                null,
                null
        )).isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("Only customer accounts");
    }

    @Test
    @DisplayName("deletePost allows author to delete their own post")
    void deletePost_authorCanDelete() {
        User customer = new User("cust@example.com", "pass", "Nguyen Van A", "0900000000", UserRole.CUSTOMER, UserStatus.ACTIVE);
        customer.setId(10L);

        CustomerPost post = new CustomerPost();
        post.setId(50L);
        post.setCustomer(customer);

        when(customerPostRepository.findById(50L)).thenReturn(Optional.of(post));

        customerPostService.deletePost(10L, 50L);

        verify(customerPostRepository).delete(post);
    }
}
