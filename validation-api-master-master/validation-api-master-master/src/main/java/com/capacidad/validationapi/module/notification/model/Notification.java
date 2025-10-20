package com.capacidad.validationapi.module.notification.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
public class Notification {

    @JsonProperty("message_id")
    private final UUID messageId;
    @JsonProperty("tenant_id")
    private final UUID tenantId;
    private final String title;
    private final String body;
    @JsonProperty("extra_data")
    private final Map<String, Object> extraData;
    @JsonProperty("type")
    private String notificationType;

    public Notification(NotificationType notificationType, UUID tenantId, String title, String body, Map<String, Object> extraData) {
        this.notificationType = notificationType.name().toLowerCase();
        this.messageId = UUID.randomUUID();
        this.tenantId = tenantId;
        this.title = title;
        this.body = body;
        this.extraData = extraData;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType.name().toLowerCase();
    }

}
