package com.photoconnect.repository;

import com.photoconnect.entity.User;
import com.photoconnect.entity.UserRole;
import com.photoconnect.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);

    long countByRole(UserRole role);
    long countByRoleAndStatus(UserRole role, UserStatus status);

    @Query("""
            SELECT YEAR(u.createdAt), MONTH(u.createdAt), u.role, COUNT(u)
            FROM User u
            WHERE u.createdAt >= :startAt AND u.createdAt < :endAt
            GROUP BY YEAR(u.createdAt), MONTH(u.createdAt), u.role
            ORDER BY YEAR(u.createdAt), MONTH(u.createdAt)
            """)
    List<Object[]> countRegistrationsByMonthAndRole(@Param("startAt") LocalDateTime startAt,
                                                     @Param("endAt") LocalDateTime endAt);

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
