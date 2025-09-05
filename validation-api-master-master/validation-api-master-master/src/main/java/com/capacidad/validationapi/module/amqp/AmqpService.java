package com.capacidad.validationapi.module.amqp;

import java.util.Properties;

public interface AmqpService {

    void createQueue(String name);

    void deleteQueue(String name);

    Integer getQueueActiveConsumers(String queueName);

    Properties getQueueProperties(String queueName);

}
