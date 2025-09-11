package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.model.ApplicationUser;
import com.capacidad.identityservice.model.ApplicationUserContext;
import com.capacidad.identityservice.model.CustomUserDetails;
import com.capacidad.identityservice.service.ApplicationUserContextService;
import com.capacidad.utils.exception.ObjectNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

import static java.util.Collections.emptyList;

@Log4j2
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final ApplicationUserContextService userContextService;

    @Autowired
    public CustomUserDetailsService(ApplicationUserContextService userContextService) {
        this.userContextService = userContextService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        ApplicationUser user;
        try {
            Set<ApplicationUserContext> contextSet = userContextService.findAllContextsByUsernameOrEmail(username);
            user = contextSet.iterator().next().getUser();
        } catch (ObjectNotFoundException e) {
            throw new UsernameNotFoundException(username);
        }
        CustomUserDetails userDetails = new CustomUserDetails(user.getUsername(), user.getPassword(), emptyList());
        userDetails.setApplicationUser(user);
        return userDetails;
    }

}