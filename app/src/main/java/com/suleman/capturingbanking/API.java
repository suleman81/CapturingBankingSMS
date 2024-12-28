package com.suleman.capturingbanking;

public class API {
    private static final String BASE = "https://www.btocsms.com/api/";
    private static final String STAGE_BASE = "https://stage.btocsms.com/api/";

    public static String getLink(String path) {
        return STAGE_BASE + path;
    }

    public static String getRestoreLink(String accountNumber) {
        return STAGE_BASE + "devicerecord/" +  accountNumber;
    }

}
