package com.vticket.identity.domain.event;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
public class EmailEvent implements Serializable {
    private String to;
    private String subject;
    private String template;
    private Map<String, Object> variables;
}
