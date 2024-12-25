package com.suleman.capturingbanking;

public class API {
    private static final String BASE = "https://www.btocsms.com/api/";
    public static String getLink(String path) {
        return BASE + path;
    }

    public static String getRestoreLink(String accountNumber) {
        return BASE + "devicerecord/" +  accountNumber;
    }

}
