package com.maybank.maybank_assessment.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long accountNumber;
    private BigDecimal trxnAmount;
    private String description;

    // Store date and time as a single timestamp for better precision
    private LocalDateTime trxnTimestamp;

    private Long customerId;

    @Version
    private Integer version;

    private boolean isProcessed = false;
}
