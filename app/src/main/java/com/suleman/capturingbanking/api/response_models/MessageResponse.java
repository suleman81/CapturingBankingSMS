package com.suleman.capturingbanking.api.response_models;

import com.google.gson.annotations.SerializedName;
import com.suleman.capturingbanking.model.TransactionResult;

public class MessageResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("result")
    private TransactionResult result;

    public String getMessage() {
        return message;
    }

    public TransactionResult getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "MessageResponse{" +
                "message='" + message + '\'' +
                ", result=" + result +
                '}';
    }
}
