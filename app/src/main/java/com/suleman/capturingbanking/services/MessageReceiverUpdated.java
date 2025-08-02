package com.suleman.capturingbanking.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.suleman.capturingbanking.utilies.NotificationHelper;
import com.suleman.capturingbanking.utilies.Utils;

public class MessageReceiverUpdated extends BroadcastReceiver {
    public String TAG = "MessageReceiverUpdated";
    public static final String INFO = "INFO";
    public static final int RETRY_DELAY_MS = 10;
    public static final String RETRY_DELAY = "RETRY_DELAY";
    private static final int MAX_RETRIES = 5;
    private static final int FRESH_MESSAGE_THRESHOLD_MS = 90_000;
    private Context context;
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

                    Log.d(TAG, "onReceive: sender: " + sender);
                    Log.d(TAG, "onReceive: Full Message: " + fullMessage);

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
                            NotificationHelper.sendNotification(context, "Message Failed", model.getSms());
                            Utils.scheduleWorkManager(context, 4);
                        }
                    }
                }
            }
        }
    }

    private void getJWT(MessageModel model) {
        Log.d(TAG, "getJWT");
        ServerConnector.getInstance(false).loginUser(new ResponseCallback() {
            @Override
            public void onSuccess(Object response) {
                retryApiCall(model, 0);
            }

            @Override
            public void onError(String response) {
                Log.d(TAG, "JWT Error : " + response);
                messageDAO.insert(model);
                NotificationHelper.sendNotification(context, "Message Failed", model.getSms());
                Utils.scheduleWorkManager(context, 4);
                Toast.makeText(context, response, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void retryApiCall(MessageModel model, int attempt) {
        if (attempt >= MAX_RETRIES) {
            messageDAO.insert(model);
            NotificationHelper.sendNotification(context, "Message Failed", model.getSms());
            Utils.scheduleWorkManager(context, 4);
            return;
        }

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ServerConnector.getInstance(false).sendMessage(model.toJson(), new ResponseCallback() {
                @Override
                public void onSuccess(Object response) {
                    Log.d(TAG, "Message sent successfully");
                    NotificationHelper.sendNotification(context, "Banking SMS", "Message sent successfully");
                }

                @Override
                public void onError(String response) {
                    NotificationHelper.sendNotification(context, "API Failed", "Retrying");
                    retryApiCall(model, attempt + 1);
                }
            });

            Log.d(TAG, "Server Request: " + model.toJson());

            if (System.currentTimeMillis() - model.getTimestamp() < FRESH_MESSAGE_THRESHOLD_MS) {
                ServerConnector.getInstance(true).sendMessageToUpiServer(model.toJson(), new ResponseCallback() {
                    @Override
                    public void onSuccess(Object response) {
                        Log.d(TAG, "UPI Message sent successfully");
                        NotificationHelper.sendNotification(context, "Upi Server", "Message Successfully Sent");
                    }

                    @Override
                    public void onError(String response) {
                        Log.d(TAG, "UpiServer Error : " + response);
                        Log.d(TAG, "UPI Message failed, but won't retry if stale");

                        NotificationHelper.sendNotification(context, "Upi Server", "Message Sending Failed");

                    }
                });
            } else {
                NotificationHelper.sendNotification(context, "Old Message", "Message is 90 Sec old for UPI API");
            }
        }, (Stash.getInt(RETRY_DELAY, RETRY_DELAY_MS) * 1000L));
    }

}
