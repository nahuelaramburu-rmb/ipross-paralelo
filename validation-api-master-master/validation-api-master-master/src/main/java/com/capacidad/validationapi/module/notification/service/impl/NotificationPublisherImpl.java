package com.capacidad.validationapi.module.notification.service.impl;

import com.capacidad.validationapi.config.security.IdentityClientService;
import com.capacidad.validationapi.misc.ApplicationProperties;
import com.capacidad.validationapi.misc.MQNameBuilder;
import com.capacidad.validationapi.module.amqp.AmqpService;
import com.capacidad.validationapi.module.notification.model.Notification;
import com.capacidad.validationapi.module.notification.model.NotificationDestination;
import com.capacidad.validationapi.module.notification.service.NotificationPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DOT;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.SLASH;
import static com.capacidad.validationapi.module.amqp.AmqpServiceImpl.QUEUE;
import static com.capacidad.validationapi.module.notification.misc.constant.NotificationConstant.*;
import static com.capacidad.validationapi.module.notification.model.NotificationMessageType.NEW_NOTIFICATION;

@Log4j2
@Component
public class NotificationPublisherImpl implements NotificationPublisher {

    private final ObjectMapper objectMapper;
    private final ApplicationProperties applicationProperties;
    private final SimpMessageSendingOperations sendingOperations;
    private final AmqpService amqpService;
    private final RestTemplateBuilder restTemplateBuilder;
    private final IdentityClientService identityService;
    private RestTemplate restTemplate;
    private final RestTemplate unbufferedRestTemplate = new RestTemplate();
    private static final String FILE_REQUEST_KEY = "file";
    private static final String NOTIFICATION_ENDPOINT = "/notifications";

    @Autowired
    public NotificationPublisherImpl(ObjectMapper objectMapper,
                                     ApplicationProperties applicationProperties,
                                     SimpMessageSendingOperations sendingOperations,
                                     AmqpService amqpService,
                                     RestTemplateBuilder restTemplateBuilder,
                                     IdentityClientService identityService) {
        this.objectMapper = objectMapper;
        this.applicationProperties = applicationProperties;
        this.sendingOperations = sendingOperations;
        this.amqpService = amqpService;
        this.restTemplateBuilder = restTemplateBuilder;
        this.identityService = identityService;
    }

