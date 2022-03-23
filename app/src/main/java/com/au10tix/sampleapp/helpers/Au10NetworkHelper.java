package com.au10tix.sampleapp.helpers;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Au10NetworkHelper {
    //
    public static final String JWT_for_Bearer = "xxx.xxx.xxx";

    public static void getBearerToken(JwtTokenReturner tokenInterface) {
        tokenInterface.onJwtTokenAcquired(JWT_for_Bearer);
    }

    public interface JwtTokenReturner {
        void onJwtTokenAcquired(String jwtToken);
    }
}

