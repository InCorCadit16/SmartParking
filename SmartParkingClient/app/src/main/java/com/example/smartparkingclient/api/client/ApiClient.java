package com.example.smartparkingclient.api.client;

import android.content.Context;

import com.example.smartparkingclient.api.utils.UserDataService;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://10.0.2.2:5000/";

    private static Retrofit retrofit;

    public static Retrofit getApiClient(UserDataService userData) {
        if (retrofit == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(new AuthInterceptor(userData));

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService(UserDataService userData) {
        return getApiClient(userData).create(ApiService.class);
    }
}
