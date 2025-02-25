package com.suleman.capturingbanking.api.response_models;

import com.suleman.capturingbanking.model.NewDeviceRecord;

public class NewDeviceResponse {
    private String message;
    private NewDeviceRecord result;

    public String getMessage() {
        return message;
    }

    public NewDeviceRecord getResult() {
        return result;
    }
}

