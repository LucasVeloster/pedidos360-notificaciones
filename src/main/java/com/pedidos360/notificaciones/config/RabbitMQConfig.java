package com.pedidos360.notificaciones.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${pedidos360.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${pedidos360.rabbitmq.queue}")
    private String queueName;

    @Value("${pedidos360.rabbitmq.routing-key}")
    private String routingKey;

    @Value("${pedidos360.rabbitmq.dlx}")
    private String dlxName;

    @Value("${pedidos360.rabbitmq.dlq}")
    private String dlqName;

    @Value("${pedidos360.rabbitmq.dlq-routing-key}")
    private String dlqRoutingKey;

    @Bean
    public DirectExchange pedidosExchange() {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(dlxName, true, false);
    }

    @Bean
    public Queue pedidosQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", dlxName)
                .withArgument("x-dead-letter-routing-key", dlqRoutingKey)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(dlqName).build();
    }

    @Bean
    public Binding pedidosBinding() {
        return BindingBuilder
                .bind(pedidosQueue())
                .to(pedidosExchange())
                .with(routingKey);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
                .bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(dlqRoutingKey);
    }
}
