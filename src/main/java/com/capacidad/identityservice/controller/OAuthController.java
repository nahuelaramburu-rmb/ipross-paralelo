package com.capacidad.identityservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
public class OAuthController {

    private final OAuth2AuthorizationService authorizationService;

    public OAuthController(OAuth2AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @DeleteMapping("/oauth/token")
    public ResponseEntity<Void> revokeToken(HttpServletRequest request) {
        String tokenValue = extractToken(request).orElse(null);
        if (tokenValue != null) {
            OAuth2Authorization authorization = authorizationService.findByToken(tokenValue, OAuth2TokenType.ACCESS_TOKEN);
            if (authorization != null) {
                authorizationService.remove(authorization);
            }
        }
        return ResponseEntity.noContent().build();
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return Optional.of(header.substring(7));
        }
        return Optional.empty();
    }
}
