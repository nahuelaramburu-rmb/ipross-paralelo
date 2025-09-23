package com.capacidad.identityservice.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import javax.crypto.spec.SecretKeySpec;

@Configuration
public class JwtConfig {


    @Value("${jwt.secret}")
    private String secretKey;

    @Bean
    public JwtEncoder jwtEncoder() {
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256");
        return new NimbusJwtEncoder(new ImmutableSecret<>(keySpec));
    }
}
