package com.vticket.notify.infra.mail;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class EmailRequest {
    private String to;
    private String subject;
    private String templateName;
    private Map<String, Object> variables;
}
