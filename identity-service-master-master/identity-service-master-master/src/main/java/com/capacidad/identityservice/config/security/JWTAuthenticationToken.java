package com.capacidad.identityservice.config.security;

import com.capacidad.identityservice.model.Group;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

import java.util.Collection;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Getter
public class JWTAuthenticationToken extends BaseAuthenticationToken {

    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;
    private final UUID resourceId;

    public JWTAuthenticationToken(Object principal, Object credentials,
                                  Collection<? extends GrantedAuthority> authorities, Group group, UUID resourceId) {
        super(principal, credentials, authorities, group);
        this.resourceId = resourceId;
    }

}
