package com.capacidad.identityservice.config.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

@Configuration
@RequiredArgsConstructor
public class RedisAuthorizationServiceConfig {

    private final RedisTemplate<String, OAuth2Authorization> redisTemplate;

    @Bean
    public OAuth2AuthorizationService authorizationService() {
        return new RedisOAuth2AuthorizationService(redisTemplate);
    }

    /**
     * Implementación personalizada de OAuth2AuthorizationService usando Redis
     */
    public static class RedisOAuth2AuthorizationService implements OAuth2AuthorizationService {

        private final RedisTemplate<String, OAuth2Authorization> redisTemplate;

        public RedisOAuth2AuthorizationService(RedisTemplate<String, OAuth2Authorization> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        @Override
        public void save(OAuth2Authorization authorization) {
            redisTemplate.opsForValue().set(authorization.getId(), authorization);
        }

        @Override
        public void remove(OAuth2Authorization authorization) {
            redisTemplate.delete(authorization.getId());
        }

        @Override
        public OAuth2Authorization findById(String id) {
            return redisTemplate.opsForValue().get(id);
        }

        @Override
        public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
            // Obtener todas las claves
            var keys = redisTemplate.keys("*");
            if (keys == null) return null;

            for (String key : keys) {
                OAuth2Authorization auth = redisTemplate.opsForValue().get(key);
                if (auth == null) continue;

                // Buscar token de acceso
                var accessToken = auth.getAccessToken();
                if (accessToken != null
                        && (tokenType == null || OAuth2TokenType.ACCESS_TOKEN.equals(tokenType))
                        && token.equals(accessToken.getToken().getTokenValue())) {
                    return auth;
                }

                // Buscar refresh token
                var refreshToken = auth.getRefreshToken();
                if (refreshToken != null
                        && (tokenType == null || OAuth2TokenType.REFRESH_TOKEN.equals(tokenType))
                        && token.equals(refreshToken.getToken().getTokenValue())) {
                    return auth;
                }
            }
            return null;
        }

    }
}