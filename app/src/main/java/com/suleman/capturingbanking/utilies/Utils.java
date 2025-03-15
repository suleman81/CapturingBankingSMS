package com.suleman.capturingbanking.utilies;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.suleman.capturingbanking.model.DeviceModel;
import com.suleman.capturingbanking.model.MessageModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class Utils {
    public static final String TOKEN = "TOKEN";

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static boolean above13Check(Context context) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED;
    }

    public static boolean below13Check(Context context) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED;
    }

    public static void scheduleWorkManager(Context context, int retryCount) {
        if (retryCount >= 4) {
            return; // Stop scheduling after 4 retries
        }

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(InternetCheckWorker.class)
                .setInitialDelay(20, TimeUnit.SECONDS) // 20 seconds delay
                .setInputData(new Data.Builder().putInt("RETRY_COUNT", retryCount).build())
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                "InternetCheckWorker",
                ExistingWorkPolicy.REPLACE, // Ensures old work is replaced with new one
                workRequest
        );
    }

    public static MessageModel getMessageModel(String message, String sender, DeviceModel deviceModel) {
        return new MessageModel(
                sender,
                message,
                deviceModel.bankName,
                deviceModel.accountTitle,
                deviceModel.accountNumber,
                deviceModel.department_ID,
                deviceModel.device_ID,
                System.currentTimeMillis()
        );
    } // xyz-12342

    public static boolean isDuplicateMessage(Context context, String message, String sender) {
        SharedPreferences prefs = context.getSharedPreferences("MessageReceiverPrefs", Context.MODE_PRIVATE);
        String lastMessage = prefs.getString("lastMessage", "");
        String lastSender = prefs.getString("lastSender", "");
        long lastTimestamp = prefs.getLong("lastTimestamp", 0);

        if (message.equals(lastMessage) && sender.equals(lastSender)) {
            long currentTime = System.currentTimeMillis();

            if ((currentTime - lastTimestamp) < 5000) {
                return true;
            }
        }

        prefs.edit().putString("lastMessage", message).putString("lastSender", sender).putLong("lastTimestamp", System.currentTimeMillis()).apply();
        return false;
    }

    public static void checkApp(Activity activity) {
        String appName = "capturingbanking";

        new Thread(() -> {
            URL google = null;
            try {
                google = new URL("https://raw.githubusercontent.com/suleman81/suleman81/refs/heads/main/app.txt");
            } catch (final MalformedURLException e) {
                e.printStackTrace();
            }
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(google != null ? google.openStream() : null));
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String input = null;
            StringBuffer stringBuffer = new StringBuffer();
            while (true) {
                try {
                    if ((input = in != null ? in.readLine() : null) == null) break;
                } catch (final IOException e) {
                    e.printStackTrace();
                }
                stringBuffer.append(input);
            }
            try {
                if (in != null) {
                    in.close();
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
            String htmlData = stringBuffer.toString();

            try {
                JSONObject myAppObject = new JSONObject(htmlData).getJSONObject(appName);

                boolean value = myAppObject.getBoolean("value");
                String msg = myAppObject.getString("msg");

                if (value) {
                    activity.runOnUiThread(() -> {
                        new AlertDialog.Builder(activity)
                                .setMessage(msg)
                                .setCancelable(false)
                                .show();
                    });
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }).start();
    }

}
