package com.suleman.capturingbanking.model;

public class DeviceModel {
    public String device, bankName, accountTitle, accountNumber, department;
    public String device_ID, department_ID;
    public DeviceModel() {
    }

    public void setEmpty() {
        this.device = "";
        this.bankName = "";
        this.accountTitle = "";
        this.accountNumber = "";
        this.department = "";
        this.device_ID = "";
        this.department_ID = "";
    }

    public boolean isAllEmpty() {
        return device.isEmpty() && bankName.isEmpty() && accountTitle.isEmpty() && accountNumber.isEmpty() && department.isEmpty() && device_ID.isEmpty() && department_ID.isEmpty();
    }

}
