package com.capacidad.validationapi.module.notification.service.impl;

import com.capacidad.validationapi.misc.ApplicationProperties;
import com.capacidad.validationapi.module.amqp.AmqpService;
import com.capacidad.validationapi.module.notification.model.Notification;
import com.capacidad.validationapi.module.notification.model.NotificationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static com.capacidad.validationapi.misc.constant.SecurityConstants.BENEFICIARY;
import static com.capacidad.validationapi.module.notification.misc.constant.NotificationConstant.PARAM_BODY;
import static com.capacidad.validationapi.module.notification.misc.constant.NotificationConstant.PARAM_TYPE;
import static com.capacidad.validationapi.module.notification.model.NotificationMessageType.NEW_NOTIFICATION;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class NotificationPublisherImplTest {

    private ObjectMapper objectMapperInstance = new ObjectMapper();

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AmqpService amqpService;

    @Mock
    private SimpMessageSendingOperations sendingOperations;

    @Mock
    private RestTemplateBuilder restTemplateBuilder;

    @Mock
    private RestTemplate restTemplate;

    @Spy
    @InjectMocks
    private NotificationPublisherImpl notificationPublisher;

    @Before
    public void init() {
        when(restTemplateBuilder.setConnectTimeout(any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.setReadTimeout(any())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.basicAuthentication(anyString(), anyString())).thenReturn(restTemplateBuilder);
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        when(restTemplateBuilder.setBufferRequestBody(anyBoolean())).thenReturn(restTemplateBuilder);
        when(applicationProperties.getNotificationServiceClientId()).thenReturn("client-id");
        when(applicationProperties.getNotificationServiceClientSecret()).thenReturn("client-secret");
        when(applicationProperties.getNotificationsEnabled()).thenReturn(true);
        when(applicationProperties.getNotificationServiceHost()).thenReturn("http://localhost/notification-service");
        when(applicationProperties.getActiveProfile()).thenReturn("dev");
        notificationPublisher.initRestTemplate();
    }

    @Test
    public void testPublishToResourceIdReturnsFalseAndDoNotPublishOverSocketWhenCannotParseNotification() throws JsonProcessingException {
        Notification notification = new Notification(NotificationType.ALL,
                UUID.randomUUID(), "title", "body", new HashMap<>());

        when(objectMapper.writeValueAsString(notification)).thenThrow(mock(JsonProcessingException.class));

        notificationPublisher.publishToResourceId(notification, Collections.singletonList("resourceid"));

        verify(restTemplate, never()).postForEntity(any(), any(), any());
        verify(amqpService, never()).getQueueProperties(anyString());
        verify(sendingOperations, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    public void testPublishToResourceIdReturnsFalseAndDoNotPublishOverSocketWhenNotificationDisabled() throws JsonProcessingException {
        Notification notification = new Notification(NotificationType.ALL,
                UUID.randomUUID(), "title", "body", new HashMap<>());

        when(applicationProperties.getNotificationsEnabled()).thenReturn(false);
        when(objectMapper.writeValueAsString(notification)).thenReturn("parsedNotification");

        notificationPublisher.publishToResourceId(notification, Collections.singletonList("resourceid"));

        verify(restTemplate, never()).postForEntity(any(), any(), any());
        verify(amqpService, never()).getQueueProperties(anyString());
        verify(sendingOperations, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    public void testPublishToResourceIdReturnsFalseAndDoNotPublishOverSocketWhenRestTemplateException() throws JsonProcessingException {
        Notification notification = new Notification(NotificationType.ALL,
                UUID.randomUUID(), "title", "body", new HashMap<>());

        when(objectMapper.writeValueAsString(notification)).thenReturn("parsedNotification");
        when(restTemplate.postForEntity(any(), any(), any())).thenThrow(new RestClientException(""));

        notificationPublisher.publishToResourceId(notification, Collections.singletonList("resourceid"));

        verify(restTemplate, times(1)).postForEntity(any(), any(), any());
        verify(amqpService, never()).getQueueProperties(anyString());
        verify(sendingOperations, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    public void testPublishToResourceIdReturnsTrueAndDoNotPublishOverSocketWhenRestTemplateException() throws JsonProcessingException {
        Notification notification = new Notification(NotificationType.ALL,
                UUID.randomUUID(), "title", "body", new HashMap<>());

        when(objectMapper.writeValueAsString(notification)).thenReturn("parsedNotification");
        when(restTemplate.postForEntity(any(), any(), any())).thenThrow(new RestClientException(""));

        notificationPublisher.publishToResourceId(notification, Collections.singletonList("resourceid"));

        verify(restTemplate, times(1)).postForEntity(any(), any(), any());
        verify(amqpService, never()).getQueueProperties(anyString());
        verify(sendingOperations, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    public void testPublishToResourceIdReturnsTrueAndDoNotPublishOverSocketWhenStatusCodeException() throws JsonProcessingException {
        Notification notification = new Notification(NotificationType.ALL,
                UUID.randomUUID(), "title", "body", new HashMap<>());

        when(objectMapper.writeValueAsString(notification)).thenReturn("parsedNotification");
        when(restTemplate.postForEntity(any(), any(), any())).thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        notificationPublisher.publishToResourceId(notification, Collections.singletonList("resourceid"));

        verify(restTemplate, times(1)).postForEntity(any(), any(), any());
        verify(amqpService, never()).getQueueProperties(anyString());
        verify(sendingOperations, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    public void testPublishToResourceIdReturnsTrueAndDoNotPublishOverSocketWhenNoActiveQueueFound() throws JsonProcessingException {
        Notification notification = new Notification(NotificationType.ALL,
                UUID.randomUUID(), "title", "body", new HashMap<>());

        when(objectMapper.writeValueAsString(notification)).thenReturn("parsedNotification");
        when(objectMapper.createObjectNode()).thenReturn(mock(ObjectNode.class));
        when(restTemplate.postForEntity(any(), any(), any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(amqpService.getQueueProperties(anyString())).thenReturn(null);

        notificationPublisher.publishToResourceId(notification, Collections.singletonList("resourceid"));

        verify(restTemplate, times(1)).postForEntity(any(), any(), any());
        verify(amqpService, times(1)).getQueueProperties(anyString());
        verify(sendingOperations, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    public void testPublishToResourceIdReturnsTrueAndPublishOverSocketWhenActiveQueueFound() throws JsonProcessingException {
        UUID tenantId = UUID.randomUUID();
        UUID resourceId = UUID.randomUUID();

        Notification notification = new Notification(NotificationType.ALL,
                tenantId, "title", "body", new HashMap<>());

        ObjectNode objectNode = objectMapperInstance.createObjectNode();

        when(objectMapper.writeValueAsString(notification)).thenReturn("parsedNotification");
        when(objectMapper.createObjectNode()).thenReturn(objectNode);
        when(restTemplate.postForEntity(any(), any(), any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(amqpService.getQueueProperties(anyString())).thenReturn(new Properties());

        notificationPublisher.publishToResourceId(notification, Collections.singletonList(resourceId.toString()));

        String expectedQueueName = StringUtils.join("/queue/dev.", tenantId.toString(), ".resource_id.", resourceId.toString());
        Map<String, Object> expectedQueueHeaders = new HashMap<>();
        expectedQueueHeaders.put("auto-delete", true);
        expectedQueueHeaders.put("exclusive", false);
        ObjectNode expectedObjectNode = objectMapperInstance.createObjectNode();
        expectedObjectNode.put(PARAM_TYPE, NEW_NOTIFICATION.toString());
        expectedObjectNode.put(PARAM_BODY, notification.getTitle());

        verify(restTemplate, times(1)).postForEntity(any(), any(), any());
        verify(amqpService, times(1)).getQueueProperties(anyString());
        verify(sendingOperations, times(1)).convertAndSend(expectedQueueName, expectedObjectNode, expectedQueueHeaders);
    }

    @Test
    public void testPublishToRoleReturnsFalseAndDoNotPublishOverSocketWhenCannotParseNotification() throws JsonProcessingException {
        Notification notification = new Notification(NotificationType.ALL,
                UUID.randomUUID(), "title", "body", new HashMap<>());

        when(objectMapper.writeValueAsString(notification)).thenThrow(mock(JsonProcessingException.class));

        notificationPublisher.publishToRole(notification, Collections.singletonList("role"));

        verify(restTemplate, never()).postForEntity(any(), any(), any());
        verify(amqpService, never()).getQueueProperties(anyString());
        verify(sendingOperations, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    public void testPublishToRoleReturnsTrueAndPublishOverSocketWhenActiveQueueFound() throws JsonProcessingException {
        UUID tenantId = UUID.randomUUID();
        String role = BENEFICIARY;

        Notification notification = new Notification(NotificationType.ALL,
                tenantId, "title", "body", new HashMap<>());

        ObjectNode objectNode = objectMapperInstance.createObjectNode();

        when(objectMapper.writeValueAsString(notification)).thenReturn("parsedNotification");
        when(objectMapper.createObjectNode()).thenReturn(objectNode);
        when(restTemplate.postForEntity(any(), any(), any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(amqpService.getQueueProperties(anyString())).thenReturn(new Properties());

        notificationPublisher.publishToRole(notification, Collections.singletonList(role));

        String expectedQueueName = StringUtils.join("/queue/dev.", tenantId.toString(), ".role.", role.toLowerCase());
        Map<String, Object> expectedQueueHeaders = new HashMap<>();
        expectedQueueHeaders.put("auto-delete", true);
        expectedQueueHeaders.put("exclusive", false);
        ObjectNode expectedObjectNode = objectMapperInstance.createObjectNode();
        expectedObjectNode.put(PARAM_TYPE, NEW_NOTIFICATION.toString());
        expectedObjectNode.put(PARAM_BODY, notification.getTitle());

        verify(restTemplate, times(1)).postForEntity(any(), any(), any());
        verify(amqpService, times(1)).getQueueProperties(anyString());
        verify(sendingOperations, times(1)).convertAndSend(expectedQueueName, expectedObjectNode, expectedQueueHeaders);
    }

    @Test
    public void testPublishToSubReturnsFalseAndDoNotPublishOverSocketWhenCannotParseNotification() throws JsonProcessingException {
        Notification notification = new Notification(NotificationType.ALL,
                UUID.randomUUID(), "title", "body", new HashMap<>());

        when(objectMapper.writeValueAsString(notification)).thenThrow(mock(JsonProcessingException.class));

        notificationPublisher.publishToSub(notification, Collections.singletonList("sub"));

        verify(restTemplate, never()).postForEntity(any(), any(), any());
        verify(amqpService, never()).getQueueProperties(anyString());
        verify(sendingOperations, never()).convertAndSend(anyString(), any(Object.class));
    }

    @Test
    public void testPublishToSubReturnsTrueAndPublishOverSocketWhenActiveQueueFound() throws JsonProcessingException {
        UUID tenantId = UUID.randomUUID();
        UUID sub = UUID.randomUUID();

        Notification notification = new Notification(NotificationType.ALL,
                tenantId, "title", "body", new HashMap<>());

        ObjectNode objectNode = objectMapperInstance.createObjectNode();

        when(objectMapper.writeValueAsString(notification)).thenReturn("parsedNotification");
        when(objectMapper.createObjectNode()).thenReturn(objectNode);
        when(restTemplate.postForEntity(any(), any(), any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(amqpService.getQueueProperties(anyString())).thenReturn(new Properties());

        notificationPublisher.publishToSub(notification, Collections.singletonList(sub.toString()));

        String expectedQueueName = StringUtils.join("/queue/dev.", tenantId.toString(), ".sub.", sub.toString());
        Map<String, Object> expectedQueueHeaders = new HashMap<>();
        expectedQueueHeaders.put("auto-delete", true);
        expectedQueueHeaders.put("exclusive", false);
        ObjectNode expectedObjectNode = objectMapperInstance.createObjectNode();
        expectedObjectNode.put(PARAM_TYPE, NEW_NOTIFICATION.toString());
        expectedObjectNode.put(PARAM_BODY, notification.getTitle());

        verify(restTemplate, times(1)).postForEntity(any(), any(), any());
        verify(amqpService, times(1)).getQueueProperties(anyString());
        verify(sendingOperations, times(1)).convertAndSend(expectedQueueName, expectedObjectNode, expectedQueueHeaders);
    }

    @Test
    public void testPublishToSubReturnsTrueAndDoNotPublishOverSocketWhenMessagingException() throws JsonProcessingException {
        UUID tenantId = UUID.randomUUID();
        UUID sub = UUID.randomUUID();

        Notification notification = new Notification(NotificationType.ALL,
                tenantId, "title", "body", new HashMap<>());

        ObjectNode objectNode = objectMapperInstance.createObjectNode();

        when(objectMapper.writeValueAsString(notification)).thenReturn("parsedNotification");
        when(objectMapper.createObjectNode()).thenReturn(objectNode);
        when(restTemplate.postForEntity(any(), any(), any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
        when(amqpService.getQueueProperties(anyString())).thenReturn(new Properties());

        String expectedQueueName = StringUtils.join("/queue/dev.", tenantId.toString(), ".sub.", sub.toString());
        Map<String, Object> expectedQueueHeaders = new HashMap<>();
        expectedQueueHeaders.put("auto-delete", true);
        expectedQueueHeaders.put("exclusive", false);
        ObjectNode expectedObjectNode = objectMapperInstance.createObjectNode();
        expectedObjectNode.put(PARAM_TYPE, NEW_NOTIFICATION.toString());
        expectedObjectNode.put(PARAM_BODY, notification.getTitle());

        doThrow(new MessagingException("")).when(sendingOperations).convertAndSend(expectedQueueName, expectedObjectNode, expectedQueueHeaders);

        notificationPublisher.publishToSub(notification, Collections.singletonList(sub.toString()));

        verify(restTemplate, times(1)).postForEntity(any(), any(), any());
        verify(amqpService, times(1)).getQueueProperties(anyString());
        verify(sendingOperations, times(1)).convertAndSend(expectedQueueName, expectedObjectNode, expectedQueueHeaders);
    }

}
