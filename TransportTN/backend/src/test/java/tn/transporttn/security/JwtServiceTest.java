package tn.transporttn.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import tn.transporttn.config.JwtProperties;
import tn.transporttn.model.Utilisateur;
import tn.transporttn.model.enums.Role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET_TEST =
            "okBlPsLQWssUvcmP6zqLWAEfvzK1FLlSOxw5Z31l6bwkpp4HJmZXkGs6QeV7sa3LI978ps+nygv0zMKLfo7Bww==";

    private final UtilisateurPrincipal client = UtilisateurPrincipal.depuis(Utilisateur.builder()
            .id(42L).nom("Ben Salah").prenom("Amira").telephone("+21622111222")
            .motDePasse("hash").role(Role.CLIENT).build());

    @Test
    void genererPuisLireUnJeton() {
        JwtService service = new JwtService(new JwtProperties(SECRET_TEST, 60_000));

        Claims claims = service.validerEtLire(service.genererToken(client));

        assertThat(claims.getSubject()).isEqualTo("+21622111222");
        assertThat(claims.get("uid", Long.class)).isEqualTo(42L);
        assertThat(claims.get("role", String.class)).isEqualTo("CLIENT");
    }

    @Test
    void jetonFalsifieRefuse() {
        JwtService service = new JwtService(new JwtProperties(SECRET_TEST, 60_000));
        String jeton = service.genererToken(client);
        String falsifie = jeton.substring(0, jeton.length() - 4) + "AAAA";

        assertThatThrownBy(() -> service.validerEtLire(falsifie)).isInstanceOf(JwtException.class);
    }

    @Test
    void jetonExpireRefuse() {
        JwtService service = new JwtService(new JwtProperties(SECRET_TEST, -1_000));   // déjà expiré
        String jeton = service.genererToken(client);

        assertThatThrownBy(() -> service.validerEtLire(jeton)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void cleTropCourteRefuseeAuDemarrage() {
        assertThatThrownBy(() -> new JwtService(new JwtProperties("Y291cnRl", 60_000)))   // "courte"
                .isInstanceOf(IllegalStateException.class);
    }
}