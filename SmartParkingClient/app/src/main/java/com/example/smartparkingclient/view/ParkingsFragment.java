package com.example.smartparkingclient.view;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.example.smartparkingclient.R;
import com.example.smartparkingclient.api.client.ApiClient;
import com.example.smartparkingclient.api.models.Parking;
import com.example.smartparkingclient.api.utils.UserDataService;
import com.example.smartparkingclient.view.adapters.ParkingsAdapter;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParkingsFragment extends Fragment {
    ListView listView;

    public ParkingsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parkings, container, false);
        listView = view.findViewById(R.id.parking_list);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadParking();
    }

    private void loadParking() {
        var userDataService = UserDataService.getInstance(getContext());
        var apiService = ApiClient.getApiService(userDataService);

        Call<Parking[]> getParkings = apiService.get_parking_by_organization(userDataService.getUser().organizationId);

        getParkings.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Parking[]> call, Response<Parking[]> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var adapter = new ParkingsAdapter(getContext(), Arrays.asList(response.body()));
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<Parking[]> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to load parking list", Toast.LENGTH_LONG).show();
            }
        });
    }
}