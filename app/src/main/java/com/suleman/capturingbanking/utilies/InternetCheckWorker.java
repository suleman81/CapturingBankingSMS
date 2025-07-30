package com.suleman.capturingbanking.utilies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.CoroutineWorker;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.suleman.capturingbanking.api.ResponseCallback;
import com.suleman.capturingbanking.api.ServerConnector;
import com.suleman.capturingbanking.db.AppDatabase;
import com.suleman.capturingbanking.db.MessageDAO;
import com.suleman.capturingbanking.model.MessageModel;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import kotlin.coroutines.Continuation;
import retrofit2.Call;
import retrofit2.Response;

public class InternetCheckWorker extends Worker {
    private static final String TAG = "InternetCheckWorker";
    private MessageDAO messageDAO;
    private static final long MESSAGE_EXPIRY_TIME = 90 * 1000; // 90 seconds

    public InternetCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        AppDatabase db = AppDatabase.getInstance(context);
        messageDAO = db.messageDAO();
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork");

        int retryCount = getInputData().getInt("RETRY_COUNT", 0);

        if (!isInternetAvailable()) {
            return Result.retry();
        }


        Log.d(TAG, "retryCount: " + retryCount);

        if (retryCount < 4) {
            retryCount++; // Increment retry count
            scheduleNextCheck(getApplicationContext(), retryCount);
            return Result.retry(); // Mark as retry to indicate it failed
        }

        List<MessageModel> pendingMessages = messageDAO.getAllMessages().getValue();
        if (pendingMessages != null && !pendingMessages.isEmpty()) {
            long currentTime = System.currentTimeMillis();

            for (MessageModel message : pendingMessages) {
                if (!processMessage(message, currentTime)) {
                    return Result.retry();
                }
            }
        }

        return Result.success();
    }
    private void scheduleNextCheck(Context context, int retryCount) {
        Utils.scheduleWorkManager(context, retryCount);
    }

    private boolean processMessage(MessageModel message, long currentTime) {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] api1Success = {false};
        final boolean[] api2Success = {false};

        // **1st API Call - Always Called**
        sendMessageApi(message, success -> {
            api1Success[0] = success;
            latch.countDown();
        });

        // **2nd API Call - Skip if Message is Older than 90 Seconds**
        if (currentTime - message.getTimestamp() > MESSAGE_EXPIRY_TIME) {
            api2Success[0] = true; // Skipping API 2, so consider it successful
        } else {
            sendMessageToUpiServer(message, success -> api2Success[0] = success);
        }

        try {
            latch.await(10, TimeUnit.SECONDS); // Wait for 10 seconds max
            return api1Success[0] && api2Success[0];
        } catch (InterruptedException e) {
            return false;
        }
    }

    private void sendMessageApi(MessageModel message, ApiCallback callback) {
        Log.d(TAG, "sendMessageApi");
        ServerConnector.getInstance(false).sendMessage(
                message.toJson(), new ResponseCallback() {
                    @Override
                    public void onSuccess(Object response) {
                        if (((Response<String>) response).isSuccessful()) {
                            messageDAO.deleteById(message.getId());
                            callback.onResult(true);
                        } else {
                            callback.onResult(false);
                        }
                    }

                    @Override
                    public void onError(String response) {
                        callback.onResult(false);
                    }
                }
        );
    }

    private void sendMessageToUpiServer(MessageModel message, ApiCallback callback) {
        ServerConnector.getInstance(true).sendMessageToUpiServer(
                message.toJson(), new ResponseCallback() {
                    @Override
                    public void onSuccess(Object response) {
                        if (((Response<String>) response).isSuccessful()) {
                            messageDAO.deleteById(message.getId());
                            callback.onResult(true);
                        } else {
                            callback.onResult(false);
                        }
                    }

                    @Override
                    public void onError(String response) {
                        callback.onResult(false);
                    }
                }
        );
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    interface ApiCallback {
        void onResult(boolean success);
    }
}
