package com.vticket.notify.app.usecase;

import com.vticket.notify.domain.event.EmailEvent;
import com.vticket.notify.domain.port.MailSenderPort;
import com.vticket.notify.infra.mail.EmailRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendMailUseCase {

    private final MailSenderPort mailSenderPort;

    public void send(EmailEvent event) {
        EmailRequest req = EmailRequest.builder()
                .to(event.getTo())
                .subject(event.getSubject())
                .templateName(event.getTemplate())
                .variables(event.getVariables())
                .build();

        mailSenderPort.sendOtpEmail(req);
    }
}