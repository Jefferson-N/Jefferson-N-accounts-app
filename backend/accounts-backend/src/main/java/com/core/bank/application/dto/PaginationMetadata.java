package com.core.bank.application.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class PaginationMetadata {

    private Integer page;
    private Integer size;
    private Long totalElements;
    private Integer totalPages;

}
