package com.example.smartparkingclient.api.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class UserDataService {
    private static UserDataService instance = new UserDataService();
    private SharedPreferences sharedPreferences;

    private UserDataService() {}

    public static void initialize() {
        if (instance == null)
            instance = new UserDataService();
    }

    public static UserDataService getInstance(Context context) {
        if (instance == null) {
            instance = new UserDataService();
            instance.sharedPreferences = context.getSharedPreferences(SharedPreferencesName.PREFERENCES_NAME, MODE_PRIVATE);
        }

        return instance;
    }

    public String getToken() {
        return sharedPreferences.getString(SharedPreferencesName.AUTH_TOKEN, null);
    }

    public void setToken(String token) {
        this.sharedPreferences.edit().putString(SharedPreferencesName.AUTH_TOKEN, token).apply();
    }

    public boolean isLoggedIn() {
        return getToken() != null;
    }
}
