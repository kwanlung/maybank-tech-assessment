package com.maybank.maybank_assessment.batch.listener;

import com.maybank.maybank_assessment.model.entity.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TransactionSkipListener implements SkipListener<Transaction, Transaction> {
    @Override
    public void onSkipInRead(Throwable t) {
        if (t instanceof FlatFileParseException ffpe) {
            String badLine = ffpe.getInput();
            log.error("Skipping malformed record: \"{}\". Error: {}", badLine, ffpe.getMessage());
        } else {
            log.error("Skip in read due to: {}", t.getMessage(), t);
        }
    }
    @Override
    public void onSkipInProcess(Transaction item, Throwable t) {
        log.error("Skipping invalid transaction {} due to error: {}", item, t.getMessage());
    }
    @Override
    public void onSkipInWrite(Transaction item, Throwable t) {
        log.error("Failed to write transaction {}. Error: {}", item, t.getMessage());
    }
}
