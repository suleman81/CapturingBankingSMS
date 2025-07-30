package com.suleman.capturingbanking.model;

import com.google.gson.annotations.SerializedName;

public class TransactionResult {
    @SerializedName("description")
    private String description;

    @SerializedName("channel")
    private String channel;

    @SerializedName("device")
    private String device;

    @SerializedName("bank_name")
    private String bankName;

    @SerializedName("account_title")
    private String accountTitle;

    @SerializedName("account_number")
    private String accountNumber;

    @SerializedName("department")
    private String department;

    @SerializedName("account")
    private String account;

    @SerializedName("available")
    private String availableBalance;

    @SerializedName("txn_type")
    private String txnType;

    @SerializedName("type")
    private String type;

    @SerializedName("amount")
    private String amount;

    @SerializedName("refNo")
    private String refNo;

    @SerializedName("transaction_type")
    private String transactionType;

    @SerializedName("tr_created_date")
    private String transactionCreatedDate;

    @SerializedName("createdAt")
    private String createdAt;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getAccountTitle() {
        return accountTitle;
    }

    public void setAccountTitle(String accountTitle) {
        this.accountTitle = accountTitle;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(String availableBalance) {
        this.availableBalance = availableBalance;
    }

    public String getTxnType() {
        return txnType;
    }

    public void setTxnType(String txnType) {
        this.txnType = txnType;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getTransactionCreatedDate() {
        return transactionCreatedDate;
    }

    public void setTransactionCreatedDate(String transactionCreatedDate) {
        this.transactionCreatedDate = transactionCreatedDate;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "TransactionResult{" +
                "description='" + description + '\'' +
                ", channel='" + channel + '\'' +
                ", device='" + device + '\'' +
                ", bankName='" + bankName + '\'' +
                ", accountTitle='" + accountTitle + '\'' +
                ", accountNumber='" + accountNumber + '\'' +
                ", department='" + department + '\'' +
                ", account='" + account + '\'' +
                ", availableBalance='" + availableBalance + '\'' +
                ", txnType='" + txnType + '\'' +
                ", type='" + type + '\'' +
                ", amount='" + amount + '\'' +
                ", refNo='" + refNo + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", transactionCreatedDate='" + transactionCreatedDate + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}

