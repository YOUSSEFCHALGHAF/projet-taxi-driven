package tn.transporttn.service;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class QrCodeServiceTest {

    private final QrCodeService service = new QrCodeService();

    @Test
    void leQrCodeGenereEstLisibleEtContientLeCode() throws Exception {
        String code = UUID.randomUUID().toString();

        String base64 = service.genererPngBase64(QrCodeService.contenuTicket(code));
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(Base64.getDecoder().decode(base64)));

        assertThat(image.getWidth()).isEqualTo(300);
        Result lu = new MultiFormatReader().decode(
                new BinaryBitmap(new HybridBinarizer(new BufferedImageLuminanceSource(image))));
        assertThat(lu.getText()).isEqualTo("TRANSPORTTN:" + code);
    }

    @Test
    void extraireCodeAccepteLesDeuxFormats() {
        assertThat(QrCodeService.extraireCode("TRANSPORTTN:abc-123")).isEqualTo("abc-123");
        assertThat(QrCodeService.extraireCode("  abc-123 ")).isEqualTo("abc-123");
    }
}