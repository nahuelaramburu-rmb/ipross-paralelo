package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.config.TenantContext;
import com.capacidad.identityservice.config.security.ClientAuthenticationToken;
import com.capacidad.identityservice.config.security.JWTAuthenticationToken;
import com.capacidad.identityservice.model.*;
import com.capacidad.identityservice.model.dto.UpdateApplicationUserContextDTO;
import com.capacidad.identityservice.model.dto.UpdateApplicationUserDTO;
import com.capacidad.identityservice.model.projection.ApplicationUserContextProjection;
import com.capacidad.identityservice.repository.ApplicationUserContextRepository;
import com.capacidad.identityservice.service.ApplicationUserContextSupportService;
import com.capacidad.identityservice.service.ApplicationUserService;
import com.capacidad.identityservice.service.RoleService;
import com.capacidad.utils.exception.ObjectNotFoundException;
import com.capacidad.utils.exception.ObjectNotValidException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.ClientDetails;

import java.util.*;

import static com.capacidad.identityservice.misc.constant.ScopeConstants.UPDATE;
import static com.capacidad.identityservice.misc.constant.SecurityConstants.*;
import static com.capacidad.utils.Constants.ROLE_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationUserContextServiceImplTest {

    @Mock
    private ClientDetails clientDetails;
    @Mock
    private ApplicationUserContextRepository userContextRepository;
    @Mock
    private ApplicationUserService userService;
    @Mock
    private RoleService roleService;
    @Mock
    private ApplicationUserContextSupportService supportService;
    @Mock
    private SecurityContext securityContext;
    @Spy
    @InjectMocks
    private ApplicationUserContextServiceImpl userContextService;

    @Test
    public void testValidateThrowsObjectNotFoundExceptionWhenRoleIsNotNullButDoesNotExist() throws ObjectNotFoundException {
        ApplicationUserContext userContext = new ApplicationUserContext();
        Role role = new Role();
        role.setName("invalid_role");
        userContext.setRole(role);

        when(roleService.findRole(role.getName())).thenThrow(new ObjectNotFoundException("Role not found"));

        ObjectNotFoundException thrown = (ObjectNotFoundException) catchThrowable(() -> userContextService.validate(userContext));

        assertThat(thrown.getMessage()).isEqualTo("Role not found");
        assertThat(thrown.getStackTrace()[0].getMethodName()).isEqualTo("validate");
    }

    @Test
    public void testValidateThrowsObjectNotFoundExceptionWhenRoleIsNullAndAuthenticatedRoleIsInvalid() throws ObjectNotFoundException {
        SecurityContextHolder.setContext(securityContext);
        ApplicationUserContext userContext = new ApplicationUserContext();
        userContext.setRole(null);

        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority(StringUtils.join(ROLE_PREFIX, ADMIN)));

        Authentication authentication = new UsernamePasswordAuthenticationToken("", "", authorityList);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(roleService.findRole(StringUtils.join(ROLE_PREFIX, ADMIN))).thenThrow(new ObjectNotFoundException("Role not found"));

        ObjectNotFoundException thrown = (ObjectNotFoundException) catchThrowable(() -> userContextService.validate(userContext));

        assertThat(thrown.getMessage()).isEqualTo("Role not found");
        assertThat(thrown.getStackTrace()[0].getMethodName()).isEqualTo("validate");
    }

    @Test
    public void testValidateThrowsObjectNotValidExceptionWhenRoleIsNotAdminNorFunderAndResourceIdIsNull() throws ObjectNotFoundException {
        SecurityContextHolder.setContext(securityContext);
        ApplicationUserContext userContext = new ApplicationUserContext();
        userContext.setUser(new ApplicationUser());

        Role role = new Role();
        role.setName(MEDICAL_CENTER);
        role.setReusableResourceId(true);
        role.setResourceIdRequired(true);

        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority(StringUtils.join(ROLE_PREFIX, MEDICAL_CENTER)));

        Authentication authentication = new UsernamePasswordAuthenticationToken("", "", authorityList);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(roleService.findRole(StringUtils.join(ROLE_PREFIX, MEDICAL_CENTER))).thenReturn(role);

        ObjectNotValidException thrown = (ObjectNotValidException) catchThrowable(() -> userContextService.validate(userContext));

        assertThat(thrown.getMessage()).isEqualTo("applicationUserContext.resourceIdRequirement");
        assertThat(thrown.getStackTrace()[0].getMethodName()).isEqualTo("validateResource");
    }

    @Test
    public void testValidateThrowsObjectNotValidExceptionWhenRoleIsBeneficiaryAndResourceIdAlreadyInUse() throws ObjectNotFoundException {
        ApplicationUserContext userContext = new ApplicationUserContext();
        ApplicationUser user = new ApplicationUser();
        user.setResourceId(UUID.randomUUID());
        userContext.setUser(user);

        Role role = new Role();
        role.setName(BENEFICIARY);
        role.setResourceIdRequired(true);
        role.setReusableResourceId(false);
        userContext.setRole(role);

        when(roleService.findRole(userContext.getRole().getName())).thenReturn(role);
        when(userService.existsByResourceId(user.getResourceId())).thenReturn(true);

        ObjectNotValidException thrown = (ObjectNotValidException) catchThrowable(() -> userContextService.validate(userContext));

        assertThat(thrown.getMessage()).isEqualTo("applicationUserContext.resourceIdAlreadyExist");
        assertThat(thrown.getStackTrace()[0].getMethodName()).isEqualTo("validateResource");
    }

    @Test
    public void testValidateThrowsObjectNotValidExceptionWhenRoleIsPractitionerAndResourceIdAlreadyInUse() throws ObjectNotFoundException {
        ApplicationUserContext userContext = new ApplicationUserContext();
        ApplicationUser user = new ApplicationUser();
        user.setResourceId(UUID.randomUUID());
        userContext.setUser(user);

        Role role = new Role();
        role.setName(PRACTITIONER);
        role.setResourceIdRequired(true);
        role.setReusableResourceId(false);
        userContext.setRole(role);

        when(roleService.findRole(userContext.getRole().getName())).thenReturn(role);
        when(userService.existsByResourceId(user.getResourceId())).thenReturn(true);

        ObjectNotValidException thrown = (ObjectNotValidException) catchThrowable(() -> userContextService.validate(userContext));

        assertThat(thrown.getMessage()).isEqualTo("applicationUserContext.resourceIdAlreadyExist");
        assertThat(thrown.getStackTrace()[0].getMethodName()).isEqualTo("validateResource");
    }

    @Test
    public void testValidateSuccessWhenRoleIsMedicalCenterAndResourceIdIsNotNull() throws ObjectNotFoundException, ObjectNotValidException {
        ApplicationUserContext userContext = new ApplicationUserContext();
        ApplicationUser user = new ApplicationUser();
        user.setResourceId(UUID.randomUUID());
        userContext.setUser(user);

        Role role = new Role();
        role.setName(MEDICAL_CENTER);
        role.setReusableResourceId(true);
        role.setResourceIdRequired(true);
        userContext.setRole(role);

        when(roleService.findRole(userContext.getRole().getName())).thenReturn(role);

        userContextService.validate(userContext);
    }

    @Test
    public void testValidateSuccessWhenRoleIsAdminAndResourceIdIsNull() throws ObjectNotFoundException, ObjectNotValidException {
        ApplicationUserContext userContext = new ApplicationUserContext();
        ApplicationUser user = new ApplicationUser();
        user.setResourceId(null);
        userContext.setUser(user);

        Role role = new Role();
        role.setName(ADMIN);
        role.setResourceIdRequired(false);
        role.setReusableResourceId(false);
        userContext.setRole(role);

        when(roleService.findRole(userContext.getRole().getName())).thenReturn(role);

        userContextService.validate(userContext);
    }

    @Test
    public void testValidateSuccessWhenRoleIsFunderAndResourceIdIsNull() throws ObjectNotFoundException, ObjectNotValidException {
        ApplicationUserContext userContext = new ApplicationUserContext();
        ApplicationUser user = new ApplicationUser();
        user.setResourceId(null);
        userContext.setUser(user);

        Role role = new Role();
        role.setName(FUNDER);
        role.setResourceIdRequired(false);
        role.setReusableResourceId(false);
        userContext.setRole(role);

        when(roleService.findRole(userContext.getRole().getName())).thenReturn(role);

        userContextService.validate(userContext);
    }

    @Test
    public void testValidateSuccessWhenRoleIsBeneficiaryAndResourceIdIsNotNullAndResourceIdDoesNotExist() throws ObjectNotFoundException, ObjectNotValidException {
        ApplicationUserContext userContext = new ApplicationUserContext();
        ApplicationUser user = new ApplicationUser();
        user.setResourceId(UUID.randomUUID());
        userContext.setUser(user);

        Role role = new Role();
        role.setName(BENEFICIARY);
        role.setResourceIdRequired(true);
        role.setReusableResourceId(false);
        userContext.setRole(role);

        when(userService.existsByResourceId(user.getResourceId())).thenReturn(false);
        when(roleService.findRole(userContext.getRole().getName())).thenReturn(role);

        userContextService.validate(userContext);
    }

    @Test
    public void testValidateDoNotSetUserResourceIdWhenAuthenticationNotInstanceOfJWTAuthenticationToken() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(ROLE_ADMIN_AUTHORITY);

        Authentication authentication = new UsernamePasswordAuthenticationToken("", "", authorities);

        ApplicationUserContext userContext = new ApplicationUserContext();
        ApplicationUser user = new ApplicationUser();
        UUID resourceId = UUID.randomUUID();
        user.setResourceId(resourceId);
        userContext.setUser(user);

        Role role = new Role();
        role.setName(MEDICAL_CENTER);
        role.setReusableResourceId(true);
        role.setResourceIdRequired(true);
        userContext.setRole(role);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(roleService.findRole(userContext.getRole().getName())).thenReturn(role);

        userContextService.validate(userContext);

        assertThat(user.getResourceId()).isEqualTo(resourceId);
    }

    @Test
    public void testValidateDoNotSetUserResourceIdWhenJWTAuthenticationTokenResourceIdIsNull() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(ROLE_ADMIN_AUTHORITY);

        Authentication authentication = new JWTAuthenticationToken("", "", authorities, Group.DEV, null);

        ApplicationUserContext userContext = new ApplicationUserContext();
        ApplicationUser user = new ApplicationUser();
        UUID resourceId = UUID.randomUUID();
        user.setResourceId(resourceId);
        userContext.setUser(user);

        Role role = new Role();
        role.setName(MEDICAL_CENTER);
        role.setResourceIdRequired(true);
        role.setReusableResourceId(true);
        userContext.setRole(role);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(roleService.findRole(userContext.getRole().getName())).thenReturn(role);

        userContextService.validate(userContext);

        assertThat(user.getResourceId()).isEqualTo(resourceId);
    }

    @Test
    public void testValidateSetsUserResourceIdWhenJWTAuthenticationTokenResourceIdIsNotNull() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(ROLE_ADMIN_AUTHORITY);

        UUID resourceId = UUID.randomUUID();

        Authentication authentication = new JWTAuthenticationToken("", "", authorities, Group.DEV, resourceId);

        ApplicationUserContext userContext = new ApplicationUserContext();
        ApplicationUser user = new ApplicationUser();

        user.setResourceId(null);
        userContext.setUser(user);

        Role role = new Role();
        role.setName(MEDICAL_CENTER);
        role.setReusableResourceId(true);
        role.setResourceIdRequired(true);
        userContext.setRole(role);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(roleService.findRole(userContext.getRole().getName())).thenReturn(role);

        userContextService.validate(userContext);

        assertThat(user.getResourceId()).isEqualTo(resourceId);
    }

    @Test
    public void testCreateReturnsValidSignedUpUserContextWhenAuthenticatedAuthorityIsClient() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(ROLE_CLIENT_AUTHORITY);
        Authentication authentication = new UsernamePasswordAuthenticationToken("", "", authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        Tenant tenant = new Tenant();
        tenant.setName("test");
        tenant.setTenantId(UUID.randomUUID());
        TenantContext.setTenant(tenant);

        ApplicationUserContext userContext = new ApplicationUserContext();
        ApplicationUser dtoUser = new ApplicationUser();
        userContext.setUser(dtoUser);
        userContext.setPermissionGroups(Collections.singletonList(new PermissionGroup()));

        ApplicationUser retrievedUser = new ApplicationUser();
        retrievedUser.setChallengeType(ChallengeType.EMAIL_VERIFICATION_REQUIRED);

        Role role = new Role();
        role.setName(BENEFICIARY);
        userContext.setRole(role);

        when(userContextRepository.saveAndFlush(userContext)).thenReturn(userContext);
        when(userService.signUp(userContext.getUser())).thenReturn(retrievedUser);
        doNothing().when(userContextService).validate(userContext);

        ApplicationUserContext result = userContextService.create(userContext);

        verify(supportService, times(1)).sendVerificationEmail(userContext);
        verify(userService, times(1)).signUp(any());
        verify(supportService, times(1)).registerUserContextToNotificationService(userContext);
        verify(supportService, times(1)).setPermissionsAndStrategyToContext(userContext);
        assertThat(result.getTenant().getName()).isEqualTo(tenant.getName());
    }

    @Test
    public void testCreateReturnsValidCreatedUserContextWhenAuthenticatedAuthorityIsNotClient() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(ROLE_FUNDER_AUTHORITY);
        Authentication authentication = new UsernamePasswordAuthenticationToken("", "", authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        Tenant tenant = new Tenant();
        tenant.setName("test");
        tenant.setTenantId(UUID.randomUUID());
        TenantContext.setTenant(tenant);

        ApplicationUserContext userContext = new ApplicationUserContext();
        ApplicationUser dtoUser = new ApplicationUser();
        userContext.setUser(dtoUser);

        ApplicationUser retrievedUser = new ApplicationUser();
        retrievedUser.setChallengeType(ChallengeType.FORCE_CHANGE_PASSWORD);

        Role role = new Role();
        role.setName(BENEFICIARY);
        userContext.setRole(role);

        when(userContextRepository.saveAndFlush(userContext)).thenReturn(userContext);
        when(userService.create(userContext.getUser())).thenReturn(retrievedUser);
        doNothing().when(userContextService).validate(userContext);

        ApplicationUserContext result = userContextService.create(userContext);

        verify(supportService, times(1)).sendConfirmationEmail(userContext);
        verify(userService, times(1)).create(any(ApplicationUser.class));
        verify(supportService, times(1)).registerUserContextToNotificationService(userContext);
        verify(supportService, times(1)).setPermissionsAndStrategyToContext(userContext);
        assertThat(result.getTenant().getName()).isEqualTo(tenant.getName());
    }

    @Test(expected = ObjectNotFoundException.class)
    public void testDeleteThrowsObjectNotFoundWhenUserDoesNotExist() throws ObjectNotFoundException, ObjectNotValidException {
        Tenant tenant = new Tenant();
        tenant.setTenantId(UUID.randomUUID());
        TenantContext.setTenant(tenant);

        when(userContextRepository.findByUserUsernameAndTenantTenantId("test", tenant.getTenantId())).thenReturn(Optional.empty());

        userContextService.delete("test");
    }

    @Test
    public void testDeleteThrowsInsufficientAuthenticationExceptionWhenAuthenticatedAuthorityGroupIsNull() {
        SecurityContextHolder.setContext(securityContext);

        ApplicationUserContext userContext = new ApplicationUserContext();
        ApplicationUser user = new ApplicationUser();
        user.setUsername("test");
        user.setGroup(Group.DEV);

        Tenant tenant = new Tenant();
        tenant.setTenantId(UUID.randomUUID());
        TenantContext.setTenant(tenant);
        userContext.setUser(user);
        userContext.setTenant(tenant);

        Role role = new Role();
        role.setName(MEDICAL_CENTER);

        userContext.setRole(role);

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("testAuthority"));
        Authentication anonymousAuthenticationToken = new AnonymousAuthenticationToken("key", "principal", authorities);

        when(securityContext.getAuthentication()).thenReturn(anonymousAuthenticationToken);
        when(userContextRepository.findByUserUsernameAndTenantTenantId("test", tenant.getTenantId())).thenReturn(Optional.of(userContext));

        InsufficientAuthenticationException thrown = (InsufficientAuthenticationException) catchThrowable(() -> userContextService.delete("test"));

        assertThat(thrown.getMessage()).isEqualTo("applicationUserContext.cannotDelete");
        assertThat(thrown.getStackTrace()[0].getMethodName()).isEqualTo("delete");
    }

    @Test
    public void testDeleteThrowsInsufficientAuthenticationExceptionWhenAuthenticatedAuthorityGroupDoesNotMatchUsersGroup() {
        SecurityContextHolder.setContext(securityContext);

        ApplicationUserContext userContext = new ApplicationUserContext();
        ApplicationUser user = new ApplicationUser();
        user.setUsername("test");
        user.setGroup(Group.DEV);

        Tenant tenant = new Tenant();
        tenant.setTenantId(UUID.randomUUID());
        TenantContext.setTenant(tenant);
        userContext.setUser(user);
        userContext.setTenant(tenant);

        Role role = new Role();
        role.setName(MEDICAL_CENTER);

        userContext.setRole(role);

        Authentication jwtAuthenticationToken = new JWTAuthenticationToken("principal", "", Collections.emptyList(), Group.TEST, null);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(userContextRepository.findByUserUsernameAndTenantTenantId("test", tenant.getTenantId())).thenReturn(Optional.of(userContext));

        InsufficientAuthenticationException thrown = (InsufficientAuthenticationException) catchThrowable(() -> userContextService.delete("test"));

        assertThat(thrown.getMessage()).isEqualTo("applicationUserContext.cannotDelete");
        assertThat(thrown.getStackTrace()[0].getMethodName()).isEqualTo("delete");
    }

    @Test
    public void testDeleteSuccessfullyDeletesUserEntirelyWhenAuthenticatedAuthorityNotJWTAuthentication() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);

        ApplicationUserContext userContext = new ApplicationUserContext();
        ApplicationUser user = new ApplicationUser();
        user.setUsername("test");
        user.setGroup(Group.DEV);
        user.getContextSet().add(userContext);

        UUID resourceId = UUID.randomUUID();
        user.setResourceId(resourceId);

        Tenant tenant = new Tenant();
        tenant.setTenantId(UUID.randomUUID());
        TenantContext.setTenant(tenant);
        userContext.setUser(user);
        userContext.setTenant(tenant);

        Role role = new Role();
        role.setName(MEDICAL_CENTER);

        userContext.setRole(role);

        Authentication clientAuthenticationToken = new ClientAuthenticationToken("principal", "", Collections.emptyList(), Group.valueOf("dev".toUpperCase()));

        when(securityContext.getAuthentication()).thenReturn(clientAuthenticationToken);
        when(userContextRepository.findByUserUsernameAndTenantTenantId("test", tenant.getTenantId())).thenReturn(Optional.of(userContext));

        userContextService.delete("test");

        verify(userService, times(1)).delete(user);
        verify(supportService, times(1)).unregisterUserContextFromNotificationService(userContext);
        verify(userService, times(1)).clearUserTokens(user.getUsername());
    }

    @Test
    public void testDeleteSuccessfullyDeletesUserEntirelyWhenAuthenticatedAuthorityResourceIdMatchUsersResourceId() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);

        ApplicationUserContext userContext = new ApplicationUserContext();
        ApplicationUser user = new ApplicationUser();
        user.setUsername("test");
        user.setGroup(Group.DEV);
        user.getContextSet().add(userContext);

        UUID resourceId = UUID.randomUUID();
        user.setResourceId(resourceId);

        Tenant tenant = new Tenant();
        tenant.setTenantId(UUID.randomUUID());
        TenantContext.setTenant(tenant);
        userContext.setUser(user);
        userContext.setTenant(tenant);

        Role role = new Role();
        role.setName(MEDICAL_CENTER);

        userContext.setRole(role);

        Authentication jwtAuthenticationToken = new JWTAuthenticationToken("principal", "", Collections.emptyList(), Group.DEV, resourceId);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(userContextRepository.findByUserUsernameAndTenantTenantId("test", tenant.getTenantId())).thenReturn(Optional.of(userContext));

        userContextService.delete("test");

        verify(userService, times(1)).delete(user);
        verify(supportService, times(1)).unregisterUserContextFromNotificationService(userContext);
        verify(userService, times(1)).clearUserTokens(user.getUsername());
    }

    @Test
    public void testDeleteSuccessfullyDeletesUserEntirelyWhenItContainsOnlyOneContext() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);

        ApplicationUserContext userContext = new ApplicationUserContext();
        ApplicationUser user = new ApplicationUser();
        user.setUsername("test");
        user.setGroup(Group.DEV);
        user.getContextSet().add(userContext);

        Tenant tenant = new Tenant();
        tenant.setTenantId(UUID.randomUUID());
        TenantContext.setTenant(tenant);
        userContext.setUser(user);
        userContext.setTenant(tenant);
        user.setResourceId(UUID.randomUUID());

        Role role = new Role();
        role.setName(ADMIN);

        userContext.setRole(role);

        Authentication jwtAuthenticationToken = new JWTAuthenticationToken("principal", "", Collections.emptyList(), Group.DEV, null);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(userContextRepository.findByUserUsernameAndTenantTenantId("test", tenant.getTenantId())).thenReturn(Optional.of(userContext));

        userContextService.delete("test");

        verify(userService, times(1)).delete(user);
        verify(supportService, times(1)).unregisterUserContextFromNotificationService(userContext);
    }

    @Test
    public void testDeleteSuccessfullyRemovesUserContextWhenItContainsMoreThanOneContext() throws ObjectNotFoundException, ObjectNotValidException {
        SecurityContextHolder.setContext(securityContext);

        ApplicationUserContext userContext = new ApplicationUserContext();
        ApplicationUser user = new ApplicationUser();
        user.setUsername("test");
        user.setGroup(Group.DEV);
        user.getContextSet().add(userContext);
        user.getContextSet().add(new ApplicationUserContext());

        Tenant tenant = new Tenant();
        tenant.setTenantId(UUID.randomUUID());
        TenantContext.setTenant(tenant);
        userContext.setUser(user);
        userContext.setTenant(tenant);

        Role role = new Role();
        role.setName(MEDICAL_CENTER);

        userContext.setRole(role);

        Authentication jwtAuthenticationToken = new JWTAuthenticationToken("principal", "", Collections.emptyList(), Group.DEV, null);

        when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);
        when(userContextRepository.findByUserUsernameAndTenantTenantId("test", tenant.getTenantId())).thenReturn(Optional.of(userContext));

        userContextService.delete("test");

        verify(userService, times(1)).update(user);
        verify(supportService, times(1)).unregisterUserContextFromNotificationService(userContext);
        assertThat(user.getContextSet().size()).isEqualTo(1);
    }

    @Test
    public void testCheckOperationalStateDoNothingWhenClientIdIsNull() {
        userContextService.checkOperationalState(new ApplicationUser(), new ApplicationUserContext(), clientDetails);
        verify(roleService, never()).validateClientRoleAccess(any(Role.class), any());
    }

    @Test
    public void testCheckOperationalStateValidatesClientRoleAccess() {
        UUID tenantId = UUID.randomUUID();

        ApplicationUserContext userContext = new ApplicationUserContext();

        Role role = new Role();
        role.setName(BENEFICIARY);

        Tenant tenant = new Tenant();
        tenant.setTenantId(tenantId);

        ApplicationUser user = new ApplicationUser();

        userContext.setRole(role);
        userContext.setTenant(tenant);

        user.getContextSet().add(userContext);

        Set<String> scope = new HashSet<>();

        when(clientDetails.getScope()).thenReturn(scope);

        userContextService.checkOperationalState(user, userContext, clientDetails);

        verify(roleService, times(1)).validateClientRoleAccess(role, scope);
    }

    @Test
    public void testForgotPasswordExecuteSendRestoreEmail() throws ObjectNotFoundException, ObjectNotValidException {
        String email = "test@test.com";
        ApplicationUser user = new ApplicationUser();
        user.setEmail(email);

        State confirmed = new State();
        confirmed.setId(StateReference.CONFIRMED.getId());

        user.setState(confirmed);

        when(userService.restorePassword(email)).thenReturn(user);

        userContextService.forgotPassword(email);

        verify(supportService, times(1)).sendRestoreEmail(user);
    }

    @Test
    public void testForgotPasswordThrowsExceptionWhenInvalidState() throws ObjectNotFoundException {
        String email = "test@test.com";
        ApplicationUser user = new ApplicationUser();
        user.setEmail(email);

        State unconfirmed = new State();
        unconfirmed.setId(StateReference.UNCONFIRMED.getId());

        user.setState(unconfirmed);

        when(userService.restorePassword(email)).thenReturn(user);

        ObjectNotValidException exception = (ObjectNotValidException) catchThrowable(() -> userContextService.forgotPassword(email));

        assertThat(exception.getMessage()).isEqualTo("applicationUser.notConfirmed");
        verify(supportService, never()).sendRestoreEmail(user);
    }

    @Test
    public void testUpdateThrowsExceptionWhenEmptyContexts() {
        UUID sub = UUID.randomUUID();
        Specification<ApplicationUserContext> spec = mock(Specification.class);
        Map<String, Object> props = new HashMap<>();

        when(supportService.buildUserSubAndTenantSpec(sub)).thenReturn(spec);
        when(supportService.buildUserAndPermissionsSearchQueryHints(false, false, true)).thenReturn(props);
        when(userContextRepository.find(spec, props)).thenReturn(Optional.empty());

        ObjectNotFoundException exception = (ObjectNotFoundException) catchThrowable(() -> userContextService.update(sub, new UpdateApplicationUserContextDTO()));

        assertThat(exception.getMessage()).isEqualTo("applicationUserContext.subNotFound");
    }

    @Test
    public void testUpdateExecutesCorrectly() throws ObjectNotFoundException {
        UUID sub = UUID.randomUUID();
        Specification<ApplicationUserContext> spec = mock(Specification.class);
        Map<String, Object> props = new HashMap<>();
        ApplicationUserContextProjection expectedResult = mock(ApplicationUserContextProjection.class);

        ApplicationUserContext userContext = new ApplicationUserContext();
        userContext.setRole(new Role());
        userContext.setPermissionGroups(new ArrayList<>());
        userContext.setPermissionSuggestion(new PermissionSuggestion());

        UpdateApplicationUserContextDTO input = new UpdateApplicationUserContextDTO();
        input.setUser(new UpdateApplicationUserDTO());

        when(supportService.buildUserSubAndTenantSpec(sub)).thenReturn(spec);
        when(supportService.buildUserAndPermissionsSearchQueryHints(false, false, true)).thenReturn(props);
        when(userContextRepository.find(spec, props)).thenReturn(Optional.of(userContext));
        doReturn(userContext).when(userContextService).mapDtoToInput(input);
        when(userContextRepository.save(userContext)).thenReturn(userContext);
        when(supportService.buildProjection(ApplicationUserContextProjection.class, userContext)).thenReturn(expectedResult);

        ApplicationUserContextProjection result = userContextService.update(sub, input);

        assertThat(result).isEqualTo(expectedResult);
        verify(roleService, times(1)).validateAuthorityRoleAccess(userContext.getRole(), UPDATE);
        verify(supportService, times(1)).setPermissionsAndStrategyToContext(userContext, userContext.getPermissionSuggestion(), userContext.getPermissionGroups());
    }
}
