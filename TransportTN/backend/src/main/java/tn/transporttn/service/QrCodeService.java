package tn.transporttn.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;

/** Génère les QR codes des tickets de bus (image PNG encodée en Base64). */
@Service
public class QrCodeService {

    public static final String PREFIXE_TICKET = "TRANSPORTTN:";
    private static final int TAILLE_PIXELS = 300;

    /** Texte encodé dans le QR code d'un ticket. */
    public static String contenuTicket(String codeUnique) {
        return PREFIXE_TICKET + codeUnique;
    }

    /** Accepte le texte scanné complet ou le code seul. */
    public static String extraireCode(String texteScanne) {
        String texte = texteScanne.trim();
        return texte.startsWith(PREFIXE_TICKET) ? texte.substring(PREFIXE_TICKET.length()) : texte;
    }

    public String genererPngBase64(String contenu) {
        Map<EncodeHintType, Object> options = Map.of(
                EncodeHintType.CHARACTER_SET, "UTF-8",
                EncodeHintType.MARGIN, 1,
                EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);   // lisible même un peu abîmé
        try {
            BitMatrix matrice = new QRCodeWriter()
                    .encode(contenu, BarcodeFormat.QR_CODE, TAILLE_PIXELS, TAILLE_PIXELS, options);
            ByteArrayOutputStream sortie = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrice, "PNG", sortie);
            return Base64.getEncoder().encodeToString(sortie.toByteArray());
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("Impossible de générer le QR code", e);
        }
    }
}