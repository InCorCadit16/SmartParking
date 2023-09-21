package com.example.smartparkingclient.api.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.smartparkingclient.api.models.User;

import java.util.Date;

public class UserDataService {
    private static UserDataService instance;
    private SharedPreferences sharedPreferences;

    private UserDataService() {}

    public static UserDataService getInstance(Context context) {
        if (instance == null) {
            instance = new UserDataService();
            instance.sharedPreferences = context.getSharedPreferences(SharedPreferencesName.PREFERENCES_NAME, MODE_PRIVATE);
        }

        return instance;
    }

    public String getToken() {
        return instance.sharedPreferences.getString(SharedPreferencesName.AUTH_TOKEN, null);
    }

    public void setToken(String token) {
        instance.sharedPreferences.edit().putString(SharedPreferencesName.AUTH_TOKEN, token).apply();
    }

    public boolean isLoggedIn() {
        return getToken() != null && JWT.decode(getToken()).getExpiresAt().after(new Date());
    }

    public User getUser() {
        if (getToken() == null) return null;
        DecodedJWT jwtData = JWT.decode(getToken());
        User user = new User();
        user.id = jwtData.getClaim("user_id").asInt();
        user.username = jwtData.getClaim("username").asString();
        user.firstName = jwtData.getClaim("first_name").asString();
        user.lastName = jwtData.getClaim("last_name").asString();
        user.phone = jwtData.getClaim("phone").asString();
        user.organizationId = jwtData.getClaim("organization_id").asInt();

        return user;
    }
}
