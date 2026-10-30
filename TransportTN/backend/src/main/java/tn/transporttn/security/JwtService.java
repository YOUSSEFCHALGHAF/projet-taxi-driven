package tn.transporttn.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import tn.transporttn.config.JwtProperties;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey cle;
    private final long expirationMs;

    public JwtService(JwtProperties proprietes) {
        byte[] octets = Decoders.BASE64.decode(proprietes.secret());
        if (octets.length < 32) {
            throw new IllegalStateException("app.jwt.secret doit faire au moins 256 bits (32 octets)");
        }
        this.cle = Keys.hmacShaKeyFor(octets);
        this.expirationMs = proprietes.expirationMs();
    }

    public String genererToken(UtilisateurPrincipal utilisateur) {
        Date maintenant = new Date();
        return Jwts.builder()
                .subject(utilisateur.getUsername())                  // téléphone
                .claim("uid", utilisateur.getId())
                .claim("role", utilisateur.getRole().name())
                .issuedAt(maintenant)
                .expiration(new Date(maintenant.getTime() + expirationMs))
                .signWith(cle)
                .compact();
    }

    /**
     * Vérifie la signature et l'expiration puis renvoie le contenu du jeton.
     *
     * @throws io.jsonwebtoken.ExpiredJwtException si le jeton a expiré
     * @throws JwtException si le jeton est falsifié ou mal formé
     */
    public Claims validerEtLire(String token) {
        return Jwts.parser()
                .verifyWith(cle)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraireTelephone(String token) {
        return validerEtLire(token).getSubject();
    }

    public long getExpirationSecondes() {
        return expirationMs / 1000;
    }
}