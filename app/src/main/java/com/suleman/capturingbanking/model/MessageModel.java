package com.suleman.capturingbanking.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Entity(tableName = "message")
public class MessageModel {
    @PrimaryKey(autoGenerate = true)
    private int id = 0;
    @ColumnInfo(name = "channel")
    private String channel;
    @ColumnInfo(name = "sms")
    private String sms;
    @ColumnInfo(name = "bank_name")
    private String bank_name;
    @ColumnInfo(name = "account_title")
    private String account_title;
    @ColumnInfo(name = "account_number")
    private String account_number;
    @ColumnInfo(name = "department")
    private String department;
    @ColumnInfo(name = "device")
    private String device;

    @ColumnInfo(name = "timestamp")
    private long timestamp;

    public MessageModel(String channel, String sms, String bank_name, String account_title,
                        String account_number, String department, String device, long timestamp) {
        this.channel = channel;
        this.sms = sms;
        this.bank_name = bank_name;
        this.account_title = account_title;
        this.account_number = account_number;
        this.department = department;
        this.device = device;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getSms() {
        return sms;
    }

    public void setSms(String sms) {
        this.sms = sms;
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

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MessageModel that = (MessageModel) obj;
        return channel.equals(that.channel) && sms.equals(that.sms);
    }

    public String toJson() {
        Gson gson = new Gson();
        JsonObject jsonObject = gson.toJsonTree(this).getAsJsonObject();
        jsonObject.remove("id");
        jsonObject.remove("timestamp");
        return gson.toJson(jsonObject);
    }

}
