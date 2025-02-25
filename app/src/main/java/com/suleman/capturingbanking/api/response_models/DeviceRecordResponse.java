package com.suleman.capturingbanking.api.response_models;

import com.suleman.capturingbanking.model.DeviceRecord;

public class DeviceRecordResponse {
    private String message;
    private DeviceRecord result;

    public String getMessage() {
        return message;
    }

    public DeviceRecord getResult() {
        return result;
    }
}