    @PostConstruct
    protected void initRestTemplate() {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(30))
                .setReadTimeout(Duration.ofSeconds(30))
                .basicAuthentication(applicationProperties.getNotificationServiceClientId(),
                        applicationProperties.getNotificationServiceClientSecret())
                .build();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setBufferRequestBody(false);
        unbufferedRestTemplate.setRequestFactory(requestFactory);
    }

    @Override
    public void publishToResourceId(Notification notification, List<String> resourceIds) {
        boolean posted = postNotification(notification, PARAM_RESOURCE_ID, resourceIds);
        if (posted)
            resourceIds.forEach(r -> sendSocketNewNotificationNotice(NotificationDestination.RESOURCE_ID,
                    r,
                    notification.getTitle(),
                    notification.getTenantId()));
    }

    @Override
    public void publishToSub(Notification notification, List<String> subs) {
        boolean posted = postNotification(notification, PARAM_SUB, subs);
        if (posted)
            subs.forEach(r -> sendSocketNewNotificationNotice(NotificationDestination.SUB,
                    r,
                    notification.getTitle(),
                    notification.getTenantId()));
    }

    @Override
    public void publishToRole(Notification notification, List<String> roles) {
        boolean posted = postNotification(notification, PARAM_ROLE, roles);
        if (posted)
            roles.forEach(r -> sendSocketNewNotificationNotice(NotificationDestination.ROLE,
                    r.toLowerCase(),
                    notification.getTitle(),
                    notification.getTenantId()));
    }

    @Override
    public void publishToTopic(File file) {
        Optional<String> bearerToken = identityService.exchangeClientCredentialsForJwt(applicationProperties.getNotificationServiceClientId(),
                applicationProperties.getNotificationServiceClientSecret());
        bearerToken.ifPresent(t -> {
            try {
                log.info("{} - publishToTopic - Starting publication of topic file, size: {} mb", this.getClass(),
                        (double) file.length() / (1024 * 1024));
                FileSystemResource fileData = new FileSystemResource(file);

                HttpHeaders fileHeaders = new HttpHeaders();
                fileHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                fileHeaders.setAccept(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM));
                fileHeaders.setAcceptCharset(Collections.singletonList(StandardCharsets.UTF_8));
                ContentDisposition contentDisposition = ContentDisposition
                        .builder("form-data")
                        .name(FILE_REQUEST_KEY)
                        .filename(file.getName())
                        .build();
                fileHeaders.setContentDisposition(contentDisposition);

                HttpEntity<FileSystemResource> fileEntity = new HttpEntity<>(fileData, fileHeaders);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.MULTIPART_FORM_DATA);
                headers.setBearerAuth(t);

                MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
                body.add(FILE_REQUEST_KEY, fileEntity);

                HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

                unbufferedRestTemplate.exchange(applicationProperties.getNotificationServiceTopicsHost(),
                        HttpMethod.PUT,
                        requestEntity,
                        Void.class);
            } catch (Exception e) {
                log.error("{} - publishToTopic - Exception Message: {}", this.getClass(), e.getMessage());
                log.error("{} - publishToTopic - Stacktrace: {}", this.getClass(), e);
            }
        });
    }

    private boolean postNotification(Notification notification, String queryParamKey, List<String> queryParamValues) {
        log.debug("Posting Notification: Body: {}, Params: {}", notification.getBody(), queryParamValues);
        UriComponentsBuilder builder = prepareUriString(queryParamKey, queryParamValues);
        Optional<String> notificationToPost = parseNotification(notification);
        if (Boolean.TRUE.equals(applicationProperties.getNotificationsEnabled()) && notificationToPost.isPresent()) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<String> entity = new HttpEntity<>(notificationToPost.get(), headers);
                restTemplate.postForEntity(builder.build().toUri(), entity, String.class);
                return true;
            } catch (Exception e) {
                handleNotificationServiceError(e, notificationToPost.get());
                return false;
            }
        }
        return false;
    }

    private void handleNotificationServiceError(Exception e, String content) {
        if (e instanceof RestClientResponseException)
            log.warn("Unexpected response posting notification to NotificationService: {} - Content: {}",
                    e.getMessage(),
                    content);
        else
            log.error("Error posting notification to NotificationService: {} - Content: {}",
                    e.getMessage(),
                    content);
    }

    private UriComponentsBuilder prepareUriString(String key, List<String> values) {
        MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.put(key, values);
        String endpoint = StringUtils.join(applicationProperties.getNotificationServiceHost(), NOTIFICATION_ENDPOINT);
        return UriComponentsBuilder.fromUriString(endpoint).queryParams(queryParams);
    }

    private Optional<String> parseNotification(Notification notification) {
        try {
            return Optional.of(objectMapper.writeValueAsString(notification));
        } catch (JsonProcessingException e) {
            log.error("Error processing Notification as JSON");
        }
        return Optional.empty();
    }

    private void sendSocketNewNotificationNotice(NotificationDestination notificationDestination, String destination, String message, UUID tenantId) {
        String queueName = buildQueueName(notificationDestination, destination, tenantId);
        String queueNameNoPrefix = StringUtils.remove(queueName, StringUtils.join(SLASH, QUEUE, SLASH));
        ObjectNode notice = objectMapper.createObjectNode();
        notice.put(PARAM_TYPE, NEW_NOTIFICATION.toString());
        notice.put(PARAM_BODY, message);
        Map<String, Object> headers = new HashMap<>();
        headers.put("auto-delete", true);
        headers.put("exclusive", false);
        try {
            Properties properties = amqpService.getQueueProperties(queueNameNoPrefix);
            if (properties != null)
                sendingOperations.convertAndSend(queueName, notice, headers);
        } catch (MessagingException e) {
            log.warn("Error sending Notification Notice ({}): {}", notice, e.getFailedMessage());
        }
    }

    private String buildQueueName(NotificationDestination notificationDestination, String destination, UUID tenantId) {
        String queueName = StringUtils.join(notificationDestination.toString().toLowerCase(), DOT, destination);
        return MQNameBuilder.buildQueueName(applicationProperties.getActiveProfile(), tenantId.toString(), queueName);
    }
}
