package com.example.smartparkingclient.api.client;

import com.example.smartparkingclient.api.models.LoginRequest;
import com.example.smartparkingclient.api.models.LoginResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);
}
