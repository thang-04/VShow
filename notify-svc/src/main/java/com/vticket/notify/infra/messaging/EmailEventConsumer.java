package com.vticket.notify.infra.messaging;

import com.vticket.commonlibs.utils.Constant;
import com.vticket.notify.app.usecase.SendMailUseCase;
import com.vticket.notify.domain.event.EmailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailEventConsumer {

    private final SendMailUseCase sendMailUseCase;

    @RabbitListener(queues = Constant.RABBITMQ.QUEUE_OTP_MAIL,
            containerFactory = "rabbitListenerFactory")
    public void handleOtp(EmailEvent event) {
        log.info("Received OTP event: {}", event);
        sendMailUseCase.send(event);    // send email thực tế
    }
}
