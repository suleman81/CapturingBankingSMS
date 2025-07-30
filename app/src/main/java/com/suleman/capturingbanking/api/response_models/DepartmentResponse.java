package com.suleman.capturingbanking.api.response_models;

import com.google.gson.annotations.SerializedName;
import com.suleman.capturingbanking.model.Department;

import java.util.List;

public class DepartmentResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("result")
    private List<Department> result;

    public String getMessage() {
        return message;
    }

    public List<Department> getResult() {
        return result;
    }
}

