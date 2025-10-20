package com.capacidad.validationapi.config.security;

import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.Collection;
import java.util.UUID;

@Getter
public class JWTAuthenticationToken extends AbstractAuthenticationToken implements Serializable {

    private static final long serialVersionUID = 141278127489124L;

    private final String principal;
    private final UUID resourceId;
    private final UUID sub;
    private final String clientId;
    private final String token;

    public JWTAuthenticationToken(@NonNull String principal,
                                  Collection<? extends GrantedAuthority> authorities,
                                  UUID resourceId,
                                  String clientId,
                                  UUID sub,
                                  String token) {
        super(authorities);
        if (authorities != null && !authorities.isEmpty())
            super.setAuthenticated(true);
        this.principal = principal;
        this.resourceId = resourceId;
        this.clientId = clientId;
        this.sub = sub;
        this.token = token;
    }

    public Object getCredentials() {
        return null;
    }

}
