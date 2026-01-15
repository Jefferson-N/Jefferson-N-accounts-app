package com.core.bank.infrastructure.controller;

import com.core.bank.api.ClientesApi;
import com.core.bank.application.dto.PaginationMetadata;
import com.core.bank.application.service.CustomerService;
import com.core.bank.application.mapper.CustomerMapper;
import com.core.bank.domain.entity.Customer;
import com.core.bank.domain.repository.CustomerRepositoryCustom;
import com.core.bank.model.dto.CustomerDTO;
import com.core.bank.model.dto.CustomerCreate;
import com.core.bank.model.dto.CustomerUpdate;
import com.core.bank.model.dto.CustomerPatch;
import com.core.bank.model.dto.PageResponseCustomer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CustomersController implements ClientesApi {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @Override
    public ResponseEntity<CustomerDTO> createCliente(CustomerCreate customerCreate) {
        Customer customer = customerMapper.toEntity(customerCreate);
        Customer created = customerService.create(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(customerMapper.toDto(created));
    }

    @Override
    public ResponseEntity<Void> deleteCliente(UUID clienteId) {
        customerService.delete(clienteId.toString());
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<CustomerDTO> getCliente(UUID clienteId) {
        Customer customer = customerService.getById(clienteId.toString());
        return ResponseEntity.ok(customerMapper.toDto(customer));
    }

    @Override
    public ResponseEntity<PageResponseCustomer> listClientes(Integer page, Integer size, String q) {
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 10;

        PaginationMetadata paginationMetadata = customerService.buildMetadata(page, size);

        int offset = pageNum * pageSize;

        List<CustomerDTO> content = customerService.findCustomers(offset, size, q)
                .stream()
                .map(customerMapper::toDto)
                .collect(Collectors.toList());

        PageResponseCustomer response = new PageResponseCustomer();
        response.setContent(content);
        response.setPage(pageNum);
        response.setSize(pageSize);
        response.setTotalElements(paginationMetadata.getTotalElements());
        response.setTotalPages(paginationMetadata.getTotalPages());

        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<CustomerDTO> patchCliente(UUID clienteId, CustomerPatch customerPatch) {
        Customer customer = customerService.getById(clienteId.toString());
        customerMapper.updateEntity(customerPatch, customer);
        Customer patched = customerService.patch(clienteId.toString(), customer);
        return ResponseEntity.ok(customerMapper.toDto(patched));
    }

    @Override
    public ResponseEntity<CustomerDTO> updateCliente(UUID clienteId, CustomerUpdate customerUpdate) {
        Customer customer = customerMapper.toEntity(customerUpdate);
        customer.setId(clienteId.toString());
        Customer updated = customerService.update(clienteId.toString(), customer);
        return ResponseEntity.ok(customerMapper.toDto(updated));
    }
}
