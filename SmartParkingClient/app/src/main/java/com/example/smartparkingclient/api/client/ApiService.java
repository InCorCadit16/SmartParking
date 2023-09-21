package com.example.smartparkingclient.api.client;

import com.example.smartparkingclient.api.contracts.GetPlacesByDateRequest;
import com.example.smartparkingclient.api.contracts.GetPlacesByDateResponse;
import com.example.smartparkingclient.api.contracts.GetPlacesResponse;
import com.example.smartparkingclient.api.contracts.LoginRequest;
import com.example.smartparkingclient.api.contracts.LoginResponse;
import com.example.smartparkingclient.api.contracts.UserBookingResponse;
import com.example.smartparkingclient.api.models.Parking;
import com.example.smartparkingclient.api.models.ParkingBooking;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @POST("login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("parking")
    Call<Parking[]> get_parking_by_organization(@Query("org_id") int organizationId);

    @GET("parking/{parkingId}/places")
    Call<GetPlacesResponse> get_parking_places(@Path("parkingId") int parkingId);

    @GET("booking/my")
    Call<UserBookingResponse> get_user_bookings();

    @POST("booking")
    Call<ParkingBooking> create_booking(@Body ParkingBooking booking);

    @PUT("booking/{bookingId}")
    Call<ParkingBooking> update_booking(@Body ParkingBooking booking, @Path("bookingId") int bookingId);

    @DELETE("booking/{bookingId}")
    Call<Void> delete_booking(@Path("bookingId") int bookingId);

    @POST("parking/search")
    Call<GetPlacesByDateResponse> search_places(@Body GetPlacesByDateRequest request);
}
