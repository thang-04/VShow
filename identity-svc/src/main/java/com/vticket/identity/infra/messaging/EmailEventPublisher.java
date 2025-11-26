package com.vticket.identity.infra.messaging;

import com.vticket.commonlibs.utils.Constant;
import com.vticket.identity.domain.event.EmailEvent;
import com.vticket.identity.domain.port.EmailPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailEventPublisher implements EmailPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(EmailEvent event) {
        log.info("Publish OTP event to exchange={}, routing={}",
                Constant.RABBITMQ.DIRECT_EXCHANGE,
                Constant.RABBITMQ.ROUTING_OTP_MAIL
        );

        rabbitTemplate.convertAndSend(
                Constant.RABBITMQ.DIRECT_EXCHANGE,
                Constant.RABBITMQ.ROUTING_OTP_MAIL,
                event
        );
    }
}