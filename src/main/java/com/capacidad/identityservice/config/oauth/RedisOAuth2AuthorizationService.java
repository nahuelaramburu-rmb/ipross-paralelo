package com.capacidad.identityservice.config.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;

import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RedisOAuth2AuthorizationService implements OAuth2AuthorizationService {

    private final RedisTemplate<String, OAuth2Authorization> redisOAuth2AuthorizationTemplate;
    private static final String PREFIX = "oauth2:auth:";

    @Override
    public void save(OAuth2Authorization authorization) {
        redisOAuth2AuthorizationTemplate.opsForValue()
                .set(PREFIX + authorization.getId(), authorization, Duration.ofHours(12));
    }

    @Override
    public void remove(OAuth2Authorization authorization) {
        redisOAuth2AuthorizationTemplate.delete(PREFIX + authorization.getId());
    }

    @Override
    public OAuth2Authorization findById(String id) {
        return redisOAuth2AuthorizationTemplate.opsForValue().get(PREFIX + id);
    }

    @Override
    public OAuth2Authorization findByToken(String token, OAuth2TokenType tokenType) {
        Set<String> keys = redisOAuth2AuthorizationTemplate.keys(PREFIX + "*");
        if (keys == null) return null;

        for (String key : keys) {
            OAuth2Authorization auth = redisOAuth2AuthorizationTemplate.opsForValue().get(key);
            if (auth == null) continue;

            if (auth.getAccessToken() != null &&
                    (tokenType == null || OAuth2TokenType.ACCESS_TOKEN.equals(tokenType)) &&
                    token.equals(auth.getAccessToken().getToken().getTokenValue())) {
                return auth;
            }

            if (auth.getRefreshToken() != null &&
                    (tokenType == null || OAuth2TokenType.REFRESH_TOKEN.equals(tokenType)) &&
                    token.equals(auth.getRefreshToken().getToken().getTokenValue())) {
                return auth;
            }
        }
        return null;
    }
}
