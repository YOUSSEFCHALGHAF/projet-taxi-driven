package tn.transporttn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Lit le bloc app.jwt de application.yml. */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(String secret, long expirationMs) {
}