package com.example.smartparkingclient.view;

import android.content.Intent;
import android.os.Bundle;

import com.example.smartparkingclient.R;
import com.example.smartparkingclient.api.client.ApiClient;
import com.example.smartparkingclient.api.client.ApiService;
import com.example.smartparkingclient.api.models.LoginRequest;
import com.example.smartparkingclient.api.models.LoginResponse;
import com.example.smartparkingclient.api.utils.UserDataService;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.smartparkingclient.databinding.ActivityLoginBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private EditText usernameField;
    private EditText passwordField;
    private ApiService apiService;
    private UserDataService userDataService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userDataService = UserDataService.getInstance(LoginActivity.this);
        apiService = ApiClient.getApiService(userDataService);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        usernameField = findViewById(R.id.username_edittext);
        passwordField = findViewById(R.id.password_edittext);
        Button loginButton = findViewById(R.id.login_button);

        loginButton.setOnClickListener(view -> {
            var username = usernameField.getText().toString();
            var password = passwordField.getText().toString();
            if (username.isBlank() || password.isBlank())
                return;

            var request = new LoginRequest();
            request.username = username;
            request.password = password;

            Call<LoginResponse> call = apiService.login(request);

            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        UserDataService.initialize();
                        userDataService.setToken(response.body().token);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                    Toast.makeText(LoginActivity.this, "Server error while trying to log in", Toast.LENGTH_LONG).show();
                }
            });
        });
    }
}