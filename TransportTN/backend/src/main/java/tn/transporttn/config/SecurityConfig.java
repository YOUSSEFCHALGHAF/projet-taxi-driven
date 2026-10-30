package tn.transporttn.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import tn.transporttn.security.JwtAuthenticationFilter;
import tn.transporttn.security.RestAccessDeniedHandler;
import tn.transporttn.security.RestAuthenticationEntryPoint;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity            // active @PreAuthorize dans les contrôleurs (étape 4)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;
    private final RestAccessDeniedHandler accessDeniedHandler;
    private final CorsProperties corsProperties;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(Customizer.withDefaults())                       // utilise corsConfigurationSource()
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .exceptionHandling(e -> e
                    .authenticationEntryPoint(authenticationEntryPoint)   // 401
                    .accessDeniedHandler(accessDeniedHandler))            // 403
            .authorizeHttpRequests(auth -> auth
                    // Requêtes de pré-vérification CORS envoyées par le navigateur
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // Routes publiques
                    .requestMatchers(HttpMethod.POST, "/api/auth/inscription", "/api/auth/login").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/lignes/**").permitAll()   // catalogue visible sans compte
                    .requestMatchers("/error").permitAll()
                    // Connexion WebSocket : l'authentification est faite sur la trame STOMP CONNECT
                    .requestMatchers("/ws", "/ws/**").permitAll()
                    // Routes par rôle
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/driver/**").hasRole("DRIVER")
                    .requestMatchers("/api/reservations/**").hasRole("CLIENT")
                    // Tout le reste : être connecté (règles plus fines avec @PreAuthorize)
                    .anyRequest().authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** BCrypt : hachage lent et salé, adapté aux mots de passe. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Utilisé par AuthService pour vérifier téléphone + mot de passe. */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /** Autorise le frontend React (Vite) à appeler l'API depuis un autre port / domaine. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(corsProperties.allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(false);   // le jeton passe dans un en-tête, pas dans un cookie
        config.setMaxAge(3600L);             // le navigateur garde la réponse OPTIONS 1 heure

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    /**
     * JwtAuthenticationFilter est un @Component : Spring Boot l'enregistrerait aussi
     * comme filtre servlet global. On désactive cet enregistrement pour qu'il ne
     * s'exécute qu'une fois, à sa place dans la chaîne Spring Security.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> desactiverEnregistrementAutomatique(
            JwtAuthenticationFilter filtre) {
        FilterRegistrationBean<JwtAuthenticationFilter> enregistrement = new FilterRegistrationBean<>(filtre);
        enregistrement.setEnabled(false);
        return enregistrement;
    }
}