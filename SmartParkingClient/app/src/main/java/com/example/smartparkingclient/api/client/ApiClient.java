package com.example.smartparkingclient.api.client;

import com.example.smartparkingclient.api.utils.UserDataService;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.Locale;

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

            var parser = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(parser))
                    .client(httpClient.build())
                    .build();
        }
        return retrofit;
    }

    public static ApiService getApiService(UserDataService userData) {
        return getApiClient(userData).create(ApiService.class);
    }
}
