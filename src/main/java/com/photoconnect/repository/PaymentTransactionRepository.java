package com.photoconnect.repository;

import com.photoconnect.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    boolean existsByProviderNameAndExternalTransactionId(String providerName, String externalTransactionId);

    Optional<PaymentTransaction> findByProviderNameAndExternalTransactionId(String providerName, String externalTransactionId);

    List<PaymentTransaction> findByDepositIdOrderByCreatedAtDesc(Long depositId);

    List<PaymentTransaction> findByPaymentReference(String paymentReference);
}
