package com.capacidad.identityservice.misc;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
public class ApplicationProperties {

    private final Environment environment;

    @Value("${com.capacidad.identityservice.mail.from}")
    private String mailFrom;

    @Value("${OTP_KEY}")
    private String otpKey;

    @Value("${com.capacidad.identityservice.identifier}")
    private String apiIdentifier;

    @Value("${com.capacidad.identityservice.account.verification-retry-minutes}")
    private Long verificationRetry;

    @Value("${com.capacidad.identityservice.account.verification-window-minutes}")
    private Long verificationWindow;

    @Value("${com.capacidad.identityservice.account.issuer}")
    private String jwtIssuer;

    @Value("${NOTIFICATION_SERVICE_HOST}")
    private String notificationServiceHost;

    @Value("${NOTIFICATION_SERVICE_CLIENT_ID}")
    private String notificationServiceClientId;

    @Value("${NOTIFICATION_SERVICE_SECRET}")
    private String notificationServiceClientSecret;

    @Value("${spring.redis.cluster.nodes}")
    private List<String> redisClusterNodes;

    @Value("${spring.redis.cluster.max-redirects}")
    private int redisClusterMaxRedirects;

    @Autowired
    public ApplicationProperties(Environment environment) {
        this.environment = environment;
    }

    public String getActiveProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            // Ningún perfil activo -> usar el por defecto o manejarlo
            return environment.getDefaultProfiles().length > 0
                    ? environment.getDefaultProfiles()[0]
                    : "default";
        }
        return activeProfiles[0];
    }

}