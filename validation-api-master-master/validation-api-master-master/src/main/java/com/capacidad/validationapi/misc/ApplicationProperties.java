package com.capacidad.validationapi.misc;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
public class ApplicationProperties {

    private final Environment environment;
    @Value("${IDENTITY_HOST}")
    private String identityHost;
    @Value("${IDENTITY_CLIENT_ID}")
    private String identityClientId;
    @Value("${IDENTITY_CLIENT_SECRET}")
    private String identityClientSecret;
    @Value("${HASHIDS_SALT}")
    private String hashidsSalt;
    @Value("${QR_ENCRYPTION_KEY}")
    private String qrEncryptionKey;
    @Value("${QR_ENCRYPTION_IV}")
    private String qrEncryptionIv;
    @Value("${com.capacidad.validationapi.identifier}")
    private String apiIdentifier;
    @Value("${S3_KEY}")
    private String s3Key;
    @Value("${S3_SECRET}")
    private String s3Secret;
    @Value("${com.capacidad.validationapi.storage.s3.region}")
    private String s3Region;
    @Value("#{'${com.capacidad.validationapi.storage.file-signatures}'.split(',')}")
    private List<Integer> fileStorageSignaturesList;
    @Value("#{'${com.capacidad.validationapi.storage.file-extensions}'.trim().split('\\s*,\\s*')}")
    private List<String> fileStorageExtensionList;
    @Value("#{'${com.capacidad.validationapi.import-processor.file-extensions}'.trim().split('\\s*,\\s*')}")
    private List<String> fileImportProcessorExtensionList;
    @Value("${spring.rabbitmq.host}")
    private String rabbitMqHost;
    @Value("${com.capacidad.validationapi.rabbitmq.stomp.port}")
    private Integer rabbitMqStompPort;
    @Value("${RABBITMQ_USERNAME}")
    private String rabbitMqUsername;
    @Value("${RABBITMQ_PASSWORD}")
    private String rabbitMqPassword;
    @Value("${NOTIFICATION_SERVICE_HOST}")
    private String notificationServiceHost;
    @Value("${NOTIFICATION_SERVICE_TOPICS_HOST}")
    private String notificationServiceTopicsHost;
    @Value("${NOTIFICATION_SERVICE_CLIENT_ID}")
    private String notificationServiceClientId;
    @Value("${NOTIFICATION_SERVICE_SECRET}")
    private String notificationServiceClientSecret;
    @Value("${spring.quartz.enabled}")
    private Boolean quartzEnabled;
    @Value("${com.capacidad.validationapi.notification-service.enabled}")
    private Boolean notificationsEnabled;
    @Value("${com.capacidad.validationapi.prescription-synchronization.enabled}")
    private Boolean prescriptionSynchronizationEnabled;

    public ApplicationProperties(Environment environment) {
        this.environment = environment;
    }

    public String getActiveProfile() {
        return environment.getActiveProfiles()[0];
    }

}
