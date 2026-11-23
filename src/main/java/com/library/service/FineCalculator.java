package com.library.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Pure fine calculation: Rs. 5 for each calendar day after the due date. */
public final class FineCalculator {
    private static final BigDecimal RATE_PER_DAY = new BigDecimal("5.00");
    private FineCalculator() {}

    public static long overdueDays(LocalDate dueDate, LocalDate returnDate) {
        if (dueDate == null || returnDate == null) throw new IllegalArgumentException("Dates cannot be null.");
        return Math.max(0, ChronoUnit.DAYS.between(dueDate, returnDate));
    }

    public static BigDecimal calculate(LocalDate dueDate, LocalDate returnDate) {
        return RATE_PER_DAY.multiply(BigDecimal.valueOf(overdueDays(dueDate, returnDate)));
    }
}
