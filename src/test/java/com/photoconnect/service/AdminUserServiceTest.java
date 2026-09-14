package com.photoconnect.service;

import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserServiceImpl(userRepository);
    }

    @Test
    void searchUsers_shouldDelegateToRepositoryWithNormalizedSearch() {
        User user = new User("user@example.com", "hash", "John Doe", "0123456789", UserRole.CUSTOMER, UserStatus.ACTIVE);
        when(userRepository.searchUsers("john", UserRole.CUSTOMER, UserStatus.ACTIVE))
                .thenReturn(List.of(user));

        List<User> results = adminUserService.searchUsers("  john  ", UserRole.CUSTOMER, UserStatus.ACTIVE);

        assertEquals(1, results.size());
        assertEquals("John Doe", results.get(0).getFullName());
        verify(userRepository).searchUsers("john", UserRole.CUSTOMER, UserStatus.ACTIVE);
    }

    @Test
    void searchUsers_emptySearch_shouldPassNullKeyword() {
        when(userRepository.searchUsers(null, null, null)).thenReturn(List.of());

        List<User> results = adminUserService.searchUsers("   ", null, null);

        assertNotNull(results);
        verify(userRepository).searchUsers(null, null, null);
    }

    @Test
    void updateUserStatus_shouldUpdateAndSave_whenValid() {
        User user = new User("user@example.com", "hash", "Target User", "0123456789", UserRole.CUSTOMER, UserStatus.ACTIVE);
        user.setId(5L);
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updated = adminUserService.updateUserStatus(5L, UserStatus.BANNED, 99L);

        assertEquals(UserStatus.BANNED, updated.getStatus());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserStatus_shouldThrowIllegalStateException_whenAdminModifiesOwnAccount() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                adminUserService.updateUserStatus(99L, UserStatus.INACTIVE, 99L));

        assertTrue(ex.getMessage().contains("Administrators cannot modify their own account status"));
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserStatus_shouldThrowIllegalArgumentException_whenUserNotFound() {
        when(userRepository.findById(55L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                adminUserService.updateUserStatus(55L, UserStatus.ACTIVE, 99L));

        assertTrue(ex.getMessage().contains("User not found with id: 55"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUserStatus_shouldThrowIllegalArgumentException_whenStatusOrUserIdNull() {
        assertThrows(IllegalArgumentException.class, () ->
                adminUserService.updateUserStatus(null, UserStatus.ACTIVE, 99L));

        assertThrows(IllegalArgumentException.class, () ->
                adminUserService.updateUserStatus(5L, null, 99L));
    }
}
