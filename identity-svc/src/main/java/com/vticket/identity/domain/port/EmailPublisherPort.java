package com.vticket.identity.domain.port;

import com.vticket.identity.domain.event.EmailEvent;

public interface EmailPublisherPort {
    void publishOtp(EmailEvent event);
    void publishResendOtp(EmailEvent event);
}