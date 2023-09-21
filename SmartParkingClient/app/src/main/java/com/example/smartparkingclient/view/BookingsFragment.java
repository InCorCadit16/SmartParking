package com.example.smartparkingclient.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.smartparkingclient.R;
import com.example.smartparkingclient.api.client.ApiClient;
import com.example.smartparkingclient.api.contracts.UserBookingResponse;
import com.example.smartparkingclient.api.utils.UserDataService;
import com.example.smartparkingclient.view.adapters.BookingsAdapter;
import com.example.smartparkingclient.view.adapters.ParkingsAdapter;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingsFragment extends Fragment {
    private RecyclerView bookingRecycler;
    private BookingsAdapter adapter;

    public BookingsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookings, container, false);
        bookingRecycler = view.findViewById(R.id.bookings_recycler);
        // empty booking adapter
        adapter = new BookingsAdapter(getContext(), new UserBookingResponse());
        bookingRecycler.setAdapter(adapter);
        bookingRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserBookings();
    }

    private void loadUserBookings() {
        var userDataService = UserDataService.getInstance(getContext());
        var apiService = ApiClient.getApiService(userDataService);

        Call<UserBookingResponse> userBookingsCall = apiService.get_user_bookings();

        userBookingsCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<UserBookingResponse> call, Response<UserBookingResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    adapter.setData(response.body());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<UserBookingResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load user bookings", Toast.LENGTH_LONG).show();
            }
        });
    }
}