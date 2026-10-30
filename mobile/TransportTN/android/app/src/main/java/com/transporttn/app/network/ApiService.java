package com.transporttn.app.network;

import com.transporttn.app.models.*;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

/**
 * Definition des endpoints REST exposes par le backend Spring Boot.
 * Voir le cahier des charges section 5 pour la specification complete.
 */
public interface ApiService {

    // --- Authentification (OTP genere par le backend, pas de Firebase Auth) ---
    @POST("api/auth/otp/request")
    Call<Map<String, Object>> requestOtp(@Body OtpRequest request);

    @POST("api/auth/otp/verify")
    Call<AuthResponse> verifyOtp(@Body OtpVerifyRequest request);

    @GET("api/auth/me")
    Call<User> getMe();

    // --- Profil / notifications ---
    @PUT("api/users/fcm-token")
    Call<Void> updateFcmToken(@Body FcmTokenRequest request);

    // --- Reservation Taxi ---
    @POST("api/reservations/taxi")
    Call<Reservation> createTaxiReservation(@Body TaxiReservationRequest request);

    // --- Taxi Collectif ---
    @POST("api/reservations/collectif")
    Call<Reservation> createCollectifReservation(@Body CollectifReservationRequest request);

    @GET("api/reservations/lignes/collectif")
    Call<List<Ligne>> getLignesCollectif(@Query("date") String date);

    // --- Bus ---
    @GET("api/reservations/lignes/bus")
    Call<List<Ligne>> getLignesBus(@Query("date") String date);

    @POST("api/reservations/bus")
    Call<Ticket> purchaseBusTicket(@Body BusTicketRequest request);

    @GET("api/reservations/tickets")
    Call<List<Ticket>> getMyTickets();

    // --- Historique ---
    @GET("api/reservations/history")
    Call<Map<String, Object>> getHistory(@Query("page") int page, @Query("size") int size);

    // --- Chauffeur ---
    @PUT("api/driver/location")
    Call<Void> updateDriverLocation(@Body Map<String, Double> location);

    @PUT("api/driver/available")
    Call<Void> updateDriverAvailability(@Query("available") boolean available);
}
