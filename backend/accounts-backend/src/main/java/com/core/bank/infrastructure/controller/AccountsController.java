package com.core.bank.infrastructure.controller;

import com.core.bank.api.CuentasApi;
import com.core.bank.application.dto.PaginationMetadata;
import com.core.bank.application.service.AccountService;
import com.core.bank.application.mapper.AccountMapper;
import com.core.bank.domain.entity.Account;
import com.core.bank.model.dto.AccountDTO;
import com.core.bank.model.dto.AccountCreate;
import com.core.bank.model.dto.AccountPatch;
import com.core.bank.model.dto.PageResponseAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class AccountsController implements CuentasApi {

    private final AccountService accountService;
    private final AccountMapper accountMapper;

    @Override
    public ResponseEntity<AccountDTO> createCuenta(AccountCreate accountCreate) {
        Account account = accountMapper.toEntity(accountCreate);
        Account created = accountService.create(account);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountMapper.toDto(created));
    }

    @Override
    public ResponseEntity<Void> deleteCuenta(UUID cuentaId) {
        accountService.delete(cuentaId.toString());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<AccountDTO> getCuenta(UUID cuentaId) {
        Account account = accountService.getById(cuentaId.toString());
        return ResponseEntity.ok(accountMapper.toDto(account));
    }

    @Override
    public ResponseEntity<PageResponseAccount> listCuentas(Integer page, Integer size, UUID clienteId, String q) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;

        PaginationMetadata paginationMetadata = accountService.buildMetadata(page, size, clienteId, q);

        int offset = pageNum * pageSize;

        List<AccountDTO> content = accountService.findAccounts(clienteId, offset, size, q)
                .stream()
                .map(accountMapper::toDto)
                .collect(Collectors.toList());

        PageResponseAccount response = new PageResponseAccount();
        response.setContent(content);
        response.setPage(pageNum);
        response.setSize(pageSize);
        response.setTotalElements(paginationMetadata.getTotalElements());
        response.setTotalPages(paginationMetadata.getTotalPages());

        return ResponseEntity.ok(response);
    }


    @Override
    public ResponseEntity<AccountDTO> patchCuenta(UUID cuentaId, AccountPatch accountPatch) {
        Account account = accountService.getById(cuentaId.toString());
        accountMapper.updateEntity(accountPatch, account);
        Account patched = accountService.patch(cuentaId.toString(), account);
        return ResponseEntity.ok(accountMapper.toDto(patched));
    }
}
