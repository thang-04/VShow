package com.vticket.eventcatalog.app.dto.req;

import lombok.Data;

@Data
public class UpdateCategoryRequest {
    private String categoryName;
    private String categoryDescription;
}
