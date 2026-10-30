package tn.transporttn.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tn.transporttn.exception.ApiErreur;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest requete,
                         HttpServletResponse reponse,
                         AuthenticationException exception) throws IOException {

        Object erreurJwt = requete.getAttribute(JwtAuthenticationFilter.ATTRIBUT_ERREUR_JWT);
        String message = erreurJwt != null ? erreurJwt.toString() : "Authentification requise";

        reponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        reponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        reponse.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(reponse.getOutputStream(),
                ApiErreur.of(HttpStatus.UNAUTHORIZED, message, requete.getRequestURI()));
    }
}