package com.capacidad.identityservice.misc;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

// mapea una lista de strings de authorities , a una lista de SimpleGrantedAuthority

@Component
public class AuthorityMapper {

    public static Collection<? extends GrantedAuthority> mapUserAuthorities(List<String> authorities) {
        return authorities.stream()
                .map(SimpleGrantedAuthority::new) // cada String -> SimpleGrantedAuthority
                .collect(Collectors.toList());
    }
}
