package com.suleman.capturingbanking.api.response_models;

public class CreateDeviceRequest {
    private String device_id;
    private String device;
    private String bank_name;
    private String account_title;
    private String account_number;
    private String department;

    public CreateDeviceRequest(String device_id, String device, String bank_name, String account_title, String account_number, String department) {
        this.device_id = device_id;
        this.device = device;
        this.bank_name = bank_name;
        this.account_title = account_title;
        this.account_number = account_number;
        this.department = department;
    }

    // Getters and setters


    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getBank_name() {
        return bank_name;
    }

    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }

    public String getAccount_title() {
        return account_title;
    }

    public void setAccount_title(String account_title) {
        this.account_title = account_title;
    }

    public String getAccount_number() {
        return account_number;
    }

    public void setAccount_number(String account_number) {
        this.account_number = account_number;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}

