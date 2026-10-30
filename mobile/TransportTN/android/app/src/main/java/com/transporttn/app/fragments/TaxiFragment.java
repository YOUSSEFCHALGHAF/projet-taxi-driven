package com.transporttn.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.transporttn.app.R;
import com.transporttn.app.activities.TaxiTrackingActivity;
import com.transporttn.app.models.Reservation;
import com.transporttn.app.models.TaxiReservationRequest;
import com.transporttn.app.network.ApiClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Reservation Taxi (cahier des charges section 2.2) :
 * saisie depart/arrivee sur carte Google Maps, estimation de prix (calculee cote backend
 * via la formule de Haversine), choix du mode de paiement, puis matching automatique.
 */
public class TaxiFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private TextInputEditText pickupInput;
    private TextInputEditText dropoffInput;
    private RadioGroup paymentGroup;
    private MaterialButton confirmButton;

    // Coordonnees saisies (dans une version complete, obtenues via geocodage inverse Google Maps
    // a partir d'un clic sur la carte, cf. cahier des charges 2.2 "geocodage inverse en francais")
    private Double pickupLat, pickupLng, dropoffLat, dropoffLng;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_taxi, container, false);

        pickupInput = view.findViewById(R.id.pickupInput);
        dropoffInput = view.findViewById(R.id.dropoffInput);
        paymentGroup = view.findViewById(R.id.paymentGroup);
        confirmButton = view.findViewById(R.id.confirmButton);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.miniMapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        confirmButton.setOnClickListener(v -> confirmReservation());

        return view;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;
        LatLng tunis = new LatLng(36.8065, 10.1815);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tunis, 12f));

        // Clic sur la carte : place un marqueur et definit les coordonnees de depart.
        // Une implementation complete distinguerait le premier clic (depart) du second (arrivee)
        // et utiliserait le Geocoder Android pour l'adresse textuelle en francais.
        googleMap.setOnMapClickListener(latLng -> {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(latLng).title("Depart"));
            pickupLat = latLng.latitude;
            pickupLng = latLng.longitude;
        });
    }

    private void confirmReservation() {
        String pickup = pickupInput.getText() != null ? pickupInput.getText().toString().trim() : "";
        String dropoff = dropoffInput.getText() != null ? dropoffInput.getText().toString().trim() : "";

        if (pickup.isEmpty() || dropoff.isEmpty()) {
            Toast.makeText(getContext(), "Veuillez remplir le depart et l'arrivee", Toast.LENGTH_SHORT).show();
            return;
        }

        // Valeurs par defaut si aucun point n'a ete selectionne sur la carte
        // (a remplacer par le geocodage reel de l'adresse saisie en texte).
        double pLat = pickupLat != null ? pickupLat : 36.8065;
        double pLng = pickupLng != null ? pickupLng : 10.1815;
        double dLat = dropoffLat != null ? dropoffLat : 36.8189;
        double dLng = dropoffLng != null ? dropoffLng : 10.1658;

        String paymentMode = paymentGroup.getCheckedRadioButtonId() == R.id.radioCarte ? "CARTE" : "ESPECES";

        TaxiReservationRequest request = new TaxiReservationRequest(
                pickup, pLat, pLng, dropoff, dLat, dLng, paymentMode);

        confirmButton.setEnabled(false);

        ApiClient.getApiService(requireContext()).createTaxiReservation(request)
                .enqueue(new Callback<Reservation>() {
                    @Override
                    public void onResponse(Call<Reservation> call, Response<Reservation> response) {
                        confirmButton.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null) {
                            Intent intent = new Intent(getActivity(), TaxiTrackingActivity.class);
                            intent.putExtra(TaxiTrackingActivity.EXTRA_RESERVATION_ID, response.body().getId());
                            startActivity(intent);
                        } else {
                            Toast.makeText(getContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Reservation> call, Throwable t) {
                        confirmButton.setEnabled(true);
                        Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
