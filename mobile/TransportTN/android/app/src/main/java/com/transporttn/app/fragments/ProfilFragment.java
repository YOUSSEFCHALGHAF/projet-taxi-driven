package com.transporttn.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.transporttn.app.R;
import com.transporttn.app.activities.LoginActivity;
import com.transporttn.app.network.TokenManager;

/** Gestion du profil utilisateur (cahier des charges module P2 "Gestion profil"). */
public class ProfilFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profil, container, false);

        TokenManager tokenManager = new TokenManager(requireContext());

        TextView nameText = view.findViewById(R.id.profileNameText);
        TextView roleText = view.findViewById(R.id.profileRoleText);
        MaterialButton logoutButton = view.findViewById(R.id.logoutButton);

        nameText.setText(tokenManager.getUserName());
        roleText.setText(tokenManager.getUserRole());

        logoutButton.setOnClickListener(v -> {
            tokenManager.clearSession();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }
}
