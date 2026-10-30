package com.transporttn.app.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.transporttn.app.R;
import com.transporttn.app.network.ApiClient;

/**
 * Ecran 7a du parcours utilisateur : suivi en temps reel de la position du chauffeur.
 *
 * NOTE IMPORTANTE (voir cahier des charges section 3, "Temps reel GPS") : en production,
 * la position du chauffeur est mise a jour toutes les 3 secondes via Firebase Firestore
 * (listener temps reel cote client), et non par polling REST. Ce squelette montre
 * l'integration Google Maps + un polling simplifie sur l'API REST a titre d'exemple ;
 * remplacer refreshDriverPosition() par un Firestore addSnapshotListener() sur le document
 * du chauffeur pour la version finale temps reel.
 */
public class TaxiTrackingActivity extends FragmentActivity implements OnMapReadyCallback {

    public static final String EXTRA_RESERVATION_ID = "reservation_id";

    private GoogleMap googleMap;
    private Marker driverMarker;
    private TextView statusText;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Long reservationId;

    private static final int POLL_INTERVAL_MS = 3000; // 3 secondes, conforme au cahier des charges

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxi_tracking);

        reservationId = getIntent().getLongExtra(EXTRA_RESERVATION_ID, -1);
        statusText = findViewById(R.id.statusText);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;
        // Position par defaut : Tunis
        LatLng tunis = new LatLng(36.8065, 10.1815);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tunis, 13f));

        startPolling();
    }

    private void startPolling() {
        handler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
    }

    private final Runnable pollRunnable = new Runnable() {
        @Override
        public void run() {
            refreshDriverPosition();
            handler.postDelayed(this, POLL_INTERVAL_MS);
        }
    };

    /**
     * TODO PRODUCTION: remplacer cet appel REST par un listener Firestore temps reel
     * sur le document "drivers/{driverId}" pour un rafraichissement fluide toutes les 3s
     * sans solliciter le backend Spring Boot pour chaque position.
     */
    private void refreshDriverPosition() {
        // Exemple d'integration laisse volontairement simple : a completer avec l'appel
        // reel a Firestore (com.google.firebase:firebase-firestore) une fois le projet
        // Firebase du chauffeur configure.
        statusText.setText(R.string.app_name);
    }

    private void updateDriverMarker(double lat, double lng) {
        LatLng position = new LatLng(lat, lng);
        if (driverMarker == null) {
            driverMarker = googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title("Chauffeur")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        } else {
            driverMarker.setPosition(position);
        }
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(position));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(pollRunnable);
    }
}
