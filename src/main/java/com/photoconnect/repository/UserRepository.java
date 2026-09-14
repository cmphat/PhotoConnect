package com.photoconnect.repository;

import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    long countByRole(UserRole role);

    @Query("""
            SELECT u FROM User u
            WHERE (:search IS NULL OR :search = ''
                   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:role IS NULL OR u.role = :role)
            AND (:status IS NULL OR u.status = :status)
            ORDER BY u.createdAt DESC
            """)
    List<User> searchUsers(@Param("search") String search,
                           @Param("role") UserRole role,
                           @Param("status") UserStatus status);
}
