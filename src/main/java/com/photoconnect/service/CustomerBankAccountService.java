package com.photoconnect.service;

import com.photoconnect.dto.CustomerBankAccountDto;
import com.photoconnect.dto.CustomerBankAccountRequest;

import java.util.List;

public interface CustomerBankAccountService {

    List<CustomerBankAccountDto> listAccounts(Long userId);

    CustomerBankAccountDto getAccount(Long userId, Long accountId);

    CustomerBankAccountDto createAccount(Long userId, CustomerBankAccountRequest request);

    CustomerBankAccountDto updateAccount(Long userId, Long accountId, CustomerBankAccountRequest request);

    void deleteAccount(Long userId, Long accountId);

    void setDefaultAccount(Long userId, Long accountId);
}
