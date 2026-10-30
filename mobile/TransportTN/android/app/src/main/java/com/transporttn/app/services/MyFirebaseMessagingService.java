package com.transporttn.app.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.transporttn.app.R;
import com.transporttn.app.activities.MainActivity;
import com.transporttn.app.models.FcmTokenRequest;
import com.transporttn.app.network.ApiClient;
import com.transporttn.app.network.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Reception des notifications push FCM (cahier des charges section 2, module "Notifications Push") :
 * assignation chauffeur, statut de course, confirmation collectif, achat de ticket, annulation.
 *
 * SEUL point d'usage de Firebase cote Android (aucune authentification Firebase).
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "transporttn_notifications";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getNotification() != null
                ? remoteMessage.getNotification().getTitle() : "TransportTN";
        String body = remoteMessage.getNotification() != null
                ? remoteMessage.getNotification().getBody() : "";

        showNotification(title, body);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        // Si l'utilisateur est deja connecte, on met a jour son token FCM cote backend
        // (le token peut changer, ex: reinstallation de l'app).
        TokenManager tokenManager = new TokenManager(this);
        if (tokenManager.isLoggedIn()) {
            ApiClient.getApiService(this).updateFcmToken(new FcmTokenRequest(token))
                    .enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) { }
                        @Override
                        public void onFailure(Call<Void> call, Throwable t) { }
                    });
        }
    }

    private void showNotification(String title, String body) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "TransportTN", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
