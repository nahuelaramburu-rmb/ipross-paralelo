package com.capacidad.identityservice.config.oauth;

import com.capacidad.identityservice.config.token.TokenBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CustomTokenServicesTest {

    @Mock
    private OAuth2Authentication oAuth2Authentication;
    @Mock
    private OAuth2Request oAuth2Request;
    @Mock
    private TokenStore tokenStore;
    @Mock
    private TokenEnhancer accessTokenEnhancer;
    @Mock
    private TokenBuilder tokenBuilder;
    @Spy
    @InjectMocks
    private CustomTokenServices customTokenServices;

    @Test
    public void testCreateAccessTokenReturnsNewEnhancedTokenWithRefreshWhenPromptNotNone() {
        DefaultOAuth2AccessToken defaultOAuth2AccessToken = new DefaultOAuth2AccessToken("token");

        when(oAuth2Authentication.getOAuth2Request()).thenReturn(oAuth2Request);
        when(oAuth2Request.getRequestParameters()).thenReturn(new HashMap<>());
        when(tokenStore.getAccessToken(oAuth2Authentication)).thenReturn(null);
        when(tokenBuilder.buildAccessToken(oAuth2Authentication)).thenReturn(defaultOAuth2AccessToken);
        when(accessTokenEnhancer.enhance(defaultOAuth2AccessToken, oAuth2Authentication)).thenReturn(defaultOAuth2AccessToken);

        customTokenServices.setTokenEnhancer(accessTokenEnhancer);
        customTokenServices.setSupportRefreshToken(true);
        OAuth2AccessToken result = customTokenServices.createAccessToken(oAuth2Authentication);

        assertThat(result.getValue()).isNotBlank();
        assertThat(result.getRefreshToken()).isNotNull();
        assertThat(result.getRefreshToken()).isInstanceOf(DefaultExpiringOAuth2RefreshToken.class);
        verify(tokenStore, times(1)).storeRefreshToken(any(OAuth2RefreshToken.class), any(OAuth2Authentication.class));
        verify(tokenStore, times(1)).storeAccessToken(defaultOAuth2AccessToken, oAuth2Authentication);
        verify(accessTokenEnhancer, times(1)).enhance(defaultOAuth2AccessToken, oAuth2Authentication);
    }

    @Test
    public void testCreateAccessTokenReturnsNewTokenWithRefreshWhenPromptNotNone() {
        DefaultOAuth2AccessToken defaultOAuth2AccessToken = new DefaultOAuth2AccessToken("token");

        when(oAuth2Authentication.getOAuth2Request()).thenReturn(oAuth2Request);
        when(oAuth2Request.getRequestParameters()).thenReturn(new HashMap<>());
        when(tokenStore.getAccessToken(oAuth2Authentication)).thenReturn(null);
        when(tokenBuilder.buildAccessToken(oAuth2Authentication)).thenReturn(defaultOAuth2AccessToken);

        customTokenServices.setTokenEnhancer(null);
        customTokenServices.setSupportRefreshToken(true);
        OAuth2AccessToken result = customTokenServices.createAccessToken(oAuth2Authentication);

        assertThat(result.getValue()).isNotBlank();
        assertThat(result.getRefreshToken()).isNotNull();
        assertThat(result.getRefreshToken()).isInstanceOf(DefaultExpiringOAuth2RefreshToken.class);
        verify(tokenStore, times(1)).storeRefreshToken(any(OAuth2RefreshToken.class), any(OAuth2Authentication.class));
        verify(tokenStore, times(1)).storeAccessToken(defaultOAuth2AccessToken, oAuth2Authentication);
        verify(accessTokenEnhancer, never()).enhance(any(OAuth2AccessToken.class), any(OAuth2Authentication.class));
    }

    @Test
    public void testCreateAccessTokenReturnsNewTokenWith0ValidityRefreshWhenPromptNotNone() {
        DefaultOAuth2AccessToken defaultOAuth2AccessToken = new DefaultOAuth2AccessToken("token");

        when(oAuth2Authentication.getOAuth2Request()).thenReturn(oAuth2Request);
        when(oAuth2Request.getRequestParameters()).thenReturn(new HashMap<>());
        when(tokenStore.getAccessToken(oAuth2Authentication)).thenReturn(null);
        when(tokenBuilder.buildAccessToken(oAuth2Authentication)).thenReturn(defaultOAuth2AccessToken);

        customTokenServices.setTokenEnhancer(null);
        customTokenServices.setSupportRefreshToken(true);
        customTokenServices.setRefreshTokenValiditySeconds(0);
        OAuth2AccessToken result = customTokenServices.createAccessToken(oAuth2Authentication);

        assertThat(result.getValue()).isNotBlank();
        assertThat(result.getRefreshToken()).isNotNull();
        assertThat(result.getRefreshToken()).isNotInstanceOf(DefaultExpiringOAuth2RefreshToken.class);
        verify(tokenStore, times(1)).storeRefreshToken(any(OAuth2RefreshToken.class), any(OAuth2Authentication.class));
        verify(tokenStore, times(1)).storeAccessToken(defaultOAuth2AccessToken, oAuth2Authentication);
        verify(accessTokenEnhancer, never()).enhance(any(OAuth2AccessToken.class), any(OAuth2Authentication.class));
    }

    @Test
    public void testCreateAccessTokenReturnsNewTokenWithoutRefreshWhenPromptNotNone() {
        DefaultOAuth2AccessToken defaultOAuth2AccessToken = new DefaultOAuth2AccessToken("token");

        when(oAuth2Authentication.getOAuth2Request()).thenReturn(oAuth2Request);
        when(oAuth2Request.getRequestParameters()).thenReturn(new HashMap<>());
        when(tokenStore.getAccessToken(oAuth2Authentication)).thenReturn(null);
        when(tokenBuilder.buildAccessToken(oAuth2Authentication)).thenReturn(defaultOAuth2AccessToken);

        customTokenServices.setTokenEnhancer(null);
        customTokenServices.setSupportRefreshToken(false);
        OAuth2AccessToken result = customTokenServices.createAccessToken(oAuth2Authentication);

        assertThat(result.getValue()).isNotBlank();
        assertThat(result.getRefreshToken()).isNull();
        verify(tokenStore, never()).storeRefreshToken(any(OAuth2RefreshToken.class), any(OAuth2Authentication.class));
        verify(tokenStore, times(1)).storeAccessToken(defaultOAuth2AccessToken, oAuth2Authentication);
        verify(accessTokenEnhancer, never()).enhance(any(OAuth2AccessToken.class), any(OAuth2Authentication.class));
    }

    @Test
    public void testCreateAccessTokenReturnsNewTokenWithoutRefreshWhenTokenIsNotDefaultInstanceAndPromptNotNone() {
        OAuth2AccessToken oAuth2AccessToken = mock(OAuth2AccessToken.class);

        when(oAuth2Authentication.getOAuth2Request()).thenReturn(oAuth2Request);
        when(oAuth2Request.getRequestParameters()).thenReturn(new HashMap<>());
        when(tokenStore.getAccessToken(oAuth2Authentication)).thenReturn(null);
        when(tokenBuilder.buildAccessToken(oAuth2Authentication)).thenReturn(oAuth2AccessToken);

        customTokenServices.setTokenEnhancer(null);
        customTokenServices.setSupportRefreshToken(false);
        OAuth2AccessToken result = customTokenServices.createAccessToken(oAuth2Authentication);

        assertThat(result.getRefreshToken()).isNull();
        verify(tokenStore, never()).storeRefreshToken(any(OAuth2RefreshToken.class), any(OAuth2Authentication.class));
        verify(tokenStore, times(1)).storeAccessToken(oAuth2AccessToken, oAuth2Authentication);
        verify(accessTokenEnhancer, never()).enhance(any(OAuth2AccessToken.class), any(OAuth2Authentication.class));
    }

    @Test
    public void testCreateAccessTokenReturnsNotExpiredExistingTokenWithoutRefreshWhenPromptNotNone() {
        DefaultOAuth2AccessToken defaultOAuth2AccessToken = new DefaultOAuth2AccessToken("token");

        when(oAuth2Authentication.getOAuth2Request()).thenReturn(oAuth2Request);
        when(oAuth2Request.getRequestParameters()).thenReturn(new HashMap<>());
        when(tokenStore.getAccessToken(oAuth2Authentication)).thenReturn(defaultOAuth2AccessToken);

        customTokenServices.setTokenEnhancer(null);
        customTokenServices.setSupportRefreshToken(false);
        OAuth2AccessToken result = customTokenServices.createAccessToken(oAuth2Authentication);

        assertThat(result.getRefreshToken()).isNull();
        verify(tokenStore, never()).storeRefreshToken(any(OAuth2RefreshToken.class), any(OAuth2Authentication.class));
        verify(tokenStore, times(1)).storeAccessToken(defaultOAuth2AccessToken, oAuth2Authentication);
        verify(accessTokenEnhancer, never()).enhance(any(OAuth2AccessToken.class), any(OAuth2Authentication.class));
    }

    @Test
    public void testCreateAccessTokenReturnsNotExpiredExistingTokenWithRefreshWhenPromptNotNone() {
        DefaultOAuth2AccessToken defaultOAuth2AccessToken = new DefaultOAuth2AccessToken("token");
        DefaultExpiringOAuth2RefreshToken defaultExpiringOAuth2RefreshToken = new DefaultExpiringOAuth2RefreshToken("value", new Date());
        defaultOAuth2AccessToken.setRefreshToken(defaultExpiringOAuth2RefreshToken);

        when(oAuth2Authentication.getOAuth2Request()).thenReturn(oAuth2Request);
        when(oAuth2Request.getRequestParameters()).thenReturn(new HashMap<>());
        when(tokenStore.getAccessToken(oAuth2Authentication)).thenReturn(defaultOAuth2AccessToken);

        customTokenServices.setTokenEnhancer(null);
        customTokenServices.setSupportRefreshToken(true);
        OAuth2AccessToken result = customTokenServices.createAccessToken(oAuth2Authentication);

        assertThat(result.getRefreshToken()).isNotNull();
        assertThat(result.getRefreshToken()).isNotEqualTo(defaultExpiringOAuth2RefreshToken);
        verify(tokenStore, times(1)).removeRefreshToken(defaultExpiringOAuth2RefreshToken);
        verify(tokenStore, times(1)).storeRefreshToken(any(OAuth2RefreshToken.class), any(OAuth2Authentication.class));
        verify(tokenStore, times(1)).storeAccessToken(defaultOAuth2AccessToken, oAuth2Authentication);
        verify(accessTokenEnhancer, never()).enhance(any(OAuth2AccessToken.class), any(OAuth2Authentication.class));
    }

    @Test
    public void testCreateAccessTokenReturnsNotExpiredExistingTokenWithSameRefreshWhenTokenNotDefaultInstanceAndPromptNotNone() {
        OAuth2AccessToken oAuth2AccessToken = mock(OAuth2AccessToken.class);
        DefaultExpiringOAuth2RefreshToken defaultExpiringOAuth2RefreshToken = new DefaultExpiringOAuth2RefreshToken("value", new Date());

        when(oAuth2AccessToken.getRefreshToken()).thenReturn(defaultExpiringOAuth2RefreshToken);
        when(oAuth2Authentication.getOAuth2Request()).thenReturn(oAuth2Request);
        when(oAuth2Request.getRequestParameters()).thenReturn(new HashMap<>());
        when(tokenStore.getAccessToken(oAuth2Authentication)).thenReturn(oAuth2AccessToken);

        customTokenServices.setTokenEnhancer(null);
        customTokenServices.setSupportRefreshToken(true);
        OAuth2AccessToken result = customTokenServices.createAccessToken(oAuth2Authentication);

        assertThat(result.getRefreshToken()).isNotNull();
        assertThat(result.getRefreshToken()).isEqualTo(defaultExpiringOAuth2RefreshToken);
        verify(tokenStore, times(1)).removeRefreshToken(defaultExpiringOAuth2RefreshToken);
        verify(tokenStore, times(1)).storeRefreshToken(any(OAuth2RefreshToken.class), any(OAuth2Authentication.class));
        verify(tokenStore, times(1)).storeAccessToken(oAuth2AccessToken, oAuth2Authentication);
        verify(accessTokenEnhancer, never()).enhance(any(OAuth2AccessToken.class), any(OAuth2Authentication.class));
    }

    @Test
    public void testCreateAccessTokenReturnsExpiredExistingTokenWithRefreshWhenPromptNotNone() {
        DefaultOAuth2AccessToken defaultOAuth2AccessToken = new DefaultOAuth2AccessToken("token");
        defaultOAuth2AccessToken.setExpiration(new Date(System.currentTimeMillis() - 10000));
        DefaultExpiringOAuth2RefreshToken defaultExpiringOAuth2RefreshToken = new DefaultExpiringOAuth2RefreshToken("value", new Date());
        defaultOAuth2AccessToken.setRefreshToken(defaultExpiringOAuth2RefreshToken);

        DefaultOAuth2AccessToken newAccessToken = new DefaultOAuth2AccessToken("new_token");

        when(oAuth2Authentication.getOAuth2Request()).thenReturn(oAuth2Request);
        when(oAuth2Request.getRequestParameters()).thenReturn(new HashMap<>());
        when(tokenStore.getAccessToken(oAuth2Authentication)).thenReturn(defaultOAuth2AccessToken);
        when(tokenBuilder.buildAccessToken(oAuth2Authentication)).thenReturn(newAccessToken);

        customTokenServices.setTokenEnhancer(null);
        customTokenServices.setSupportRefreshToken(true);
        OAuth2AccessToken result = customTokenServices.createAccessToken(oAuth2Authentication);

        assertThat(result.getRefreshToken()).isNotNull();
        assertThat(result.getRefreshToken()).isNotEqualTo(defaultExpiringOAuth2RefreshToken);
        assertThat(result.getValue()).isEqualTo(newAccessToken.getValue());
        verify(tokenStore, times(1)).removeAccessToken(defaultOAuth2AccessToken);
        verify(tokenStore, times(1)).removeRefreshToken(defaultExpiringOAuth2RefreshToken);
        verify(tokenStore, times(1)).storeRefreshToken(any(OAuth2RefreshToken.class), any(OAuth2Authentication.class));
        verify(tokenStore, times(1)).storeAccessToken(newAccessToken, oAuth2Authentication);
        verify(accessTokenEnhancer, never()).enhance(any(OAuth2AccessToken.class), any(OAuth2Authentication.class));
    }

    @Test
    public void testCreateAccessTokenReturnsNewTokenWithoutRefreshWhenPromptIsNone() {
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("prompt", "none");

        DefaultOAuth2AccessToken defaultOAuth2AccessToken = new DefaultOAuth2AccessToken("token");

        when(oAuth2Authentication.getOAuth2Request()).thenReturn(oAuth2Request);
        when(oAuth2Request.getRequestParameters()).thenReturn(requestParameters);
        when(tokenStore.getAccessToken(oAuth2Authentication)).thenReturn(null);
        when(tokenBuilder.buildAccessToken(oAuth2Authentication)).thenReturn(defaultOAuth2AccessToken);

        customTokenServices.setTokenEnhancer(null);
        customTokenServices.setSupportRefreshToken(true);
        OAuth2AccessToken result = customTokenServices.createAccessToken(oAuth2Authentication);

        assertThat(result.getValue()).isNotBlank();
        assertThat(result.getRefreshToken()).isNull();
        verify(tokenStore, never()).removeAccessToken(any(OAuth2AccessToken.class));
        verify(tokenStore, times(1)).storeAccessToken(defaultOAuth2AccessToken, oAuth2Authentication);
        verify(accessTokenEnhancer, never()).enhance(any(OAuth2AccessToken.class), any(OAuth2Authentication.class));
    }

    @Test
    public void testCreateAccessTokenReturnsNewTokenOverridingOldOneWithoutRefreshWhenPromptIsNone() {
        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("prompt", "none");

        DefaultOAuth2AccessToken defaultOAuth2AccessToken = new DefaultOAuth2AccessToken("token");
        DefaultOAuth2AccessToken newAccessToken = new DefaultOAuth2AccessToken("new_token");

        when(oAuth2Authentication.getOAuth2Request()).thenReturn(oAuth2Request);
        when(oAuth2Request.getRequestParameters()).thenReturn(requestParameters);
        when(tokenStore.getAccessToken(oAuth2Authentication)).thenReturn(defaultOAuth2AccessToken);
        when(tokenBuilder.buildAccessToken(oAuth2Authentication)).thenReturn(newAccessToken);

        customTokenServices.setTokenEnhancer(null);
        customTokenServices.setSupportRefreshToken(true);
        OAuth2AccessToken result = customTokenServices.createAccessToken(oAuth2Authentication);

        assertThat(result.getValue()).isEqualTo(newAccessToken.getValue());
        assertThat(result.getRefreshToken()).isNull();
        verify(tokenStore, times(1)).removeAccessToken(defaultOAuth2AccessToken);
        verify(tokenStore, times(1)).storeAccessToken(newAccessToken, oAuth2Authentication);
        verify(accessTokenEnhancer, never()).enhance(any(OAuth2AccessToken.class), any(OAuth2Authentication.class));
    }
}
