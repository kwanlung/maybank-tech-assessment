package com.maybank.maybank_assessment.controller;

import com.maybank.maybank_assessment.model.dto.TransactionDto;
import com.maybank.maybank_assessment.model.dto.UpdateTransactionRequest;
import com.maybank.maybank_assessment.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping
    public Page<TransactionDto> listTransactions(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long accountNumber,
            @RequestParam(required = false) String description,
            @PageableDefault(size = 20)Pageable pageable
            ){
        return transactionService.getTransactions(customerId, accountNumber, description, pageable);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDto> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionRequest updateRequest) {
        TransactionDto updated = transactionService.updateTransaction(id, updateRequest.getDescription());
        return ResponseEntity.ok(updated);
    }
}
