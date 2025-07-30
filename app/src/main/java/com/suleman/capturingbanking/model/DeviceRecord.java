package com.suleman.capturingbanking.model;

public class DeviceRecord {
    private String _id;
    private String name;
    private String bank_name;
    private String account_title;
    private String account_number;
    private Department department;
    private int status;
    private String createdAt;
    private String updatedAt;

    public DeviceRecord() {
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

    public Department getDepartment() {
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
        this.department = new Department();
        this.status = -1;
        this.createdAt = "";
        this.updatedAt = "";
    }

    // Function to check if data is empty
    public boolean isEmpty() {
        return _id == null || _id.isEmpty();
    }

}

