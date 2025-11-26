package com.vticket.identity.domain.port;

import com.vticket.identity.domain.event.EmailEvent;

public interface EmailPublisherPort {
    void publish(EmailEvent event);
}