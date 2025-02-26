package com.suleman.capturingbanking;

import android.app.Application;

import com.fxn.stash.Stash;
import com.google.firebase.analytics.FirebaseAnalytics;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Stash.init(this);
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true);
    }
}
