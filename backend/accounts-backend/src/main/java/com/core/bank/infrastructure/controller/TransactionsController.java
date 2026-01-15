package com.core.bank.infrastructure.controller;

import com.core.bank.api.MovimientosApi;
import com.core.bank.application.dto.PaginationMetadata;
import com.core.bank.application.service.TransactionService;
import com.core.bank.application.mapper.TransactionMapper;
import com.core.bank.domain.entity.Transaction;
import com.core.bank.model.dto.TransactionDTO;
import com.core.bank.model.dto.TransactionCreate;
import com.core.bank.model.dto.PageResponseTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class TransactionsController implements MovimientosApi {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    @Override
    public ResponseEntity<TransactionDTO> createMovimiento(TransactionCreate transactionCreate) {
        Transaction transaction = transactionMapper.toEntity(transactionCreate);
        Transaction created = transactionService.create(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionMapper.toDto(created));
    }

    @Override
    public ResponseEntity<Void> deleteMovimiento(UUID id) {
        transactionService.delete(id.toString());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<PageResponseTransaction> listMovimientos(Integer page, Integer size, UUID cuentaId,
                                                                   LocalDate from, LocalDate to) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;

        String accountId = cuentaId != null ? cuentaId.toString() : null;
        LocalDateTime fromDateTime = from != null ? from.atStartOfDay() : null;
        LocalDateTime toDateTime = to != null ? to.atTime(23, 59, 59) : null;

        PaginationMetadata paginationMetadata = transactionService.buildMetadata(page, size, accountId, fromDateTime, toDateTime);

        int offset = pageNum * pageSize;
        List<TransactionDTO> content =
                transactionService.getTransactions(accountId, fromDateTime, toDateTime, offset, pageSize)
                        .stream().map(transactionMapper::toDto)
                        .collect(Collectors.toList());

        PageResponseTransaction response = new PageResponseTransaction();
        response.setContent(content);
        response.setPage(pageNum);
        response.setSize(pageSize);
        response.setTotalElements(paginationMetadata.getTotalElements());
        response.setTotalPages(paginationMetadata.getTotalPages());

        return ResponseEntity.ok(response);
    }
}
