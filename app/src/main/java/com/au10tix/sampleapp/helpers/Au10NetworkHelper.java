package com.au10tix.sampleapp.helpers;

public class Au10NetworkHelper {

    public static final String ACCESS_TOKEN = "xxx.xxx.xxx";
    public static final String SESSION_TOKEN = "xxx.xxx.xxx";

    public static void getBearerToken(JwtTokenReturner tokenInterface) {
        tokenInterface.onJwtTokenAcquired(ACCESS_TOKEN, SESSION_TOKEN);
    }

    public interface JwtTokenReturner {
        void onJwtTokenAcquired(String accessToken, String sessionToken);
    }
}

