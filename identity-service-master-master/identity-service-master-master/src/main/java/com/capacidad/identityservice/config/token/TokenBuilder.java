package com.capacidad.identityservice.config.token;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

public interface TokenBuilder {

    OAuth2AccessToken buildAccessToken(OAuth2Authentication oAuth2Authentication);

}
