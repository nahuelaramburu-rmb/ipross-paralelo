package com.capacidad.identityservice.controller;

import com.capacidad.identityservice.config.oauth.CustomTokenServices;
import com.capacidad.utils.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.endpoint.FrameworkEndpoint;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@FrameworkEndpoint
public class OAuthController {

    private final CustomTokenServices tokenServices;

    @Autowired
    public OAuthController(CustomTokenServices tokenServices) {
        this.tokenServices = tokenServices;
    }

    @DeleteMapping(value = "/oauth/token")
    @ResponseBody
    public ResponseEntity<Object> revokeToken(HttpServletRequest request) {
        String bearerToken = SecurityUtils.getBearerToken(request).orElse("");
        if (StringUtils.isNotEmpty(bearerToken))
            tokenServices.revokeToken(bearerToken);
        return ResponseEntity.noContent().build();
    }
}
