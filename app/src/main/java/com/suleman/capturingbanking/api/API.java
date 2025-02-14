package com.suleman.capturingbanking.api;

import static com.suleman.capturingbanking.utilies.Utils.TOKEN;

import android.content.Context;
import android.util.Log;

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
    public static final String UPI_SERVER_STAGING = "https://stage.upipayment.co/api/message-request";
    public static final String DEVICE_RECORD = "devicerecordnew/";
    public static final String DEVICE_RECORD_BY_ID = "devicerecordnewbyid/";
    public static final String Staging_Email2 = "stage@blueirissoft.com";
    public static final String Staging_Password2 = "stage123";
    public static final String Production_Email = "bankingsms@blueirissoft.com";
    public static final String Production_Password = "BankSMS76&60$$";

    public static String getLink(String path) {
        return STAGE + path;
    }

    public static String getRestoreLink(String accountNumber) {
        return STAGE + DEVICE_RECORD + accountNumber;
    }

    public static String getRestoreDeviceLink(String deviceID) {
        return STAGE + DEVICE_RECORD_BY_ID + deviceID;
    }

    public void authenticateUser(ResponseCallback responseCallback) throws Exception {
        Log.d(TAG, "authenticateUser: " + getLink("signinApp"));
        JSONObject json = new JSONObject();
        json.put("email", Staging_Email2);
        json.put("password", Staging_Password2);
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, getLink("signinApp"), json,
                responseCallback::onSuccess,
                error -> responseCallback.onError(parseError(error, "Failed to authenticate")));
        requestQueue.add(stringRequest);
    }

    public void getDepartmentList(ResponseCallback responseCallback) {
        Log.d(TAG, "getDepartmentList: " + getLink("departmentnew"));
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, getLink("departmentnew"), null,
                responseCallback::onSuccess,
                error -> {
                    responseCallback.onError(parseError(error, "Failed to fetch department list"));
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = getToken();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        requestQueue.add(objectRequest);
    }

    public void fetchData(String accountNumber, ResponseCallback responseCallback) {
        Log.d(TAG, "fetchData: " + getRestoreLink(accountNumber));
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, getRestoreLink(accountNumber), null,
                responseCallback::onSuccess, error -> {
            responseCallback.onError(parseError(error, "Failed to fetch data"));
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = getToken();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        requestQueue.add(objectRequest);
    }

    private static final String TAG = "API";
    public void restoreDevice(String deviceID, ResponseCallback responseCallback) {
        Log.d(TAG, "restoreDevice: " + getRestoreDeviceLink(deviceID));
        JsonObjectRequest objectRequest = new JsonObjectRequest(Request.Method.GET, getRestoreDeviceLink(deviceID), null,
                responseCallback::onSuccess, error -> {
            responseCallback.onError(parseError(error, "Failed to Refresh Data"));
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = getToken();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        requestQueue.add(objectRequest);
    }

    public void createDevice(JSONObject json, ResponseCallback responseCallback) {
        Log.d(TAG, "createDevice: " + getLink("device-create"));
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, getLink("device-create"), json,
                responseCallback::onSuccess, error -> {
            responseCallback.onError(parseError(error, "Failed to create device"));
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = getToken();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        requestQueue.add(stringRequest);
    }

    public void updateDevice(JSONObject json, ResponseCallback responseCallback) {
        Log.d(TAG, "createDevice: " + getLink("device-updatenew"));
        JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, API.getLink("device-updatenew"), json,
                responseCallback::onSuccess, error -> {
            responseCallback.onError(parseError(error, "failed to update device"));
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                String token = getToken();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };
        requestQueue.add(stringRequest);
    }

    private String getToken() {
        return Stash.getString(TOKEN, "");
    }

    public static String parseError(VolleyError error, String defaultMessage) {
        try {
            String errorBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
            JSONObject jsonError = new JSONObject(errorBody);
            Stash.put("DATA", jsonError.toString());
            return jsonError.getString("message");
        } catch (Exception e) {
            e.printStackTrace();
            return defaultMessage;
        }
    }

}
