package com.suleman.capturingbanking.model;

public class NewDeviceRecord {
    private String _id;
    private String name;
    private String bank_name;
    private String account_title;
    private String account_number;
    private String department;
    private int status;
    private String createdAt;
    private String updatedAt;

    public NewDeviceRecord() {
        setDefaultData();
    }

    public String getId() {
        return _id;
    }

    public String getName() {
        return name;
    }

    public String getBankName() {
        return bank_name;
    }

    public String getAccountTitle() {
        return account_title;
    }

    public String getAccountNumber() {
        return account_number;
    }

    public String getDepartment() {
        return department;
    }

    public int getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setDefaultData() {
        this._id = "";
        this.name = "";
        this.bank_name = "";
        this.account_title = "";
        this.account_number = "";
        this.department = "";
        this.status = -1;
        this.createdAt = "";
        this.updatedAt = "";
    }

    // Function to check if data is empty
    public boolean isEmpty() {
        return _id == null || _id.isEmpty();
    }

}

