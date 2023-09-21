package com.example.smartparkingclient.view.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartparkingclient.R;
import com.example.smartparkingclient.api.client.ApiClient;
import com.example.smartparkingclient.api.client.ApiService;
import com.example.smartparkingclient.api.contracts.UserBookingResponse;
import com.example.smartparkingclient.api.models.BookingEditForm;
import com.example.smartparkingclient.api.models.Parking;
import com.example.smartparkingclient.api.models.ParkingBooking;
import com.example.smartparkingclient.api.models.ParkingPlace;
import com.example.smartparkingclient.api.utils.DateFormatter;
import com.example.smartparkingclient.api.utils.UserDataService;
import com.example.smartparkingclient.view.CreateEditActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingsAdapter extends RecyclerView.Adapter<BookingsAdapter.BookingHolder> {
    private Context context;
    private ArrayList<ParkingBooking> bookings;
    private List<Parking> parkings;
    private List<ParkingPlace> places;

    public BookingsAdapter(Context context, UserBookingResponse bookingData) {
        this.context = context;
        setData(bookingData);
    }

    @NonNull
    @Override
    public BookingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.booking_item, parent, false);
        return new BookingHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingHolder holder, int position) {
        ParkingBooking booking = bookings.get(position);
        ParkingPlace place = places.stream().filter(p -> p.id == booking.placeId).findFirst().get();
        Parking parking = parkings.stream().filter(p -> p.id == place.parkingId).findFirst().get();

        holder.parkingName.setText(parking.name);
        holder.placeName.setText(place.code);
        holder.date.setText(DateFormatter.getDate(booking.startDate));
        holder.timeSpan.setText(DateFormatter.getTimeSpan(booking.startDate, booking.endDate));

        holder.editButton.setOnClickListener((v) -> {
            var intent = new Intent(context, CreateEditActivity.class);
            var editForm = new BookingEditForm();
            editForm.id = booking.id;
            editForm.startDate = booking.startDate;
            editForm.endDate = booking.endDate;
            editForm.parkingId = parking.id;
            editForm.placeId = place.id;
            intent.putExtra("BOOKING", editForm);
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener((v) -> {
            var userData = UserDataService.getInstance(context);
            var api = ApiClient.getApiService(userData);
            
            Call<Void> deleteBooking = api.delete_booking(booking.id);
            deleteBooking.enqueue(new Callback<>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(context, "Booking deleted!", Toast.LENGTH_SHORT).show();
                        bookings.remove(booking);
                        notifyItemRemoved(holder.getAdapterPosition());
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(context, "Failed to delete booking", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public void setData(UserBookingResponse bookingData) {
        Arrays.sort(bookingData.bookings, (b1, b2) -> b1.startDate.before(b2.startDate) ? -1 : 1);
        bookings = new ArrayList<>(Arrays.asList(bookingData.bookings));
        parkings = Arrays.asList(bookingData.parkings);
        places = Arrays.asList(bookingData.places);
    }

    public static class BookingHolder extends RecyclerView.ViewHolder {
        TextView parkingName;
        TextView placeName;
        TextView date;
        TextView timeSpan;
        Button editButton, deleteButton;
        LinearLayout actionsLayout;

        boolean showActions = false;

        public BookingHolder(@NonNull View itemView) {
            super(itemView);
            parkingName = itemView.findViewById(R.id.parking_name);
            placeName = itemView.findViewById(R.id.place_name);
            date = itemView.findViewById(R.id.date_value);
            timeSpan = itemView.findViewById(R.id.time_span_value);
            actionsLayout = itemView.findViewById(R.id.actions_layout);
            editButton = itemView.findViewById(R.id.edit_button);
            deleteButton = itemView.findViewById(R.id.delete_button);

            itemView.setOnClickListener((v) -> {
                showActions = !showActions;
                actionsLayout.setVisibility(showActions ? View.VISIBLE : View.GONE);
            });
        }
    }
}
