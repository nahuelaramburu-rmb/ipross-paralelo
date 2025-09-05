package com.capacidad.identityservice.config.oauth;

import com.capacidad.identityservice.config.token.TokenBuilder;
import com.capacidad.identityservice.model.CustomUserDetails;
import com.capacidad.identityservice.service.impl.CustomUserDetailsService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.*;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.TokenRequest;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import java.util.Date;
import java.util.UUID;

import static com.capacidad.identityservice.misc.constant.SecurityConstants.TENANT_ID_HEADER;

@Log4j2
public class CustomTokenServices extends BaseTokenServices {

    private final TokenStore tokenStore;
    private final TokenBuilder tokenBuilder;
    private final CustomUserDetailsService customUserDetailsService;


    public CustomTokenServices(TokenStore tokenStore,
                               TokenBuilder tokenBuilder,
                               ClientDetailsService clientDetailsService,
                               CustomUserDetailsService customUserDetailsService) {
        super();
        this.tokenBuilder = tokenBuilder;
        this.customUserDetailsService = customUserDetailsService;
        this.tokenStore = tokenStore;
        setTokenStore(tokenStore);
        setClientDetailsService(clientDetailsService);
    }

    @Override
    public OAuth2AccessToken refreshAccessToken(String refreshTokenValue, TokenRequest tokenRequest) throws AuthenticationException {
        OAuth2Authentication authentication;
        if (!this.isSupportRefreshToken())
            throw new InvalidGrantException("Invalid refresh token: " + refreshTokenValue);
        OAuth2RefreshToken refreshToken = tokenStore.readRefreshToken(refreshTokenValue);
        if (refreshToken == null)
            throw new InvalidGrantException("Invalid refresh token: " + refreshTokenValue);
        else
            authentication = tokenStore.readAuthenticationForRefreshToken(refreshToken);
        if (this.getAuthenticationManager() != null && !authentication.isClientOnly()) {
            // The client has already been authenticated, but the user authentication might be old now, so give it a
            // chance to re-authenticate.
            Authentication user = new PreAuthenticatedAuthenticationToken(authentication.getUserAuthentication(), "", authentication.getAuthorities());
            user = this.getAuthenticationManager().authenticate(user);
            Object details = authentication.getDetails();
            authentication = new OAuth2Authentication(authentication.getOAuth2Request(), user);
            authentication.setDetails(details);
        }
        String clientId = authentication.getOAuth2Request().getClientId();
        if (clientId == null || !clientId.equals(tokenRequest.getClientId()))
            throw new InvalidGrantException("Wrong client for this refresh token: " + refreshTokenValue);
        // clear out any access tokens already associated with the refresh
        // token.
        tokenStore.removeAccessTokenUsingRefreshToken(refreshToken);
        if (isExpired(refreshToken)) {
            tokenStore.removeRefreshToken(refreshToken);
            throw new InvalidTokenException("Invalid refresh token (expired): " + refreshToken);
        }
        authentication = createRefreshedAuthentication(authentication, tokenRequest);
        if (!this.reuseRefreshToken()) {
            tokenStore.removeRefreshToken(refreshToken);
            refreshToken = customCreateRefreshToken(authentication);
        }
        // refresh user data
        UserDetails refreshedUserDetails = customUserDetailsService.loadUserByUsername(authentication.getName());
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
        customUserDetails.setApplicationUser(((CustomUserDetails) refreshedUserDetails).getApplicationUser());
        OAuth2AccessToken accessToken = customCreateAccessToken(authentication, refreshToken);
        tokenStore.storeAccessToken(accessToken, authentication);
        if (!this.reuseRefreshToken())
            tokenStore.storeRefreshToken(accessToken.getRefreshToken(), authentication);
        return accessToken;
    }

    @Override
    public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication) {

        if (!hasPromptNoneParam(authentication)) {
            return normallyCreateAccessToken(authentication);
        }

        OAuth2AccessToken existingAccessToken = tokenStore.getAccessToken(authentication);
        if (existingAccessToken != null)
            tokenStore.removeAccessToken(existingAccessToken);
        OAuth2AccessToken accessToken = customCreateAccessToken(authentication, null);
        tokenStore.storeAccessToken(accessToken, authentication);

        return accessToken;
    }

    private OAuth2AccessToken normallyCreateAccessToken(OAuth2Authentication authentication) {
        OAuth2AccessToken existingAccessToken = tokenStore.getAccessToken(authentication);
        OAuth2RefreshToken refreshToken = null;
        if (existingAccessToken != null) {
            if (existingAccessToken.getRefreshToken() != null) {
                tokenStore.removeRefreshToken(existingAccessToken.getRefreshToken());
                refreshToken = createAndStoreRefreshToken(authentication);
                if (existingAccessToken instanceof DefaultOAuth2AccessToken)
                    ((DefaultOAuth2AccessToken) existingAccessToken).setRefreshToken(refreshToken);
            }
            if (existingAccessToken.isExpired())
                tokenStore.removeAccessToken(existingAccessToken);
            else {
                // Re-store the access token in case the authentication has changed
                if (!authentication.getOAuth2Request().getRequestParameters().containsKey(TENANT_ID_HEADER)) {
                    tokenStore.storeAccessToken(existingAccessToken, authentication);
                    return existingAccessToken;
                }
                tokenStore.removeAccessToken(existingAccessToken);
            }
        }

        if (refreshToken == null)
            refreshToken = createAndStoreRefreshToken(authentication);

        OAuth2AccessToken accessToken = customCreateAccessToken(authentication, refreshToken);
        tokenStore.storeAccessToken(accessToken, authentication);
        return accessToken;
    }

    private OAuth2RefreshToken createAndStoreRefreshToken(OAuth2Authentication authentication) {
        OAuth2RefreshToken refreshToken = customCreateRefreshToken(authentication);
        if (refreshToken != null)
            tokenStore.storeRefreshToken(refreshToken, authentication);
        return refreshToken;
    }

    private OAuth2AccessToken customCreateAccessToken(OAuth2Authentication authentication, OAuth2RefreshToken refreshToken) {
        OAuth2AccessToken token = tokenBuilder.buildAccessToken(authentication);
        if (token instanceof DefaultOAuth2AccessToken) {
            DefaultOAuth2AccessToken defaultToken = (DefaultOAuth2AccessToken) token;
            defaultToken.setRefreshToken(refreshToken);
        }
        return (this.getAccessTokenEnhancer() != null ? this.getAccessTokenEnhancer().enhance(token, authentication) : token);
    }

    private OAuth2RefreshToken customCreateRefreshToken(OAuth2Authentication authentication) {
        if (!isSupportRefreshToken(authentication.getOAuth2Request()))
            return null;
        int validitySeconds = getRefreshTokenValiditySeconds(authentication.getOAuth2Request());
        String value = UUID.randomUUID().toString();
        if (validitySeconds > 0)
            return new DefaultExpiringOAuth2RefreshToken(value, new Date(System.currentTimeMillis()
                    + (validitySeconds * 1000L)));
        return new DefaultOAuth2RefreshToken(value);
    }

    private boolean hasPromptNoneParam(OAuth2Authentication authentication) {
        return StringUtils.equals("none", authentication.getOAuth2Request().getRequestParameters().get("prompt"));
    }

}
