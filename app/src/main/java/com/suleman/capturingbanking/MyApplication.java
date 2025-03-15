package com.suleman.capturingbanking;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Configuration;
import androidx.work.WorkManager;

import com.fxn.stash.Stash;
import com.google.firebase.analytics.FirebaseAnalytics;

public class MyApplication extends Application implements  Configuration.Provider{
    @Override
    public void onCreate() {
        super.onCreate();
        Stash.init(this);
       // WorkManager.initialize(this, new Configuration.Builder().build());
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder().setMinimumLoggingLevel(
                Log.DEBUG
        ).build();
    }
}
