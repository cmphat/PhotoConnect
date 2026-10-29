package com.photoconnect.repository;

import com.photoconnect.entity.CustomerBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerBankAccountRepository extends JpaRepository<CustomerBankAccount, Long> {

    List<CustomerBankAccount> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);

    Optional<CustomerBankAccount> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndAccountNumber(Long userId, String accountNumber);

    @Modifying
    @Query("UPDATE CustomerBankAccount b SET b.isDefault = false WHERE b.user.id = :userId")
    void clearDefaultAccountsForUser(@Param("userId") Long userId);
}
