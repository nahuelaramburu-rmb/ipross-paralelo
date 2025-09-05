package com.capacidad.validationapi.module.amqp;

import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Properties;

import static org.springframework.amqp.rabbit.core.RabbitAdmin.QUEUE_CONSUMER_COUNT;

@Service
@Log4j2
public class AmqpServiceImpl implements AmqpService {

    public static final String QUEUE = "queue";
    private final AmqpAdmin amqpAdmin;

    @Autowired
    public AmqpServiceImpl(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    @Override
    public void createQueue(String name) {
        Properties properties = amqpAdmin.getQueueProperties(name);
        if (properties == null) {
            var queueArgs = new HashMap<String, Object>();
            Queue queue = new Queue(name, true, false, false, queueArgs);
            amqpAdmin.declareQueue(queue);
        }
    }

    @Override
    public void deleteQueue(String name) {
        amqpAdmin.deleteQueue(name);
    }

    @Override
    public Integer getQueueActiveConsumers(String queueName) {
        Properties properties = getQueueProperties(queueName);
        return properties == null ? 0 : (Integer) properties.get(QUEUE_CONSUMER_COUNT);
    }

    @Override
    public Properties getQueueProperties(String queueName) {
        return amqpAdmin.getQueueProperties(queueName);
    }

}
