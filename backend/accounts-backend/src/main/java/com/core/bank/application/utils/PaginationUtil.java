package com.core.bank.application.utils;

import com.core.bank.application.dto.PaginationMetadata;

public final class PaginationUtil {

    private PaginationUtil (){

    }

    public static PaginationMetadata buildMetadata(int page, int size, Long totalElements) {


        int totalPages = (int) Math.ceil((double) totalElements / size);
        return PaginationMetadata.builder()
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .build();

    }
}
