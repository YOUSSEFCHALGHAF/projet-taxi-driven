package com.transporttn.app.fragments;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.transporttn.app.R;
import com.transporttn.app.activities.TicketDetailActivity;
import com.transporttn.app.adapters.LigneAdapter;
import com.transporttn.app.models.BusTicketRequest;
import com.transporttn.app.models.Ligne;
import com.transporttn.app.models.Ticket;
import com.transporttn.app.network.ApiClient;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Bus Interurbain (cahier des charges section 2.4) : catalogue des lignes,
 * selection de date et quantite, achat -> generation d'un QR code unique.
 */
public class BusFragment extends Fragment {

    private RecyclerView recyclerView;
    private LigneAdapter adapter;
    private TextView selectedDateText;
    private SwipeRefreshLayout swipeRefresh;

    private String selectedDate;
    private final SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.FRANCE);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bus, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewLignes);
        selectedDateText = view.findViewById(R.id.selectedDateText);
        swipeRefresh = view.findViewById(R.id.swipeRefresh);
        MaterialButton pickDateButton = view.findViewById(R.id.pickDateButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new LigneAdapter(this::onLigneSelected);
        recyclerView.setAdapter(adapter);

        Calendar today = Calendar.getInstance();
        selectedDate = isoFormat.format(today.getTime());
        selectedDateText.setText(selectedDate);

        pickDateButton.setOnClickListener(v -> showDatePicker());
        swipeRefresh.setOnRefreshListener(this::loadLignes);

        loadLignes();

        return view;
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(requireContext(), (view, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(year, month, day);
            selectedDate = isoFormat.format(picked.getTime());
            selectedDateText.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void loadLignes() {
        swipeRefresh.setRefreshing(true);
        ApiClient.getApiService(requireContext()).getLignesBus(selectedDate)
                .enqueue(new Callback<List<Ligne>>() {
                    @Override
                    public void onResponse(Call<List<Ligne>> call, Response<List<Ligne>> response) {
                        swipeRefresh.setRefreshing(false);
                        if (response.isSuccessful() && response.body() != null) {
                            adapter.setLignes(response.body());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Ligne>> call, Throwable t) {
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void onLigneSelected(Ligne ligne) {
        NumberPicker picker = new NumberPicker(requireContext());
        picker.setMinValue(1);
        picker.setMaxValue(10);

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Quantite de tickets - " + ligne.getName())
                .setView(picker)
                .setPositiveButton("Acheter", (dialog, which) -> purchaseTicket(ligne, picker.getValue()))
                .setNegativeButton("Annuler", null)
                .show();
    }

    private void purchaseTicket(Ligne ligne, int quantity) {
        BusTicketRequest request = new BusTicketRequest(ligne.getId(), selectedDate, quantity);

        ApiClient.getApiService(requireContext()).purchaseBusTicket(request)
                .enqueue(new Callback<Ticket>() {
                    @Override
                    public void onResponse(Call<Ticket> call, Response<Ticket> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Ticket ticket = response.body();
                            Intent intent = new Intent(getActivity(), TicketDetailActivity.class);
                            intent.putExtra(TicketDetailActivity.EXTRA_QR_CODE, ticket.getQrCode());
                            intent.putExtra(TicketDetailActivity.EXTRA_LIGNE_NAME, ligne.getName());
                            intent.putExtra(TicketDetailActivity.EXTRA_TRAVEL_DATE, selectedDate);
                            intent.putExtra(TicketDetailActivity.EXTRA_QUANTITY, quantity);
                            startActivity(intent);
                        } else {
                            Toast.makeText(getContext(), R.string.error_generic, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Ticket> call, Throwable t) {
                        Toast.makeText(getContext(), R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
