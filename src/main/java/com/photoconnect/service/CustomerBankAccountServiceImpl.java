package com.photoconnect.service;

import com.photoconnect.dto.CustomerBankAccountDto;
import com.photoconnect.dto.CustomerBankAccountRequest;
import com.photoconnect.entity.CustomerBankAccount;
import com.photoconnect.entity.User;
import com.photoconnect.exception.ResourceNotFoundException;
import com.photoconnect.repository.CustomerBankAccountRepository;
import com.photoconnect.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerBankAccountServiceImpl implements CustomerBankAccountService {

    private final CustomerBankAccountRepository bankAccountRepository;
    private final UserRepository userRepository;

    public CustomerBankAccountServiceImpl(CustomerBankAccountRepository bankAccountRepository,
                                         UserRepository userRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerBankAccountDto> listAccounts(Long userId) {
        return bankAccountRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream()
                .map(CustomerBankAccountDto::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerBankAccountDto getAccount(Long userId, Long accountId) {
        CustomerBankAccount account = findOwnedAccount(userId, accountId);
        return CustomerBankAccountDto.fromEntity(account);
    }

    @Override
    @Transactional
    public CustomerBankAccountDto createAccount(Long userId, CustomerBankAccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        List<CustomerBankAccount> existing = bankAccountRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
        boolean shouldBeDefault = request.isMakeDefault() || existing.isEmpty();

        if (shouldBeDefault) {
            bankAccountRepository.clearDefaultAccountsForUser(userId);
        }

        CustomerBankAccount account = new CustomerBankAccount();
        account.setUser(user);
        account.setBankName(request.getBankName().trim());
        account.setBankCode(request.getBankCode() != null ? request.getBankCode().trim() : null);
        account.setBranchName(request.getBranchName() != null ? request.getBranchName().trim() : null);
        account.setAccountNumber(request.getAccountNumber().trim());
        account.setAccountHolderName(request.getAccountHolderName().trim().toUpperCase());
        account.setDefault(shouldBeDefault);

        CustomerBankAccount saved = bankAccountRepository.save(account);
        return CustomerBankAccountDto.fromEntity(saved);
    }

    @Override
    @Transactional
    public CustomerBankAccountDto updateAccount(Long userId, Long accountId, CustomerBankAccountRequest request) {
        CustomerBankAccount account = findOwnedAccount(userId, accountId);

        if (request.isMakeDefault() && !account.isDefault()) {
            bankAccountRepository.clearDefaultAccountsForUser(userId);
            account.setDefault(true);
        }

        account.setBankName(request.getBankName().trim());
        if (request.getBankCode() != null) account.setBankCode(request.getBankCode().trim());
        if (request.getBranchName() != null) account.setBranchName(request.getBranchName().trim());
        account.setAccountNumber(request.getAccountNumber().trim());
        account.setAccountHolderName(request.getAccountHolderName().trim().toUpperCase());

        CustomerBankAccount updated = bankAccountRepository.save(account);
        return CustomerBankAccountDto.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteAccount(Long userId, Long accountId) {
        CustomerBankAccount account = findOwnedAccount(userId, accountId);
        boolean wasDefault = account.isDefault();
        bankAccountRepository.delete(account);

        // If the default account was deleted, promote the oldest remaining account to default
        if (wasDefault) {
            List<CustomerBankAccount> remaining = bankAccountRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
            if (!remaining.isEmpty()) {
                CustomerBankAccount newDefault = remaining.get(0);
                newDefault.setDefault(true);
                bankAccountRepository.save(newDefault);
            }
        }
    }

    @Override
    @Transactional
    public void setDefaultAccount(Long userId, Long accountId) {
        CustomerBankAccount account = findOwnedAccount(userId, accountId);
        bankAccountRepository.clearDefaultAccountsForUser(userId);
        account.setDefault(true);
        bankAccountRepository.save(account);
    }

    private CustomerBankAccount findOwnedAccount(Long userId, Long accountId) {
        return bankAccountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found or access denied."));
    }
}
