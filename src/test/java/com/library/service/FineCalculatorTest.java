package com.library.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class FineCalculatorTest {
    private final LocalDate due = LocalDate.of(2026, 12, 7);
    @Test void noFineWhenReturnedOnDueDate() { assertEquals(new BigDecimal("0.00"), FineCalculator.calculate(due,due)); }
    @Test void noFineWhenReturnedEarly() { assertEquals(new BigDecimal("0.00"), FineCalculator.calculate(due,due.minusDays(2))); }
    @Test void chargesFiveRupeesPerOverdueDay() { assertEquals(new BigDecimal("15.00"), FineCalculator.calculate(due,due.plusDays(3))); }
    @Test void rejectsNullDates() { assertThrows(IllegalArgumentException.class, () -> FineCalculator.calculate(null,due)); }
}
