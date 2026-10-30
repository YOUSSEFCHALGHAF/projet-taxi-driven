package com.transporttn.app;

import android.app.Application;

public class TransportTnApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // Point d'initialisation global (Firebase s'auto-initialise via google-services.json)
    }
}
