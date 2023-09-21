package com.example.smartparkingclient.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.smartparkingclient.R;
import com.example.smartparkingclient.api.client.ApiClient;
import com.example.smartparkingclient.api.client.ApiService;
import com.example.smartparkingclient.api.contracts.LoginResponse;
import com.example.smartparkingclient.api.models.User;
import com.example.smartparkingclient.api.utils.UserDataService;

import retrofit2.Call;

public class UserFragment extends Fragment {
    private final User user;
    private TextView usernameTextView;
    private TextView firstNameTextView;
    private TextView lastNameTextView;
    private TextView phoneTextView;

    public UserFragment(User user) {
        this.user = user;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        usernameTextView = view.findViewById(R.id.username_textview);
        firstNameTextView = view.findViewById(R.id.first_name_textview);
        lastNameTextView = view.findViewById(R.id.last_name_textview);
        phoneTextView = view.findViewById(R.id.phone_textview);

        usernameTextView.setText(user.username);
        firstNameTextView.setText(user.firstName);
        lastNameTextView.setText(user.lastName);
        phoneTextView.setText(user.phone);

        return view;
    }
}