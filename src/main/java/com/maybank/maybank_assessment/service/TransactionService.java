package com.maybank.maybank_assessment.service;

import com.maybank.maybank_assessment.model.dto.TransactionDto;
import com.maybank.maybank_assessment.model.entity.Transaction;
import com.maybank.maybank_assessment.repository.TransactionRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.maybank.maybank_assessment.service.spec.TransactionSpecifications.withFilters;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public Page<TransactionDto> getTransactions(Long customerId, Long accountNumber, String description, Pageable pageable) {
        // Build JPA Specification dynamically based on filters:
        var spec = withFilters(customerId, accountNumber, description);
        Page<Transaction> page = transactionRepository.findAll(spec, pageable);
        // Map entities to DTOs
        return page.map(TransactionDto::fromEntity);
    }

    @Transactional
    public TransactionDto updateTransaction(Long id, String newDescription) {
        Transaction txn = transactionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Transaction not found with id " + id));
        txn.setDescription(newDescription);
        // Save will trigger optimistic lock check via @Version
        Transaction updated = transactionRepository.save(txn);
        return TransactionDto.fromEntity(updated);
    }
}
