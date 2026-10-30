package com.transporttn.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.transporttn.app.R;
import com.transporttn.app.models.OtpRequest;
import com.transporttn.app.network.ApiClient;

import java.util.Map;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Ecran 2 du parcours utilisateur : saisie du numero de telephone (+216XXXXXXXX)
 * puis demande d'un code OTP au backend (POST /api/auth/otp/request).
 */
public class LoginActivity extends AppCompatActivity {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+216[0-9]{8}$");

    private TextInputEditText phoneInput;
    private MaterialButton sendCodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneInput = findViewById(R.id.phoneInput);
        sendCodeButton = findViewById(R.id.sendCodeButton);

        sendCodeButton.setOnClickListener(v -> sendOtp());
    }

    private void sendOtp() {
        String phone = phoneInput.getText() != null ? phoneInput.getText().toString().trim() : "";

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            phoneInput.setError(getString(R.string.phone_hint));
            return;
        }

        sendCodeButton.setEnabled(false);

        ApiClient.getApiService(this).requestOtp(new OtpRequest(phone))
                .enqueue(new Callback<Map<String, Object>>() {
                    @Override
                    public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                        sendCodeButton.setEnabled(true);
                        if (response.isSuccessful()) {
                            Intent intent = new Intent(LoginActivity.this, OtpActivity.class);
                            intent.putExtra("phone", phone);
                            startActivity(intent);
                        } else {
                            Toast.makeText(LoginActivity.this, R.string.error_generic, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                        sendCodeButton.setEnabled(true);
                        Toast.makeText(LoginActivity.this, R.string.error_network, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
