package com.capacidad.identityservice.config.security;

import com.capacidad.identityservice.model.Group;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@EqualsAndHashCode(callSuper = true)
@Getter
public class BaseAuthenticationToken extends AbstractAuthenticationToken {

    private final transient Object principal;
    private final transient Object credentials;
    private final Group group;

    BaseAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities, Group group) {
        super(authorities);
        if (authorities != null && !authorities.isEmpty())
            super.setAuthenticated(true);
        this.group = group;
        this.principal = principal;
        this.credentials = credentials;
    }

    public Object getCredentials() {
        return null;
    }

}
