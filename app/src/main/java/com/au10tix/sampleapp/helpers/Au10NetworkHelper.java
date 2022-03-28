package com.au10tix.sampleapp.helpers;

public class Au10NetworkHelper {

    public static final String JWT_for_Bearer = "xxx.xxx.xxx";

    public static void getBearerToken(JwtTokenReturner tokenInterface) {
        tokenInterface.onJwtTokenAcquired(JWT_for_Bearer);
    }

    public interface JwtTokenReturner {
        void onJwtTokenAcquired(String jwtToken);
    }
}

