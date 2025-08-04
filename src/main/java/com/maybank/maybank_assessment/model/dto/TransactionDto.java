package com.maybank.maybank_assessment.model.dto;

import com.maybank.maybank_assessment.model.entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private Long accountNumber;
    private BigDecimal trxnAmount;
    private String description;
    private LocalDateTime trxnTimestamp;
    private Long customerId;
    private Integer version;

    public static TransactionDto fromEntity(Transaction txn) {
        return TransactionDto.builder()
                .id(txn.getId())
                .accountNumber(txn.getAccountNumber())
                .trxnAmount(txn.getTrxnAmount())
                .description(txn.getDescription())
                .trxnTimestamp(txn.getTrxnTimestamp())
                .customerId(txn.getCustomerId())
                .version(txn.getVersion())
                .build();
    }


}
