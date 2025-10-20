package com.capacidad.validationapi.module.amqp.service.impl;

import com.capacidad.validationapi.module.amqp.AmqpServiceImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.amqp.rabbit.core.RabbitAdmin.QUEUE_CONSUMER_COUNT;

@RunWith(MockitoJUnitRunner.class)
public class AmqpServiceImplTest {

    @Mock
    private AmqpAdmin amqpAdmin;

    @InjectMocks
    private AmqpServiceImpl amqpService;

    @Test
    public void testQueueIsNotCreatedWhenAlreadyExist() {
        String queueName = "queueTest";
        when(amqpAdmin.getQueueProperties(queueName)).thenReturn(new Properties());

        amqpService.createQueue(queueName);

        verify(amqpAdmin, never()).declareQueue(any(Queue.class));
    }

    @Test
    public void testQueueCreatesSuccessfully() {
        String queueName = "queueTest";
        when(amqpAdmin.getQueueProperties(queueName)).thenReturn(null);

        amqpService.createQueue(queueName);

        verify(amqpAdmin, times(1)).declareQueue(any(Queue.class));
    }

    @Test
    public void testGetQueueActiveConsumersReturnsNullWhenQueueDoesNotExist() {
        String queueName = "queueTest";
        when(amqpAdmin.getQueueProperties(queueName)).thenReturn(null);

        Integer activeConsumers = amqpService.getQueueActiveConsumers(queueName);

        assertThat(activeConsumers).isZero();
    }

    @Test
    public void testGetQueueActiveConsumersReturnsValidCountWhenQueueExists() {
        String queueName = "queueTest";
        Properties properties = new Properties();
        properties.put(QUEUE_CONSUMER_COUNT, 5);
        when(amqpAdmin.getQueueProperties(queueName)).thenReturn(properties);

        Integer activeConsumers = amqpService.getQueueActiveConsumers(queueName);

        assertThat(activeConsumers).isEqualTo(5);
    }

}
