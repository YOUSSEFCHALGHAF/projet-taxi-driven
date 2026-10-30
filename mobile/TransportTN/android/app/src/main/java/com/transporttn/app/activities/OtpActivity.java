package com.transporttn.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.messaging.FirebaseMessaging;
import com.transporttn.app.R;
import com.transporttn.app.models.AuthResponse;
import com.transporttn.app.models.FcmTokenRequest;
import com.transporttn.app.models.OtpRequest;
import com.transporttn.app.models.OtpVerifyRequest;
import com.transporttn.app.network.ApiClient;
import com.transporttn.app.network.TokenManager;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Ecran 3 du parcours utilisateur : verification du code OTP recu par SMS
 * (POST /api/auth/otp/verify) -> recuperation du JWT applicatif -> enregistrement
 * du token FCM pour les notifications push.
 */
public class OtpActivity extends AppCompatActivity {

    private TextInputEditText otpInput;
    private TextInputEditText nameInput;
    private MaterialButton verifyButton;
    private MaterialButton resendButton;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        phone = getIntent().getStringExtra("phone");

        otpInput = findViewById(R.id.otpInput);
        nameInput = findViewById(R.id.nameInput);
        verifyButton = findViewById(R.id.verifyButton);
        resendButton = findViewById(R.id.resendButton);

        verifyButton.setOnClickListener(v -> verifyOtp());
        resendButton.setOnClickListener(v -> resendOtp());
    }

    private void verifyOtp() {
        String code = otpInput.getText() != null ? otpInput.getText().toString().trim() : "";
        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : "";

        if (code.length() != 6) {
            otpInput.setError(getString(R.string.otp_hint));
            return;
        }

        verifyButton.setEnabled(false);

        OtpVerifyRequest request = new OtpVerifyRequest(phone, code, name.isEmpty() ? null : name);

        ApiClient.getApiService(this).verifyOtp(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                verifyButton.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse auth = response.body();
                    TokenManager tokenManager = new TokenManager(OtpActivity.this);
                    tokenManager.saveSession(auth.getToken(), auth.getUserId(), auth.getName(),
                            auth.getPhone(), auth.getRole());

                    registerFcmToken();

                    Intent intent = new Intent(OtpActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(OtpActivity.this, "Code invalide ou nom requis pour l'inscription",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                verifyButton.setEnabled(true);
                Toast.makeText(OtpActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Recupere le token FCM de l'appareil et l'enregistre cote backend pour les notifications push. */
    private void registerFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(Task<String> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String fcmToken = task.getResult();
                            ApiClient.getApiService(OtpActivity.this)
                                    .updateFcmToken(new FcmTokenRequest(fcmToken))
                                    .enqueue(new Callback<Void>() {
                                        @Override
                                        public void onResponse(Call<Void> call, Response<Void> response) { }
                                        @Override
                                        public void onFailure(Call<Void> call, Throwable t) { }
                                    });
                        }
                    }
                });
    }

    private void resendOtp() {
        ApiClient.getApiService(this).requestOtp(new OtpRequest(phone))
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        Toast.makeText(OtpActivity.this, "Code renvoye", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        Toast.makeText(OtpActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
