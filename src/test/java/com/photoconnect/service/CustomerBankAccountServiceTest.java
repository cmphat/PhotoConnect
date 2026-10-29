package com.photoconnect.service;

import com.photoconnect.dto.CustomerBankAccountDto;
import com.photoconnect.dto.CustomerBankAccountRequest;
import com.photoconnect.entity.CustomerBankAccount;
import com.photoconnect.entity.User;
import com.photoconnect.exception.ResourceNotFoundException;
import com.photoconnect.repository.CustomerBankAccountRepository;
import com.photoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Customer Bank Account Management Unit Tests")
class CustomerBankAccountServiceTest {

    @Mock
    private CustomerBankAccountRepository bankAccountRepository;

    @Mock
    private UserRepository userRepository;

    private CustomerBankAccountService bankAccountService;

    private User customer;

    @BeforeEach
    void setUp() {
        bankAccountService = new CustomerBankAccountServiceImpl(bankAccountRepository, userRepository);
        customer = new User();
        customer.setId(100L);
        customer.setEmail("customer@photoconnect.vn");
    }

    @Test
    @DisplayName("Creating first bank account automatically sets isDefault to true")
    void createAccount_firstAccount_setsDefault() {
        CustomerBankAccountRequest request = new CustomerBankAccountRequest();
        request.setBankName("Vietcombank");
        request.setAccountNumber("0123456789");
        request.setAccountHolderName("NGUYEN VAN A");

        when(userRepository.findById(100L)).thenReturn(Optional.of(customer));
        when(bankAccountRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(100L)).thenReturn(Collections.emptyList());
        when(bankAccountRepository.save(any(CustomerBankAccount.class))).thenAnswer(invocation -> {
            CustomerBankAccount saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        CustomerBankAccountDto result = bankAccountService.createAccount(100L, request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.isDefault()).isTrue();
        assertThat(result.getMaskedAccountNumber()).isEqualTo("•••• •••• 6789");
        assertThat(result.getAccountHolderName()).isEqualTo("NGUYEN VAN A");
    }

    @Test
    @DisplayName("Setting account as default clears previous defaults for the user")
    void setDefaultAccount_clearsPreviousDefaults() {
        CustomerBankAccount account = new CustomerBankAccount();
        account.setId(2L);
        account.setUser(customer);
        account.setAccountNumber("9876543210");
        account.setDefault(false);

        when(bankAccountRepository.findByIdAndUserId(2L, 100L)).thenReturn(Optional.of(account));

        bankAccountService.setDefaultAccount(100L, 2L);

        verify(bankAccountRepository).clearDefaultAccountsForUser(100L);
        assertThat(account.isDefault()).isTrue();
        verify(bankAccountRepository).save(account);
    }

    @Test
    @DisplayName("User cannot access, modify, or delete another user's bank account")
    void crossUserSecurity_throwsException() {
        when(bankAccountRepository.findByIdAndUserId(999L, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bankAccountService.deleteAccount(100L, 999L))
                .isInstanceOf(com.photoconnect.exception.ResourceNotFoundException.class)
                .hasMessageContaining("access denied");
    }

    @Test
    @DisplayName("Masking utility properly hides all but the last 4 digits")
    void masking_hidesSensitiveDigits() {
        CustomerBankAccount acc = new CustomerBankAccount();
        acc.setAccountNumber("19034567890123");
        assertThat(acc.getMaskedAccountNumber()).isEqualTo("•••• •••• 0123");

        acc.setAccountNumber("1234");
        assertThat(acc.getMaskedAccountNumber()).isEqualTo("1234");
    }
}
