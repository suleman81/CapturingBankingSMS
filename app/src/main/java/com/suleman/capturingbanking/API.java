package com.suleman.capturingbanking;

public class API {
    //    private static final String BASE = "http://3.29.241.160:8000/";
    private static final String BASE = "https://www.btocsms.com/api/";

    public static String getLink(String path) {
        return BASE + path;
    }

}
