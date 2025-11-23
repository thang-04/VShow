package com.vticket.eventcatalog.domain.entity;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    private Long id;
    private String categoryName;
    private String categoryDescription;
}
