package com.maybank.maybank_assessment.service.spec;

import com.maybank.maybank_assessment.model.entity.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionSpecificationsTests {

    @Test
    void testHasCustomerId() {
        Root root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<Long> path = mock(Path.class);
        when(root.get("customerId")).thenReturn(path);
        Predicate predicate = mock(Predicate.class);
        when(cb.equal(path, 123L)).thenReturn(predicate);

        Specification<Transaction> spec = TransactionSpecifications.hasCustomerId(123L);
        assertEquals(predicate, spec.toPredicate(root, query, cb));
    }

    @Test
    void testHasAccountNumber() {
        Root root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<Long> path = mock(Path.class);
        when(root.get("accountNumber")).thenReturn(path);
        Predicate predicate = mock(Predicate.class);
        when(cb.equal(path, 456L)).thenReturn(predicate);

        Specification<Transaction> spec = TransactionSpecifications.hasAccountNumber(456L);
        assertEquals(predicate, spec.toPredicate(root, query, cb));
    }

    @Test
    void testDescriptionContains() {
        Root root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<String> path = mock(Path.class);
        Expression<String> expr = mock(Expression.class);
        when(root.get("description")).thenReturn(path);
        when(cb.lower(path)).thenReturn(expr);
        Predicate predicate = mock(Predicate.class);
        when(cb.like(expr, "%test%")).thenReturn(predicate);

        Specification<Transaction> spec = TransactionSpecifications.descriptionContains("Test");
        assertEquals(predicate, spec.toPredicate(root, query, cb));
    }

    @Test
    void testTimestampBetween() {
        Root root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<LocalDateTime> path = mock(Path.class);
        when(root.get("trxTimestamp")).thenReturn(path);
        Predicate predicate = mock(Predicate.class);
        LocalDateTime from = LocalDateTime.now().minusDays(1);
        LocalDateTime to = LocalDateTime.now();
        when(cb.between(path, from, to)).thenReturn(predicate);

        Specification<Transaction> spec = TransactionSpecifications.timestampBetween(from, to);
        assertEquals(predicate, spec.toPredicate(root, query, cb));
    }

    @Test
    void testAmountGte() {
        Root root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<BigDecimal> path = mock(Path.class);
        when(root.get("trxAmount")).thenReturn(path);
        Predicate predicate = mock(Predicate.class);
        BigDecimal min = new BigDecimal("10.00");
        when(cb.greaterThanOrEqualTo(path, min)).thenReturn(predicate);

        Specification<Transaction> spec = TransactionSpecifications.amountGte(min);
        assertEquals(predicate, spec.toPredicate(root, query, cb));
    }

    @Test
    void testAmountLte() {
        Root root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);

        Path<BigDecimal> path = mock(Path.class);
        when(root.get("trxAmount")).thenReturn(path);
        Predicate predicate = mock(Predicate.class);
        BigDecimal max = new BigDecimal("100.00");
        when(cb.lessThanOrEqualTo(path, max)).thenReturn(predicate);

        Specification<Transaction> spec = TransactionSpecifications.amountLte(max);
        assertEquals(predicate, spec.toPredicate(root, query, cb));
    }
}
