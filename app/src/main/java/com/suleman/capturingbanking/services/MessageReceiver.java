package com.suleman.capturingbanking.services;

import static com.suleman.capturingbanking.api.API.UPI_SERVER;
import static com.suleman.capturingbanking.api.API.UPI_SERVER_STAGING;
import static com.suleman.capturingbanking.utilies.Utils.TOKEN;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fxn.stash.Stash;
import com.suleman.capturingbanking.api.API;
import com.suleman.capturingbanking.api.ResponseCallback;
import com.suleman.capturingbanking.model.DeviceModel;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MessageReceiver extends BroadcastReceiver {
    public String TAG = "MyPhoneStateListener";
    Context context;
    public static final String INFO = "INFO";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        this.context = context;
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED") || intent.getAction().equals("com.google.android.gms.rcs.RECEIVE_RCS_MESSAGE")) {
            Log.d(TAG, "onReceive SMS");
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    StringBuilder fullMessage = new StringBuilder();
                    String sender = "";
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        sender = smsMessage.getDisplayOriginatingAddress();
                        fullMessage.append(smsMessage.getMessageBody());
                    }

                    // Check if the message is a duplicate
                    if (isDuplicateMessage(context, fullMessage.toString(), sender)) {
                        Log.d(TAG, "Duplicate SMS received, ignoring...");
                        return;
                    }

                    DeviceModel deviceModel = (DeviceModel) Stash.getObject(INFO, DeviceModel.class);
                    if (deviceModel != null) {
                        try {
                            String finalSender = sender;
                            API.getInstance(context).authenticateUser(new ResponseCallback() {
                                @Override
                                public void onSuccess(JSONObject response) {
                                    try {
                                        String token = response.getString("token");
                                        Log.d(TAG, "onSuccess: " + token);
                                        Stash.put(TOKEN, token);
                                        JSONObject json = getJSON(fullMessage.toString(), finalSender, deviceModel);
                                        new Handler().postDelayed(() -> {
                                            callApi(context, json);
                                        }, 200);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                @Override
                                public void onError(String response) {
                                    Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private JSONObject getJSON(String message, String sender, DeviceModel deviceModel) throws Exception {
        JSONObject json = new JSONObject();
        json.put("channel", sender);
        json.put("sms", message);
        json.put("bank_name", deviceModel.bankName);
        json.put("account_title", deviceModel.accountTitle);
        json.put("account_number", deviceModel.accountNumber);
        json.put("department", deviceModel.department_ID);
        json.put("device", deviceModel.device_ID);
        return json;
    }

    private boolean isDuplicateMessage(Context context, String message, String sender) {
        SharedPreferences prefs = context.getSharedPreferences("MessageReceiverPrefs", Context.MODE_PRIVATE);
        String lastMessage = prefs.getString("lastMessage", "");
        String lastSender = prefs.getString("lastSender", "");
        long lastTimestamp = prefs.getLong("lastTimestamp", 0);

        // Check if the current message and sender are the same as the last ones
        if (message.equals(lastMessage) && sender.equals(lastSender)) {
            long currentTime = System.currentTimeMillis();
            // If the last message was received within 5 seconds, consider it duplicate
            if ((currentTime - lastTimestamp) < 5000) {
                return true;
            }
        }

        // Save the new message details
        prefs.edit().putString("lastMessage", message).putString("lastSender", sender).putLong("lastTimestamp", System.currentTimeMillis()).apply();

        return false;
    }

    private void callApi(Context context, JSONObject json) {
        Log.d(TAG, "json: " + json);

        RequestQueue requestQueue = VolleySingleton.getInstance(this.context).getRequestQueue();
        try {
            Log.d(TAG, "callApi: " + API.getLink("createnew"));
            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, API.getLink("createnew"), json, response -> {
                Log.d(TAG, "Response : " + response.toString());
            }, error -> {
                Log.e(TAG, "Error: " + parseError(error, "No response received"));
                if (error.networkResponse != null) {
                    String errorData = new String(error.networkResponse.data);
                    Log.e(TAG, "Error: " + error.getLocalizedMessage());
                    Log.e(TAG, "Error Response Data: " + errorData);
                    Log.e(TAG, "Status Code: " + error.networkResponse.statusCode);
                }
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    String token = Stash.getString(TOKEN, "");
                    Log.d(TAG, "getHeaders: " + token);
                    headers.put("Authorization", "Bearer " + token);
                    return headers;
                }
            };
            requestQueue.add(stringRequest);

            JsonObjectRequest upipayment = new JsonObjectRequest(Request.Method.POST, UPI_SERVER_STAGING, json, response -> {
                Log.d(TAG, "Response UPI: " + response.toString());
            }, error -> {
                Log.e(TAG, "Error UPI: " + parseError(error, "No response received"));
                if (error.networkResponse != null) {
                    String errorData = new String(error.networkResponse.data);
                    Log.e(TAG, "Error UPI : " + error.getLocalizedMessage());
                    Log.e(TAG, "Error Response Data UPI: " + errorData);
                    Log.e(TAG, "Status Code UPI : " + error.networkResponse.statusCode);
                }
            });
            requestQueue.add(upipayment);
        } catch (SecurityException e) {
            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public String parseError(VolleyError error, String defaultMessage) {
        Log.d(TAG, "parseError: " + error);
        try {
            if (error.networkResponse != null) {
                String errorBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
                JSONObject jsonError = new JSONObject(errorBody);
                return jsonError.getString("message");
            }
            return defaultMessage;
        } catch (Exception e) {
            e.printStackTrace();
            return defaultMessage;
        }
    }

}