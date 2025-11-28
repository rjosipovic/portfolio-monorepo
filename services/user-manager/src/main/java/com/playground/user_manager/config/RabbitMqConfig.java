package com.playground.user_manager.config;

import brave.Tracing;
import brave.spring.rabbit.SpringRabbitTracing;
import com.playground.user_manager.auth.messaging.AuthMessagingConfiguration;
import com.playground.user_manager.messaging.callback.CallbackManager;
import com.playground.user_manager.messaging.callback.DlxMessagingConfiguration;
import com.playground.user_manager.user.messaging.UserMessagingConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMqConfig {

    private final CallbackManager callbackManager;
    private final UserMessagingConfiguration userMessagingConfiguration;
    private final AuthMessagingConfiguration authMessagingConfiguration;
    private final DlxMessagingConfiguration dlxMessagingConfiguration;
    private final Tracing tracing;

    @Bean
    public Queue dlq() {
        var queueName = dlxMessagingConfiguration.getQueue();
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public DirectExchange dlxExchange() {
        var exchangeName = dlxMessagingConfiguration.getExchange();
        return ExchangeBuilder.directExchange(exchangeName).durable(true).build();
    }

    @Bean
    public Binding dlxBinding() {
        var routingKey = dlxMessagingConfiguration.getRoutingKey();
        return BindingBuilder.bind(dlq()).to(dlxExchange()).with(routingKey);
    }

    @Bean
    public TopicExchange userExchange() {
        var exchangeName = userMessagingConfiguration.getExchange();
        return ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
    }

    @Bean
    public DirectExchange notificationsExchange() {
        var exchangeName = authMessagingConfiguration.getExchange();
        return ExchangeBuilder.directExchange(exchangeName).durable(true).build();
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SpringRabbitTracing springRabbitTracing() {
        return SpringRabbitTracing.newBuilder(this.tracing).build();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter, SpringRabbitTracing springRabbitTracing) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setConfirmCallback((correlationData, ack, cause) -> callbackManager.processCallback(correlationData, ack));
        return springRabbitTracing.decorateRabbitTemplate(template);
    }
}
