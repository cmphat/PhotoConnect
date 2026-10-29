package com.photoconnect.dto;

import com.photoconnect.entity.CustomerBankAccount;

import java.time.LocalDateTime;

public class CustomerBankAccountDto {

    private Long id;
    private String bankName;
    private String bankCode;
    private String branchName;
    private String maskedAccountNumber;
    private String accountHolderName;
    private boolean isDefault;
    private LocalDateTime createdAt;

    public CustomerBankAccountDto() {}

    public static CustomerBankAccountDto fromEntity(CustomerBankAccount entity) {
        if (entity == null) return null;
        CustomerBankAccountDto dto = new CustomerBankAccountDto();
        dto.setId(entity.getId());
        dto.setBankName(entity.getBankName());
        dto.setBankCode(entity.getBankCode());
        dto.setBranchName(entity.getBranchName());
        dto.setMaskedAccountNumber(entity.getMaskedAccountNumber());
        dto.setAccountHolderName(entity.getAccountHolderName());
        dto.setDefault(entity.isDefault());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getMaskedAccountNumber() { return maskedAccountNumber; }
    public void setMaskedAccountNumber(String maskedAccountNumber) { this.maskedAccountNumber = maskedAccountNumber; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
