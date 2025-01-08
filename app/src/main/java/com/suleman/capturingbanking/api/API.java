package com.suleman.capturingbanking.api;

import static com.suleman.capturingbanking.Utlis.TOKEN;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fxn.stash.Stash;
import com.suleman.capturingbanking.services.VolleySingleton;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class API {
    RequestQueue requestQueue;
    private static API mInstance;

    public API(Context context) {
        requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
    }

    public static synchronized API getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new API(context);
        }
        return mInstance;
    }

    private static final String BASE = "https://www.btocsms.com/api/";
    private static final String STAGE = "https://stage.btocsms.com/api/";
    public static final String UPI_SERVER = "https://upipayment.co/api/message-request";

    public static String getLink(String path) {
        return STAGE + path;
    }

    public static String getRestoreLink(String accountNumber) {
        return STAGE + "devicerecord/" + accountNumber;
    }

    public void authenticateUser(ResponseCallback responseCallback) throws Exception {
        JSONObject json = new JSONObject();
        json.put("email", "adminapp@gmail.com");
        json.put("password", "Stage@445678");
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, getLink("signinApp"), json,
                responseCallback::onSuccess,
                error -> responseCallback.onError(parseError(error, "Failed to authenticate")));
        requestQueue.add(stringRequest);
    }

    public void getDepartmentList(ResponseCallback responseCallback) {
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, getLink("department"), null,
                responseCallback::onSuccess,
                error -> {
                    responseCallback.onError(parseError(error, "Failed to fetch department list"));
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = Stash.getString(TOKEN, "");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        requestQueue.add(objectRequest);
    }

    public void fetchData(String accountNumber, ResponseCallback responseCallback) {
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, getRestoreLink(accountNumber), null,
                responseCallback::onSuccess, error -> {
            responseCallback.onError(parseError(error, "Failed to fetch data"));
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = Stash.getString(TOKEN, "");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        requestQueue.add(objectRequest);
    }

    public void createDevice(JSONObject json, ResponseCallback responseCallback) {
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, getLink("device-create"), json,
                responseCallback::onSuccess, error -> {
            responseCallback.onError(parseError(error, "Failed to create device"));
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = Stash.getString(TOKEN, "");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void updateDevice(JSONObject json, ResponseCallback responseCallback) {
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, API.getLink("device-update"), json,
                responseCallback::onSuccess, error -> {
            responseCallback.onError(parseError(error, "failed to update device"));
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = Stash.getString(TOKEN, "");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        requestQueue.add(stringRequest);
    }

    public static String parseError(VolleyError error, String defaultMessage) {
        try {
            String errorBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
            JSONObject jsonError = new JSONObject(errorBody);
            return jsonError.getString("message");
        } catch (Exception e) {
            e.printStackTrace();
            return defaultMessage;
        }
    }

}
