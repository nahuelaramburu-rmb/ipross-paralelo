package com.capacidad.identityservice.config.security;

import com.capacidad.identityservice.model.Group;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

import java.util.Collection;

@EqualsAndHashCode(callSuper = true)
@Getter
public class ClientAuthenticationToken extends BaseAuthenticationToken {
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

    public ClientAuthenticationToken(Object principal, Object credentials,
                                     Collection<? extends GrantedAuthority> authorities, Group group) {
        super(principal, credentials, authorities, group);
    }

}