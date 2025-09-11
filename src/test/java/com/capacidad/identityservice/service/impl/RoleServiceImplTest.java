package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.model.Role;
import com.capacidad.identityservice.repository.RoleRepository;
import com.capacidad.utils.exception.ObjectNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static com.capacidad.identityservice.misc.constant.ApplicationConstants.COLON;
import static com.capacidad.identityservice.misc.constant.ScopeConstants.CREATE;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RoleServiceImplTest {

    @Mock
    private SecurityContext securityContext;
    @Mock
    private RoleRepository roleRepository;
    @InjectMocks
    private RoleServiceImpl roleService;

    @Test(expected = InsufficientAuthenticationException.class)
    public void testValidateAuthorityRoleAccessThrowsExceptionWhenAuthorityAccessLevelIsEqualLevelButDifferentRole() throws ObjectNotFoundException {
        SecurityContextHolder.setContext(securityContext);

        Role medicalCenter = new Role();
        medicalCenter.setId(1L);
        medicalCenter.setName(MEDICAL_CENTER);
        medicalCenter.setAccessLevel(10);

        Role practitioner = new Role();
        practitioner.setId(2L);
        practitioner.setName(PRACTITIONER);
        practitioner.setAccessLevel(10);

        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(ROLE_MEDICAL_CENTER_AUTHORITY);
        Authentication authentication = new UsernamePasswordAuthenticationToken("", "", authorityList);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(roleRepository.findByNameIgnoreCase(MEDICAL_CENTER)).thenReturn(Optional.of(medicalCenter));

        roleService.validateAuthorityRoleAccess(practitioner, CREATE);
    }

    @Test(expected = InsufficientAuthenticationException.class)
    public void testValidateAuthorityRoleAccessThrowsExceptionWhenAuthorityAccessLevelIsLowerLevelAndDifferentRole() throws ObjectNotFoundException {
        SecurityContextHolder.setContext(securityContext);

        Role funder = new Role();
        funder.setId(1L);
        funder.setName(FUNDER);
        funder.setAccessLevel(90);

        Role admin = new Role();
        admin.setId(2L);
        admin.setName(ADMIN);
        admin.setAccessLevel(100);

        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(ROLE_FUNDER_AUTHORITY);
        Authentication authentication = new UsernamePasswordAuthenticationToken("", "", authorityList);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(roleRepository.findByNameIgnoreCase(FUNDER)).thenReturn(Optional.of(funder));

        roleService.validateAuthorityRoleAccess(admin, CREATE);
    }

    @Test(expected = InsufficientAuthenticationException.class)
    public void testValidateAuthorityRoleAccessThrowsExceptionWhenAuthorityAccessLevelIsEqualLevelAndDifferentRoleAndNoScope() throws ObjectNotFoundException {
        SecurityContextHolder.setContext(securityContext);

        Role client = new Role();
        client.setId(1L);
        client.setName(CLIENT);
        client.setAccessLevel(1);

        Role practitioner = new Role();
        practitioner.setId(2L);
        practitioner.setName(PRACTITIONER);
        practitioner.setAccessLevel(1);

        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(ROLE_CLIENT_AUTHORITY);
        Authentication authentication = new UsernamePasswordAuthenticationToken("", "", authorityList);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(roleRepository.findByNameIgnoreCase(CLIENT)).thenReturn(Optional.of(client));

        roleService.validateAuthorityRoleAccess(practitioner, CREATE);
    }

    @Test
    public void testValidateAuthorityRoleAccessDoNotThrowsExceptionWhenAuthorityAccessLevelIsBiggerLevelAndDifferentRole() throws ObjectNotFoundException {
        SecurityContextHolder.setContext(securityContext);

        Role funder = new Role();
        funder.setId(1L);
        funder.setName(FUNDER);
        funder.setAccessLevel(90);

        Role admin = new Role();
        admin.setId(2L);
        admin.setName(ADMIN);
        admin.setAccessLevel(100);

        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(ROLE_ADMIN_AUTHORITY);
        Authentication authentication = new UsernamePasswordAuthenticationToken("", "", authorityList);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(roleRepository.findByNameIgnoreCase(ADMIN)).thenReturn(Optional.of(admin));

        roleService.validateAuthorityRoleAccess(funder, CREATE);
    }

    @Test
    public void testValidateAuthorityRoleAccessDoNotThrowsExceptionWhenAuthorityAccessLevelIsEqualLevelAndRoleIsEqual() throws ObjectNotFoundException {
        SecurityContextHolder.setContext(securityContext);

        Role organization = new Role();
        organization.setId(1L);
        organization.setName(ORGANIZATION);
        organization.setAccessLevel(1);

        Role practitioner = new Role();
        practitioner.setId(2L);
        practitioner.setName(PRACTITIONER);
        practitioner.setAccessLevel(1);

        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(ROLE_ORGANIZATION_AUTHORITY);
        authorityList.add(new SimpleGrantedAuthority(StringUtils.join(CREATE, COLON, PRACTITIONER.toLowerCase())));
        Authentication authentication = new UsernamePasswordAuthenticationToken("", "", authorityList);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(roleRepository.findByNameIgnoreCase(ORGANIZATION)).thenReturn(Optional.of(organization));

        roleService.validateAuthorityRoleAccess(practitioner, CREATE);
    }

    @Test
    public void testValidateAuthorityRoleAccessDoNotThrowsExceptionWhenAuthorityAccessLevelIsEqualLevelAndRoleIsDifferentButScope() throws ObjectNotFoundException {
        SecurityContextHolder.setContext(securityContext);

        Role medicalCenter = new Role();
        medicalCenter.setId(1L);
        medicalCenter.setName(MEDICAL_CENTER);
        medicalCenter.setAccessLevel(10);

        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(ROLE_MEDICAL_CENTER_AUTHORITY);
        Authentication authentication = new UsernamePasswordAuthenticationToken("", "", authorityList);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(roleRepository.findByNameIgnoreCase(MEDICAL_CENTER)).thenReturn(Optional.of(medicalCenter));

        roleService.validateAuthorityRoleAccess(medicalCenter, CREATE);
    }

    @Test(expected = InsufficientAuthenticationException.class)
    public void testValidateClientRoleAccessThrowsExceptionWhenClientDoesNotContainTokenPermissionForRequestedRole() {
        Set<String> clientScopes = new HashSet<>();

        clientScopes.add("token:beneficiary");
        clientScopes.add("token:practitioner");

        Role medicalCenter = new Role();
        medicalCenter.setId(1L);
        medicalCenter.setName(MEDICAL_CENTER);
        medicalCenter.setAccessLevel(10);

        roleService.validateClientRoleAccess(medicalCenter, clientScopes);
    }

    @Test
    public void testValidateClientRoleAccessDoNotThrowsExceptionWhenClientContainsTokenPermissionForRequestedRole() {
        Set<String> clientScopes = new HashSet<>();

        clientScopes.add("token:beneficiary");
        clientScopes.add("token:practitioner");
        clientScopes.add("token:medical_center");

        Role medicalCenter = new Role();
        medicalCenter.setId(1L);
        medicalCenter.setName(MEDICAL_CENTER);
        medicalCenter.setAccessLevel(10);

        roleService.validateClientRoleAccess(medicalCenter, clientScopes);
    }

    @Test
    public void testValidateClientRoleAccessDoNotThrowsExceptionWhenClientContainsAllPermission() {
        Set<String> clientScopes = new HashSet<>();

        clientScopes.add("token:all");

        Role medicalCenter = new Role();
        medicalCenter.setId(1L);
        medicalCenter.setName(MEDICAL_CENTER);
        medicalCenter.setAccessLevel(10);

        roleService.validateClientRoleAccess(medicalCenter, clientScopes);
    }

    @Test
    public void testFindRolesReturnRoleListWhenValidRoleNames() {
        Role role1 = new Role();
        role1.setName(ADMIN);

        Role role2 = new Role();
        role2.setName(FUNDER);

        List<String> roleNames = Arrays.asList(role1.getName(), role2.getName());
        List<Role> roleList = new ArrayList<>();
        roleList.add(role1);
        roleList.add(role2);

        when(roleRepository.findAllByNameIn(any())).thenReturn(roleList);

        List<Role> result = roleService.findRoles(roleNames);

        assertThat(result).isEqualTo(roleList);
    }

}
