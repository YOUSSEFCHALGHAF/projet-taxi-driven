package tn.transporttn;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan   // active JwtProperties et CorsProperties
public class TransportTnApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransportTnApplication.class, args);
    }
}