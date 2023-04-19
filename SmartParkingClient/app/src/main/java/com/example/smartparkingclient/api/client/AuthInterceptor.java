package com.example.smartparkingclient.api.client;

import android.content.Context;

import com.example.smartparkingclient.api.utils.UserDataService;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private UserDataService userData;

    public AuthInterceptor(UserDataService userData) {
        this.userData = userData;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalReq = chain.request();
        if (!originalReq.url().pathSegments().get(0).equals("login") && this.userData.isLoggedIn()) {
            Request newRequest = originalReq.newBuilder()
                    .header("Authorization", "Bearer " + this.userData.getToken())
                    .build();
            return chain.proceed(newRequest);
        }
        return chain.proceed(originalReq);
    }
}
