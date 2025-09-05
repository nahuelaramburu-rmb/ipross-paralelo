package com.capacidad.validationapi.misc;

import org.apache.commons.lang3.StringUtils;

import static com.capacidad.validationapi.misc.constant.ApplicationConstants.DOT;
import static com.capacidad.validationapi.misc.constant.ApplicationConstants.SLASH;
import static com.capacidad.validationapi.module.amqp.AmqpServiceImpl.QUEUE;
import static org.springframework.amqp.core.ExchangeTypes.TOPIC;

public class MQNameBuilder {

    private MQNameBuilder() {
    }

    public static String buildTopicName(String activeProfile, String tenantId, String name) {
        return StringUtils.join(SLASH, TOPIC, SLASH, activeProfile, DOT, tenantId, DOT, name);
    }

    public static String buildQueueName(String activeProfile, String tenantId, String name) {
        return StringUtils.join(SLASH, QUEUE, SLASH, activeProfile, DOT, tenantId, DOT, name);
    }

}
