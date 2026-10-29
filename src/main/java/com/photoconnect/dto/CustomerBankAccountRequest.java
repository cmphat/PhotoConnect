package com.photoconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CustomerBankAccountRequest {

    @NotBlank(message = "Vui lòng chọn hoặc nhập tên ngân hàng.")
    @Size(max = 100, message = "Tên ngân hàng không được quá 100 ký tự.")
    private String bankName;

    @Size(max = 50, message = "Mã ngân hàng không quá 50 ký tự.")
    private String bankCode;

    @Size(max = 150, message = "Chi nhánh ngân hàng không quá 150 ký tự.")
    private String branchName;

    @NotBlank(message = "Vui lòng nhập số tài khoản ngân hàng.")
    @Size(min = 6, max = 50, message = "Số tài khoản từ 6 đến 50 ký tự.")
    private String accountNumber;

    @NotBlank(message = "Vui lòng nhập tên chủ tài khoản.")
    @Size(max = 150, message = "Tên chủ tài khoản không quá 150 ký tự.")
    private String accountHolderName;

    private boolean makeDefault;

    public CustomerBankAccountRequest() {}

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }

    public String getBranchName() { return branchName; }
    public void setBranchName(String branchName) { this.branchName = branchName; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

    public boolean isMakeDefault() { return makeDefault; }
    public void setMakeDefault(boolean makeDefault) { this.makeDefault = makeDefault; }
}
