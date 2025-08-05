package com.maybank.maybank_assessment.repository;

import com.maybank.maybank_assessment.model.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    boolean existsByAccountNumberAndTrxnAmountAndDescriptionAndTrxnTimestampAndCustomerId(
            Long accountNumber,
            BigDecimal trxnAmount,
            String description,
            LocalDateTime trxnTimestamp,
            Long customerId
    );
}
