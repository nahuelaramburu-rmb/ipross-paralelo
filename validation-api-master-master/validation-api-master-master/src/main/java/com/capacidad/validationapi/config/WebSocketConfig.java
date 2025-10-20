package com.capacidad.validationapi.config;

import com.capacidad.utils.TokenUtils;
import com.capacidad.validationapi.config.filter.JWTAuthenticationFilter;
import com.capacidad.validationapi.config.security.IdentityClientService;
import com.capacidad.validationapi.config.security.WebSocketAuthentication;
import com.capacidad.validationapi.misc.ApplicationProperties;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.capacidad.utils.Constants.JWT_CLAIM_TENANT;
import static com.capacidad.utils.Constants.JWT_CLAIM_USERNAME;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DOT;
import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_WEBSOCKET_AUDIT;
import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.ENDPOINT_WEBSOCKET_NOTIFICATIONS;
import static com.capacidad.validationapi.misc.constant.SecurityConstants.HEADER_AUTHORIZATION;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@EnableWebSocketMessageBroker
@Log4j2
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final ApplicationProperties applicationProperties;
    private final JWTAuthenticationFilter jwtAuthenticationFilter;
    private final IdentityClientService tokenVerifier;

    @Autowired
    public WebSocketConfig(ApplicationProperties applicationProperties,
                           JWTAuthenticationFilter jwtAuthenticationFilter,
                           IdentityClientService tokenVerifier) {
        this.applicationProperties = applicationProperties;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.tokenVerifier = tokenVerifier;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
        registry.enableStompBrokerRelay("/queue", "/topic", "/exchange")
                .setRelayHost(applicationProperties.getRabbitMqHost())
                .setRelayPort(applicationProperties.getRabbitMqStompPort())
                .setVirtualHost("/")
                .setSystemLogin(applicationProperties.getRabbitMqUsername())
                .setSystemPasscode(applicationProperties.getRabbitMqPassword())
                .setClientLogin(applicationProperties.getRabbitMqUsername())
                .setClientPasscode(applicationProperties.getRabbitMqPassword());
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(ENDPOINT_WEBSOCKET_AUDIT, ENDPOINT_WEBSOCKET_NOTIFICATIONS)
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                return onPreSend(message);
            }
        });
    }

    protected Message<?> onPreSend(Message<?> message) {
        StompHeaderAccessor accessor = getAccessor(message);
        if (accessor != null && (StompCommand.CONNECT.equals(accessor.getCommand()) || StompCommand.SUBSCRIBE.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand()))) {
            List<String> authorization = accessor.getNativeHeader(HEADER_AUTHORIZATION);
            Authentication authentication = authenticate(authorization != null ? authorization.get(0) : "");
            accessor.setUser(authentication);
            validateSubscription(accessor, authentication);
        }
        return message;
    }

    protected StompHeaderAccessor getAccessor(Message<?> message) {
        return MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
    }

    private void validateSubscription(StompHeaderAccessor accessor, Authentication authentication) {
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) && authentication != null) {
            String destination = accessor.getDestination();
            UUID tenantId = ((WebSocketAuthentication) authentication).getTenantId();
            if (!StringUtils.contains(destination, StringUtils.join(applicationProperties.getActiveProfile(), DOT, tenantId.toString())))
                accessor.setUser(null);
        }
    }

    private Authentication authenticate(String jwt) {
        Map<String, Object> jwtValues = jwtAuthenticationFilter.parseAndValidateJwt(jwt);
        //tokenVerifier.verify(jwt);
        UUID tenantId = UUID.fromString((String) jwtValues.get(JWT_CLAIM_TENANT));
        String subject = TokenUtils.getJwtSubject(jwt).orElse("");
        return new WebSocketAuthentication((String) jwtValues.get(JWT_CLAIM_USERNAME), jwtAuthenticationFilter.getAuthorities(jwtValues), tenantId, UUID.fromString(subject));
    }

}
