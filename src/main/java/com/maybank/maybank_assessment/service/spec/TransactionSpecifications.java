package com.maybank.maybank_assessment.service.spec;

import com.maybank.maybank_assessment.model.entity.Transaction;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Specifications for dynamic filtering of {@link Transaction} queries.
 *
 * Usage (typical):
 *   Specification<Transaction> spec = TransactionSpecifications.withFilters(customerId, accountNumber, description);
 *   Page<Transaction> page = repo.findAll(spec, pageable);
 *
 * Notes:
 *  - Each method returns a Specification that may yield null Predicate if the input is null/blank.
 *    Spring Data JPA will ignore null predicates when composing Specifications.
 *  - All "contains" string searches are case-insensitive (lowercased on both sides).
 */
public final class TransactionSpecifications {

    // Avoid magic strings; compile-time safety for field names
    private static final String F_CUSTOMER_ID   = "customerId";
    private static final String F_ACCOUNT_NO    = "accountNumber";
    private static final String F_DESCRIPTION   = "description";
    private static final String F_TRX_TIMESTAMP = "trxTimestamp";
    private static final String F_TRX_AMOUNT    = "trxAmount";

    private TransactionSpecifications() { }

    /** customerId = ? */
    public static Specification<Transaction> hasCustomerId(Long customerId) {
        return (root, query, cb) ->
                (customerId == null) ? null : cb.equal(root.get(F_CUSTOMER_ID), customerId);
    }

    /** accountNumber = ? */
    public static Specification<Transaction> hasAccountNumber(Long accountNumber) {
        return (accountNumber == null)
                ? null
                : (root, query, cb) -> cb.equal(root.get(F_ACCOUNT_NO), accountNumber);
    }

    /** lower(description) LIKE %value% (case-insensitive contains) */
    public static Specification<Transaction> descriptionContains(String value) {
        final String needle = normalize(value);
        return (root, query, cb) -> {
            if (needle == null) return null;
            Expression<String> field = cb.lower(root.get(F_DESCRIPTION));
            return cb.like(field, "%" + needle + "%");
        };
    }

    /** trxTimestamp between [from, to] (inclusive). Either bound may be null. */
    public static Specification<Transaction> timestampBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            if (from != null && to != null) {
                return cb.between(root.get(F_TRX_TIMESTAMP), from, to);
            }
            return (from != null)
                    ? cb.greaterThanOrEqualTo(root.get(F_TRX_TIMESTAMP), from)
                    : cb.lessThanOrEqualTo(root.get(F_TRX_TIMESTAMP), to);
        };
    }

    /** trxAmount >= min. Null-safe. */
    public static Specification<Transaction> amountGte(java.math.BigDecimal min) {
        return (root, query, cb) ->
                (min == null) ? null : cb.greaterThanOrEqualTo(root.get(F_TRX_AMOUNT), min);
    }

    /** trxAmount <= max. Null-safe. */
    public static Specification<Transaction> amountLte(java.math.BigDecimal max) {
        return (root, query, cb) ->
                (max == null) ? null : cb.lessThanOrEqualTo(root.get(F_TRX_AMOUNT), max);
    }

    /**
     * Convenience aggregator used by your service:
     * Combines customerId, accountNumber, and description filters.
     */
    // Java
    public static Specification<Transaction> withFilters(Long customerId,
                                                         Long accountNumber,
                                                         String description) {
        Specification<Transaction> spec = hasCustomerId(customerId);
        spec = spec.and(hasAccountNumber(accountNumber));
        spec = spec.and(descriptionContains(description));
        return spec;
    }

    /**
     * Extended aggregator (optional) if you expose date/amount filters later.
     * Shows how to keep the service layer clean as requirements grow.
     */
    // src/main/java/com/maybank/maybank_assessment/service/spec/TransactionSpecifications.java

    public static Specification<Transaction> withExtendedFilters(
            Long customerId,
            Long accountNumber,
            String description,
            LocalDateTime from,
            LocalDateTime to,
            java.math.BigDecimal minAmount,
            java.math.BigDecimal maxAmount
    ) {
        Specification<Transaction> spec = hasCustomerId(customerId);
        spec = spec.and(hasAccountNumber(accountNumber));
        spec = spec.and(descriptionContains(description));
        spec = spec.and(timestampBetween(from, to));
        spec = spec.and(amountGte(minAmount));
        spec = spec.and(amountLte(maxAmount));
        return spec;
    }

    // ---------- helpers ----------

    private static String normalize(String s) {
        if (s == null) return null;
        String trimmed = s.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }
}

