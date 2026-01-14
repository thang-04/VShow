package com.vticket.notify.domain.event;

import lombok.Data;

import java.util.Map;

@Data
public class EmailEvent {
    private String to;
    private String subject;
    private String template;
    private Map<String, Object> variables;
}
