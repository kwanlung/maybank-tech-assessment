package com.maybank.maybank_assessment.service;

import com.maybank.maybank_assessment.model.dto.TransactionDto;
import com.maybank.maybank_assessment.model.entity.Transaction;
import com.maybank.maybank_assessment.repository.TransactionRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TransactionServiceTests {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetTransactions() {
        Transaction txn = Transaction.builder()
                .id(1L)
                .accountNumber(123456L)
                .trxnAmount(new BigDecimal("100.00"))
                .description("Test Desc")
                .trxnTimestamp(LocalDateTime.now())
                .customerId(10L)
                .version(1)
                .build();
        Page<Transaction> page = new PageImpl<>(List.of(txn));
        when(transactionRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class))).thenReturn(page);

        Page<TransactionDto> result = transactionService.getTransactions(10L, 123456L, "Test Desc", PageRequest.of(0, 20));
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Desc", result.getContent().get(0).getDescription());
    }

    @Test
    void testUpdateTransaction_Success() {
        Transaction txn = Transaction.builder()
                .id(1L)
                .accountNumber(123456L)
                .trxnAmount(new BigDecimal("100.00"))
                .description("Old Desc")
                .trxnTimestamp(LocalDateTime.now())
                .customerId(10L)
                .version(1)
                .build();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(txn));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionDto result = transactionService.updateTransaction(1L, "New Desc");
        assertEquals("New Desc", result.getDescription());
    }

    @Test
    void testUpdateTransaction_NotFound() {
        when(transactionRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> transactionService.updateTransaction(2L, "Desc"));
    }
}
