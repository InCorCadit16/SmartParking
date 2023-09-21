package com.example.smartparkingclient.view;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartparkingclient.R;
import com.example.smartparkingclient.api.client.ApiClient;
import com.example.smartparkingclient.api.client.ApiService;
import com.example.smartparkingclient.api.contracts.GetPlacesByDateRequest;
import com.example.smartparkingclient.api.contracts.GetPlacesByDateResponse;
import com.example.smartparkingclient.api.models.BaseModel;
import com.example.smartparkingclient.api.models.BookingEditForm;
import com.example.smartparkingclient.api.models.Parking;
import com.example.smartparkingclient.api.models.ParkingBooking;
import com.example.smartparkingclient.api.models.ParkingPlace;
import com.example.smartparkingclient.api.utils.StreamUtils;
import com.example.smartparkingclient.api.utils.UserDataService;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateEditActivity extends AppCompatActivity {

    private DatePicker datePicker;
    private TimePicker timePickerStart;
    private TimePicker timePickerEnd;
    private Spinner parkingSpinner;
    private Spinner parkingPlaceSpinner;

    private Button bookButton;

    private ApiService apiService;
    private UserDataService userDataService;

    private ParkingBooking booking = new ParkingBooking();
    private boolean isEdit = false;
    private GetPlacesByDateResponse currentParkingData;
    private List<ParkingPlace> currentParkingPlaces;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit);

        var bookingForm = new BookingEditForm();
        bookingForm.startDate = new Date();
        bookingForm.endDate = new Date();
        if (getIntent().hasExtra("BOOKING")) {
            bookingForm = (BookingEditForm) getIntent().getSerializableExtra("BOOKING");
            isEdit = true;
        }

        userDataService = UserDataService.getInstance(CreateEditActivity.this);
        apiService = ApiClient.getApiService(userDataService);

        datePicker = findViewById(R.id.date_picker);
        timePickerStart = findViewById(R.id.time_picker_start);
        timePickerEnd = findViewById(R.id.time_picker_end);
        parkingSpinner = findViewById(R.id.parking_spinner);
        parkingPlaceSpinner = findViewById(R.id.parking_place_spinner);
        bookButton = findViewById(R.id.booking_action);

        bookButton.setText(isEdit ? "Update" : "Book");
        bookButton.setOnClickListener((v) -> {
            if(isEdit) updateBooking();
            else createBooking();
        });

        setupForm(bookingForm);
    }

    private void setupForm(BookingEditForm formData) {
        booking.userId = this.userDataService.getUser().id;
        if (isEdit) {
            booking.id = formData.id;
            booking.startDate = formData.startDate;
            booking.endDate = formData.endDate;
            booking.placeId = formData.placeId;
        }

        var calendar = Calendar.getInstance();
        calendar.setTime(formData.startDate);

        datePicker.init(
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH),
                (view, year, monthOfYear, dayOfMonth) -> {}
        );

        timePickerStart.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePickerStart.setMinute(calendar.get(Calendar.MINUTE));
        timePickerStart.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            if (!timePickerStart.validateInput())
                return;
            Calendar date = getSelectedDate();
            date.set(Calendar.HOUR_OF_DAY, hourOfDay);
            date.set(Calendar.MINUTE, minute);
            booking.startDate = date.getTime();
            loadAvailableParking();
        });

        calendar.setTime(formData.endDate);
        timePickerEnd.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        timePickerEnd.setMinute(calendar.get(Calendar.MINUTE));
        timePickerEnd.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            if (!timePickerEnd.validateInput())
                return;
            Calendar date = getSelectedDate();
            date.set(Calendar.HOUR_OF_DAY, hourOfDay);
            date.set(Calendar.MINUTE, minute);
            booking.endDate = date.getTime();
            loadAvailableParking();
        });

        setSpinnersListeners();
        if (isEdit) {
            loadAvailableParking(true);
        }
    }

    private void loadAvailableParking() {
        loadAvailableParking(false);
    }

    private void loadAvailableParking(boolean setEditValues) {
        // button disabled while new options are loading
        // it will be enabled as soon as a parking place
        // will be available and selected
        bookButton.setEnabled(false);

        // check if start and end date are both set
        if (booking.startDate == null || booking.endDate == null)
            return;

        // check if start and end date are valid
        if (booking.startDate.after(booking.endDate)) {
            Toast.makeText(this, "Start time must be before end time", Toast.LENGTH_SHORT).show();
            return;
        }

        var request = new GetPlacesByDateRequest();
        request.startDate = booking.startDate;
        request.endDate = booking.endDate;
        Call<GetPlacesByDateResponse> getPlaces = apiService.search_places(request);

        getPlaces.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<GetPlacesByDateResponse> call, Response<GetPlacesByDateResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentParkingData = response.body();
                    setSpinnersValues(setEditValues);
                }
            }

            @Override
            public void onFailure(Call<GetPlacesByDateResponse> call, Throwable t) {
                Toast.makeText(CreateEditActivity.this, "Failed to load parking data", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setSpinnersValues(boolean setEditValues) {
        var names = Arrays.stream(currentParkingData.parkings).map(p -> p.name).collect(Collectors.toList());
        setSpinnerToAdapter(names, parkingSpinner);
        int selectedParkingIndex = 0;
        int selectedPlaceIndex = 0;

        int parkingId = currentParkingData.parkings[selectedParkingIndex].id;
        currentParkingPlaces = Arrays.stream(currentParkingData.places)
                .filter(pl -> pl.parkingId == parkingId)
                .collect(Collectors.toList());
        var places = currentParkingPlaces.stream().map(p -> p.code).collect(Collectors.toList());
        setSpinnerToAdapter(places, parkingPlaceSpinner);

        if (setEditValues) {
            ParkingPlace pp = Arrays.stream(currentParkingData.places).filter(p -> p.id == booking.id).findFirst().get();
            Parking pk = Arrays.stream(currentParkingData.parkings).filter(p -> p.id == pp.parkingId).findFirst().get();

            selectedParkingIndex = Arrays.asList(currentParkingData.parkings).indexOf(pk);
            selectedPlaceIndex = currentParkingPlaces.indexOf(pp);
        }

        parkingSpinner.setSelection(selectedParkingIndex);
        parkingPlaceSpinner.setSelection(selectedPlaceIndex);
    }

    private void setSpinnersListeners() {
        parkingSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Parking currentParking = currentParkingData.parkings[position];
                currentParkingPlaces = Arrays.stream(currentParkingData.places)
                        .filter(pl -> pl.parkingId == currentParking.id)
                        .collect(Collectors.toList());
                var names = currentParkingPlaces.stream().map(p -> p.code).collect(Collectors.toList());
                setSpinnerToAdapter(names, parkingPlaceSpinner);
                bookButton.setEnabled(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        parkingPlaceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                booking.placeId = currentParkingPlaces.get(position).id;
                bookButton.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private Calendar getSelectedDate() {
        var calendar = Calendar.getInstance();
        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
        return calendar;
    }

    private void createBooking() {
        Call<ParkingBooking> createBook = apiService.create_booking(booking);

        createBook.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ParkingBooking> call, Response<ParkingBooking> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateEditActivity.this, "Booking created!", Toast.LENGTH_SHORT).show();
                    // return to main screen
                    onBackPressed();
                }
            }

            @Override
            public void onFailure(Call<ParkingBooking> call, Throwable t) {
                Toast.makeText(CreateEditActivity.this, "Failed to create booking", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateBooking() {
        Call<ParkingBooking> updateBook = apiService.update_booking(booking, booking.id);

        updateBook.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ParkingBooking> call, Response<ParkingBooking> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CreateEditActivity.this, "Booking updated!", Toast.LENGTH_SHORT).show();
                    // return to main screen
                    onBackPressed();
                }
            }

            @Override
            public void onFailure(Call<ParkingBooking> call, Throwable t) {
                Toast.makeText(CreateEditActivity.this, "Failed to create booking", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setSpinnerToAdapter(List<String> values, Spinner spinner) {
        var adapter = new ArrayAdapter<>(CreateEditActivity.this, android.R.layout.simple_spinner_dropdown_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
}
