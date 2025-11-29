package com.vticket.notify.infra.config;

import com.vticket.commonlibs.utils.Constant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // JSON Converter
    @Bean
    public Jackson2JsonMessageConverter jsonConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // Queue builder có gắn DLX
    private Queue buildQueue(String name) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", Constant.RABBITMQ.DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", name + ".dlq")
                .build();
    }

    @Bean
    public Queue otpQueue() {
        return buildQueue(Constant.RABBITMQ.QUEUE_OTP_MAIL);
    }

    @Bean
    public Queue ticketQueue() {
        return buildQueue(Constant.RABBITMQ.QUEUE_TICKET);
    }

    @Bean
    public Queue topicUserQueue() {
        return buildQueue(Constant.RABBITMQ.QUEUE_TOPIC_USER);
    }

    @Bean
    public Queue topicAdminQueue() {
        return buildQueue(Constant.RABBITMQ.QUEUE_TOPIC_ADMIN);
    }

    @Bean
    public Queue fanoutQueue() {
        return new Queue(Constant.RABBITMQ.QUEUE_FANOUT, true);
    }

    @Bean
    public Queue dlqQueue() {
        return QueueBuilder.durable(Constant.RABBITMQ.DLQ_QUEUE).build();
    }

    // MAIN EXCHANGE
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(Constant.RABBITMQ.DIRECT_EXCHANGE);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(Constant.RABBITMQ.TOPIC_EXCHANGE);
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange(Constant.RABBITMQ.FANOUT_EXCHANGE);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(Constant.RABBITMQ.DLX_EXCHANGE);
    }

    // DIRECT BINDINGS
    @Bean
    public Binding bindingOtp(@Qualifier("otpQueue") Queue otpQueue,
                              @Qualifier("directExchange") DirectExchange directExchange) {
        return BindingBuilder.bind(otpQueue)
                .to(directExchange)
                .with(Constant.RABBITMQ.ROUTING_OTP_MAIL);
    }

    // TOPIC BINDINGS
    @Bean
    public Binding bindingTopicUser(@Qualifier("topicUserQueue") Queue topicUserQueue,
                                    @Qualifier("topicExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(topicUserQueue)
                .to(topicExchange)
                .with("email.user.*");
    }

    @Bean
    public Binding bindingTopicAdmin(@Qualifier("topicAdminQueue") Queue topicAdminQueue,
                                     @Qualifier("topicExchange") TopicExchange topicExchange) {
        return BindingBuilder.bind(topicAdminQueue)
                .to(topicExchange)
                .with("email.admin.#");
    }

    // FANOUT BINDING
    @Bean
    public Binding bindingFanout(@Qualifier("fanoutExchange") FanoutExchange fanoutExchange,
                                 @Qualifier("fanoutQueue") Queue fanoutQueue) {
        return BindingBuilder.bind(fanoutQueue).to(fanoutExchange);
    }

    // DLX BINDING
    // Mọi message lỗi → dồn về DLQ
    @Bean
    public Binding bindingDLQ(@Qualifier("dlxExchange") DirectExchange dlxExchange,
                              @Qualifier("dlqQueue") Queue dlqQueue) {
        return BindingBuilder.bind(dlqQueue).to(dlxExchange).with("#");
    }

    // LISTENER CONFIG + RETRY
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);

        factory.setConcurrentConsumers(3);     // sl thread dùng
        factory.setMaxConcurrentConsumers(10); // tối đa thread

        // Retry nội bộ (không push xuống DLQ ngay)
        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(3)
                        .recoverer((msg, cause) -> {
                            throw new RuntimeException("Failed after retry", cause);
                        })
                        .build()
        );
        return factory;
    }
}
