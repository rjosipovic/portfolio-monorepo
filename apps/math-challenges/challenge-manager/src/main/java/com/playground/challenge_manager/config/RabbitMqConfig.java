package com.playground.challenge_manager.config;

import brave.Tracing;
import brave.spring.rabbit.SpringRabbitTracing;
import com.playground.challenge_manager.challenge.messaging.MessagingConfiguration;
import com.playground.challenge_manager.messaging.callback.CallbackManager;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.FanoutExchange;
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

    private final MessagingConfiguration messagingConfiguration;
    private final CallbackManager callbackManager;
    private final Tracing tracing;

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
        var routingKey = messagingConfiguration.getDeadLetter().getRoutingKey();
        return BindingBuilder.bind(dlq()).to(dlxExchange()).with(routingKey);
    }

    @Bean
    public TopicExchange challengeExchange() {
        var exchangeName = messagingConfiguration.getChallenge().getExchange();
        return ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
    }

    @Bean
    public FanoutExchange challengeUpdateExchange() {
        var exchangeName = messagingConfiguration.getChallengeUpdate().getFanoutExchange();
        return ExchangeBuilder.fanoutExchange(exchangeName).durable(false).build(); // Transient exchange
    }

    @Bean
    public Queue challengeUpdateQueue() {
        return new AnonymousQueue(); // Temporary, exclusive, auto-delete queue
    }

    @Bean
    public Binding challengeUpdateBinding() {
        return BindingBuilder.bind(challengeUpdateQueue()).to(challengeUpdateExchange());
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
        var template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        template.setConfirmCallback((correlationData, ack, cause) -> callbackManager.processCallback(correlationData, ack));
        return springRabbitTracing.decorateRabbitTemplate(template);
    }
}
