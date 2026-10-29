package com.photoconnect.repository;

import com.photoconnect.entity.CustomerPost;
import com.photoconnect.entity.PortfolioCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerPostRepository extends JpaRepository<CustomerPost, Long> {

    @Query("SELECT p FROM CustomerPost p JOIN FETCH p.customer WHERE (:category IS NULL OR p.category = :category) ORDER BY p.createdAt DESC")
    List<CustomerPost> findFeedCustomerPosts(@Param("category") PortfolioCategory category, Pageable pageable);

    @Query("SELECT p FROM CustomerPost p JOIN FETCH p.customer WHERE p.customer.id = :customerId ORDER BY p.createdAt DESC")
    List<CustomerPost> findByCustomerId(@Param("customerId") Long customerId);
}
