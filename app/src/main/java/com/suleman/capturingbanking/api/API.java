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
    public static final String UPI_SERVER = "https://upipayment.co/api/message-request";
    public static final String UPI_SERVER_STAGING = "https://stage.upipayment.co/api/message-request";
    public static String getLink(String path) {
        return BASE + path;
    }
}
