package com.maybank.maybank_assessment.batch;

import com.maybank.maybank_assessment.model.entity.Transaction;
import com.maybank.maybank_assessment.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BatchConfigTests {

    private TransactionRepository transactionRepository;
    private BatchConfig batchConfig;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        batchConfig = new BatchConfig(null, transactionRepository);
    }

    @Test
    void testTransactionProcessor_deduplicationAndValidation() throws Exception {
        var processor = batchConfig.transactionProcessor();

        Transaction tx1 = Transaction.builder()
                .accountNumber(1L)
                .trxnAmount(BigDecimal.valueOf(100))
                .description("desc")
                .trxnTimestamp(LocalDateTime.now())
                .customerId(2L)
                .build();

        when(transactionRepository.existsByAccountNumberAndTrxnAmountAndDescriptionAndTrxnTimestampAndCustomerId(
                anyLong(), any(), anyString(), any(), anyLong())).thenReturn(false);

        // First time: should process
        Transaction result1 = processor.process(tx1);
        assertNotNull(result1);
        assertTrue(result1.isProcessed());

        // Duplicate in-memory: should skip (return null)
        Transaction result2 = processor.process(tx1);
        assertNull(result2);

        // New transaction, but negative amount: should throw
        Transaction tx2 = Transaction.builder()
                .accountNumber(1L)
                .trxnAmount(BigDecimal.valueOf(-10))
                .description("desc")
                .trxnTimestamp(LocalDateTime.now())
                .customerId(2L)
                .build();

        assertThrows(IllegalArgumentException.class, () -> processor.process(tx2));
    }

    @Test
    void testTransactionProcessor_dbDuplicate() throws Exception {
        var processor = batchConfig.transactionProcessor();

        Transaction tx = Transaction.builder()
                .accountNumber(1L)
                .trxnAmount(BigDecimal.valueOf(100))
                .description("desc")
                .trxnTimestamp(LocalDateTime.now())
                .customerId(2L)
                .build();

        when(transactionRepository.existsByAccountNumberAndTrxnAmountAndDescriptionAndTrxnTimestampAndCustomerId(
                anyLong(), any(), anyString(), any(), anyLong())).thenReturn(true);

        // Should skip if already in DB
        Transaction result = processor.process(tx);
        assertNull(result);
    }
}
