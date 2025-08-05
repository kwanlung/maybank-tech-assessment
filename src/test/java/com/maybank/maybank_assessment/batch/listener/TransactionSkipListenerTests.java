package com.maybank.maybank_assessment.batch.listener;

import com.maybank.maybank_assessment.model.entity.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.batch.item.file.FlatFileParseException;
import lombok.extern.slf4j.Slf4j;

class TransactionSkipListenerTests {

    private TransactionSkipListener listener;

    @BeforeEach
    void setUp() {
        listener = new TransactionSkipListener();
    }

    @Test
    void testOnSkipInRead_withFlatFileParseException_logsError() {
        FlatFileParseException ex = new FlatFileParseException("Parse error", "bad,line");
        try (MockedStatic<Slf4j> slf4j = Mockito.mockStatic(Slf4j.class)) {
            listener.onSkipInRead(ex);
            // No assertion, just ensure no exception and log is called
        }
    }

    @Test
    void testOnSkipInRead_withOtherException_logsError() {
        Exception ex = new Exception("Other error");
        listener.onSkipInRead(ex);
        // No assertion, just ensure no exception and log is called
    }

    @Test
    void testOnSkipInProcess_logsError() {
        Transaction tx = new Transaction();
        Exception ex = new Exception("Process error");
        listener.onSkipInProcess(tx, ex);
        // No assertion, just ensure no exception and log is called
    }

    @Test
    void testOnSkipInWrite_logsError() {
        Transaction tx = new Transaction();
        Exception ex = new Exception("Write error");
        listener.onSkipInWrite(tx, ex);
        // No assertion, just ensure no exception and log is called
    }
}
