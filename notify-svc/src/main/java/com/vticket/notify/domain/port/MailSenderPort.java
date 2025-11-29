package com.vticket.notify.domain.port;

import com.vticket.notify.infra.mail.EmailRequest;

public interface MailSenderPort {
    void sendOtpEmail(EmailRequest request);
}
