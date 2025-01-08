package com.suleman.capturingbanking.api;

import org.json.JSONObject;

public interface ResponseCallback {
    void onSuccess(JSONObject response);
    void onError(String response);
}
