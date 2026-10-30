package com.transporttn.app.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.transporttn.app.R;

/** Ecran d'accueil : choix entre Taxi / Collectif / Bus (cahier des charges section 4.1, etape 4). */
public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        view.findViewById(R.id.cardTaxi).setOnClickListener(v ->
                navigateTo(new TaxiFragment()));
        view.findViewById(R.id.cardCollectif).setOnClickListener(v ->
                navigateTo(new CollectifFragment()));
        view.findViewById(R.id.cardBus).setOnClickListener(v ->
                navigateTo(new BusFragment()));

        return view;
    }

    private void navigateTo(Fragment fragment) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }
}
