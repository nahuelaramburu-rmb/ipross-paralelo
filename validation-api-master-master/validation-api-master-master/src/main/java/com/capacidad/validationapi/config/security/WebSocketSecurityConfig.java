package com.capacidad.validationapi.config.security;

import com.capacidad.validationapi.misc.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

import static com.capacidad.validationapi.misc.constant.ControllerEndpoints.*;
import static com.capacidad.validationapi.misc.constant.ScopeConstants.*;

@Configuration
public class WebSocketSecurityConfig
        extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry registry) {
        registry
                .simpTypeMatchers(SimpMessageType.CONNECT, SimpMessageType.SUBSCRIBE, SimpMessageType.MESSAGE).authenticated()
                .simpDestMatchers(StringUtils.join(ENDPOINT_WEBSOCKET_AUDITORS, ALL_PARAMS),
                        StringUtils.join(ENDPOINT_WEBSOCKET_AUDIT_TRAYS, ALL_PARAMS))
                .hasAuthority(SecurityUtils.buildScope(READ, AUDIT_TRAY))
                .simpDestMatchers(StringUtils.join(ENDPOINT_WEBSOCKET_NOTIFICATIONS, ALL_PARAMS))
                .hasAuthority(SecurityUtils.buildScope(READ, NOTIFICATIONS));
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }

}
