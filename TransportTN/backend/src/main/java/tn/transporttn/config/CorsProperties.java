package tn.transporttn.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/** Lit le bloc app.cors de application.yml. */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {
}