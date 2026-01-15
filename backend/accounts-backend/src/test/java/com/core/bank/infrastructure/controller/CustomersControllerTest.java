package com.core.bank.infrastructure.controller;

import com.core.bank.application.service.CustomerService;
import com.core.bank.application.mapper.CustomerMapper;
import com.core.bank.domain.entity.Customer;
import com.core.bank.model.dto.CustomerCreate;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(CustomersController.class)
class CustomersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private CustomerMapper customerMapper;

    @Test
    @WithMockUser(username = "test", roles = {"USER"})
    void testCreateCustomerEndpoint() throws Exception {
        CustomerCreate customerCreate = new CustomerCreate();
        customerCreate.setName("Jose Lema");
        customerCreate.setGender(CustomerCreate.GenderEnum.MASCULINO);
        customerCreate.setAge(30);
        customerCreate.setIdentification("1234567890");
        customerCreate.setAddress("Otavalo y su principal");
        customerCreate.setPhone("0985247885");
        customerCreate.setPassword("1234");
        customerCreate.setStatus(true);

        Customer createdCustomer = new Customer();
        createdCustomer.setId(UUID.randomUUID().toString());
        createdCustomer.setName("Jose Lema");
        createdCustomer.setIdentification("1234567890");

        when(customerMapper.toEntity(any(CustomerCreate.class))).thenReturn(new Customer());
        when(customerService.create(any())).thenReturn(createdCustomer);
        when(customerMapper.toDto(any(Customer.class))).thenReturn(new com.core.bank.model.dto.CustomerDTO());

        mockMvc.perform(post("/api/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(customerCreate))
                .with(csrf()))
            .andExpect(status().isCreated());
    }
}
