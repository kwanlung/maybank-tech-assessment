package com.maybank.maybank_assessment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maybank.maybank_assessment.model.dto.TransactionDto;
import com.maybank.maybank_assessment.model.dto.UpdateTransactionRequest;
import com.maybank.maybank_assessment.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TransactionControllerTests {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void testListTransactions() throws Exception {
        TransactionDto dto = TransactionDto.builder()
                .id(1L)
                .accountNumber(123456L)
                .trxnAmount(new BigDecimal("100.00"))
                .description("Test Desc")
                .trxnTimestamp(LocalDateTime.now())
                .customerId(10L)
                .version(1)
                .build();
        Page<TransactionDto> page = new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);

        when(transactionService.getTransactions(any(), any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/transactions")
                        .param("customerId", "10")
                        .param("accountNumber", "123456")
                        .param("description", "Test Desc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].accountNumber").value(123456L))
                .andExpect(jsonPath("$.content[0].description").value("Test Desc"));
    }

    @Test
    void testUpdateTransaction() throws Exception {
        Long id = 1L;
        UpdateTransactionRequest req = new UpdateTransactionRequest("Updated Desc");
        TransactionDto updatedDto = TransactionDto.builder()
                .id(id)
                .accountNumber(123456L)
                .trxnAmount(new BigDecimal("100.00"))
                .description("Updated Desc")
                .trxnTimestamp(LocalDateTime.now())
                .customerId(10L)
                .version(2)
                .build();

        when(transactionService.updateTransaction(eq(id), eq("Updated Desc")))
                .thenReturn(updatedDto);

        mockMvc.perform(put("/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.description").value("Updated Desc"));
    }
}