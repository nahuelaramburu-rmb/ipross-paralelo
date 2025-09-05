package com.capacidad.identityservice.service.impl;

import com.capacidad.identityservice.misc.ApplicationProperties;
import com.capacidad.identityservice.model.ApplicationUser;
import com.capacidad.identityservice.model.ApplicationUserContext;
import com.capacidad.identityservice.service.NotificationService;
import com.capacidad.utils.exception.ObjectNotValidException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

@Log4j2
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final ApplicationProperties applicationProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Autowired
    public NotificationServiceImpl(ApplicationProperties applicationProperties,
                                   RestTemplateBuilder restTemplateBuilder,
                                   ObjectMapper objectMapper) {
        this.applicationProperties = applicationProperties;
        this.objectMapper = objectMapper;
        restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(3))
                .basicAuthentication(applicationProperties.getNotificationServiceClientId(),
                        applicationProperties.getNotificationServiceClientSecret())
                .build();
    }

    private UriComponentsBuilder prepareUriString() {
        return UriComponentsBuilder.fromUriString(applicationProperties.getNotificationServiceHost());
    }

    private String buildUserContextObject(ApplicationUserContext applicationUserContext) {
        ApplicationUser user = applicationUserContext.getUser();
        ObjectNode userObject = objectMapper.createObjectNode();
        userObject.put("sub", user.getSub().toString());
        userObject.put("group", user.getGroup().toString().toLowerCase());
        userObject.put("resource_id", user.getResourceId() != null ? user.getResourceId().toString() : null);
        userObject.put("email", user.getEmail());
        userObject.put("username", user.getUsername());
        userObject.put("role", applicationUserContext.getRole().getName());
        userObject.put("name", user.getProfile().getName());
        userObject.put("last_name", user.getProfile().getLastName());
        userObject.put("id_number", StringUtils.join(user.getProfile().getIdType(), " ", user.getProfile().getIdNumber()));
        userObject.put("tenant_id", applicationUserContext.getTenant().getTenantId().toString());
        return userObject.toString();
    }

    @Override
    public void registerUserContext(ApplicationUserContext userContext) throws ObjectNotValidException {
        try {
            String userObjectString = buildUserContextObject(userContext);
            UriComponentsBuilder builder = prepareUriString();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(userObjectString, headers);
            restTemplate.postForEntity(builder.build().toUri(), entity, String.class);
        } catch (RestClientResponseException e) {
            log.error("Error registering ApplicationUserContext with NotificationService: {} {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw new ObjectNotValidException("notification.cannotRegister");
        } catch (Exception e) {
            log.error("Error registering ApplicationUserContext with NotificationService: {}", e.getMessage());
            throw new ObjectNotValidException("notification.cannotRegister");
        }
    }

    @Override
    public void unregisterUserContext(ApplicationUserContext userContext) throws ObjectNotValidException {
        try {
            UriComponentsBuilder builder = prepareUriString();
            builder
                    .queryParam("sub", userContext.getUser().getSub())
                    .queryParam("tenant_id", userContext.getTenant().getTenantId().toString())
                    .queryParam("group", userContext.getUser().getGroup().toString().toLowerCase());
            restTemplate.exchange(builder.build().toUri(), HttpMethod.DELETE, null, String.class);
        } catch (Exception e) {
            log.error("Error unregistering ApplicationUserContext with NotificationService: {}", e.getMessage());
            throw new ObjectNotValidException("notification.cannotUnregister");
        }
    }
}
