package com.vticket.eventcatalog.app.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitTicketRequest {
    private Long eventId;
    private List<ListItem> listItem;
    private Long timestamp;
}
