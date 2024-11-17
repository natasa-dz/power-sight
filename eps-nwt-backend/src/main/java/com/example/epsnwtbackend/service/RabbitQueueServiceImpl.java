package com.example.epsnwtbackend.service;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.AbstractMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitQueueServiceImpl implements RabbitQueueService {
    @Autowired
    private RabbitAdmin rabbitAdmin;
    @Autowired
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    @Override
    public void addNewQueue(String queueName, String exchangeName, String routingKey) {
        Queue queue = new Queue(queueName, true, false, false);
        Binding binding = new Binding(
                queueName,
                Binding.DestinationType.QUEUE,
                exchangeName,
                routingKey,
                null
        );
        rabbitAdmin.declareQueue(queue);
        rabbitAdmin.declareBinding(binding);
        this.addQueueToListener(exchangeName,queueName);
    }

    @Override
    public void addQueueToListener(String listenerId, String queueName) {
        if (!checkQueueExistOnListener(listenerId,queueName)) {
            this.getMessageListenerContainerById(listenerId).addQueueNames(queueName);
        }
    }

    @Override
    public void removeQueueFromListener(String listenerId, String queueName) {
        if (checkQueueExistOnListener(listenerId,queueName)) {
            this.getMessageListenerContainerById(listenerId).removeQueueNames(queueName);
            this.rabbitAdmin.deleteQueue(queueName);
        }
    }

    @Override
    public Boolean checkQueueExistOnListener(String listenerId, String queueName) {
        try {
            String[] queueNames = this.getMessageListenerContainerById(listenerId).getQueueNames();
            if (queueNames != null) {
                for (String name : queueNames) {
                    if (name.equals(queueName)) {
                        return Boolean.TRUE;
                    }
                }
                return Boolean.FALSE;
            } else {
                return Boolean.FALSE;
            }
        } catch (Exception e) {
            return Boolean.FALSE;
        }
    }

    private AbstractMessageListenerContainer getMessageListenerContainerById(String listenerId) {
        return ((AbstractMessageListenerContainer) this.rabbitListenerEndpointRegistry
                .getListenerContainer(listenerId)
        );
    }
}
