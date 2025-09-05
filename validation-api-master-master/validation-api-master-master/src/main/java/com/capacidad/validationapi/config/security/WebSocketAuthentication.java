package com.capacidad.validationapi.config.security;

import lombok.Getter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

@Getter
public class WebSocketAuthentication extends AbstractAuthenticationToken {
    private static final long serialVersionUID = 141278127489124L;

    private final String principal;
    private final UUID tenantId;
    private final UUID sub;

    public WebSocketAuthentication(@NonNull String principal, Collection<? extends GrantedAuthority> authorities, UUID tenantId, UUID sub) {
        super(authorities);
        if (authorities != null && !authorities.isEmpty())
            super.setAuthenticated(true);
        this.principal = principal;
        this.tenantId = tenantId;
        this.sub = sub;
    }

    @Override
    public Object getCredentials() {
        return null;
    }
}
