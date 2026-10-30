package com.transporttn.app.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.transporttn.app.R;

/**
 * Ecran 7b du parcours utilisateur : affichage du ticket bus avec QR code.
 * Le QR code encode la valeur "qrCode" generee par le backend (UUID base64, section 2.4).
 */
public class TicketDetailActivity extends AppCompatActivity {

    public static final String EXTRA_QR_CODE = "qr_code";
    public static final String EXTRA_LIGNE_NAME = "ligne_name";
    public static final String EXTRA_TRAVEL_DATE = "travel_date";
    public static final String EXTRA_QUANTITY = "quantity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_detail);

        ImageView qrImage = findViewById(R.id.qrImageView);
        TextView ligneText = findViewById(R.id.ligneNameText);
        TextView dateText = findViewById(R.id.travelDateText);
        TextView quantityText = findViewById(R.id.quantityText);

        String qrCode = getIntent().getStringExtra(EXTRA_QR_CODE);
        ligneText.setText(getIntent().getStringExtra(EXTRA_LIGNE_NAME));
        dateText.setText(getIntent().getStringExtra(EXTRA_TRAVEL_DATE));
        quantityText.setText(String.valueOf(getIntent().getIntExtra(EXTRA_QUANTITY, 1)));

        if (qrCode != null) {
            try {
                BarcodeEncoder encoder = new BarcodeEncoder();
                Bitmap bitmap = encoder.encodeBitmap(qrCode, BarcodeFormat.QR_CODE, 600, 600);
                qrImage.setImageBitmap(bitmap);
            } catch (WriterException e) {
                // En cas d'erreur de generation, le QR reste vide ; le ticket demeure
                // consultable via son code texte pour verification manuelle.
            }
        }
    }
}
