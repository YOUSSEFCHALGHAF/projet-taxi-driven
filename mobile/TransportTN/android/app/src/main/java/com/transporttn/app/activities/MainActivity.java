package com.transporttn.app.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.transporttn.app.R;
import com.transporttn.app.fragments.BusFragment;
import com.transporttn.app.fragments.CollectifFragment;
import com.transporttn.app.fragments.HomeFragment;
import com.transporttn.app.fragments.ProfilFragment;
import com.transporttn.app.fragments.TaxiFragment;

/**
 * Ecran 4 (Accueil) et conteneur de la navigation principale.
 * Navigation par BottomNavigationView : Accueil, Taxi, Collectif, Bus, Profil
 * (conformement au cahier des charges section 4.2).
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);

        // Fragment par defaut
        loadFragment(new HomeFragment());

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_taxi) {
                fragment = new TaxiFragment();
            } else if (id == R.id.nav_collectif) {
                fragment = new CollectifFragment();
            } else if (id == R.id.nav_bus) {
                fragment = new BusFragment();
            } else {
                fragment = new ProfilFragment();
            }

            return loadFragment(fragment);
        });
    }

    private boolean loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
        return true;
    }
}
