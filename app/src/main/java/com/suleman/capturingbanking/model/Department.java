package com.suleman.capturingbanking.model;

import com.google.gson.annotations.SerializedName;

public class Department {
    @SerializedName("_id")
    private String id = "";

    @SerializedName("name")
    private String name = "";

    @SerializedName("status")
    private int status = -1;

    @SerializedName("createdAt")
    private String createdAt = "";

    @SerializedName("updatedAt")
    private String updatedAt = "";

    public Department() {
        setDefaultData();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDefaultData() {
        this.id = "";
        this.name = "";
        this.status = -1;
        this.createdAt = "";
        this.updatedAt = "";
    }

}
