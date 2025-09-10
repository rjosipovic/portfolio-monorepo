package com.playground.analytics_manager.config;

import com.playground.analytics_manager.inbound.messaging.MessagingConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    private final MessagingConfiguration messagingConfiguration;

    @Bean
    public Queue dlq() {
        var queueName = messagingConfiguration.getDeadLetter().getQueue();
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public DirectExchange dlxExchange() {
        var exchangeName = messagingConfiguration.getDeadLetter().getExchange();
        return ExchangeBuilder.directExchange(exchangeName).durable(true).build();
    }

    @Bean
    public Binding dlxBinding() {
        var routingKey = messagingConfiguration.getDeadLetter().getBindingKey();
        return BindingBuilder.bind(dlq()).to(dlxExchange()).with(routingKey);
    }

    @Bean
    public TopicExchange userExchange() {
        var name = messagingConfiguration.getUser().getExchange();
        return ExchangeBuilder.topicExchange(name).durable(true).build();
    }

    @Bean
    public Queue userQueue() {
        var name = messagingConfiguration.getUser().getQueue();
        var dlxExchange = messagingConfiguration.getDeadLetter().getExchange();
        var dlxRoutingKey = messagingConfiguration.getDeadLetter().getBindingKey();
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", dlxExchange)
                .withArgument("x-dead-letter-routing-key", dlxRoutingKey)
                .build();
    }

    @Bean
    public Binding userBinding() {
        var bindingKey = messagingConfiguration.getUser().getBindingKey();
        return BindingBuilder.bind(userQueue()).to(userExchange()).with(bindingKey);
    }

    @Bean
    public TopicExchange challengeExchange() {
        var name = messagingConfiguration.getChallenge().getExchange();
        return ExchangeBuilder.topicExchange(name).durable(true).build();
    }

    @Bean
    public Queue challengeQueue() {
        var name = messagingConfiguration.getChallenge().getQueue();
        var dlxExchange = messagingConfiguration.getDeadLetter().getExchange();
        var dlxRoutingKey = messagingConfiguration.getDeadLetter().getBindingKey();
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", dlxExchange)
                .withArgument("x-dead-letter-routing-key", dlxRoutingKey)
                .build();
    }

    @Bean
    public Binding challengeBinding() {
        var bindingKey = messagingConfiguration.getChallenge().getBindingKey();
        return BindingBuilder.bind(challengeQueue()).to(challengeExchange()).with(bindingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
