package co.com.jhompo.util;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AmortizationUtilsTest {


    @Test
    @DisplayName("should handle zero monthly rate")
    void calculateMonthlyPayment_should_handle_zero_rate() {
        // Arrange
        BigDecimal amount = new BigDecimal("1200.00");
        BigDecimal monthlyRate = BigDecimal.ZERO;
        int months = 12;

        // Act
        BigDecimal monthlyPayment = AmortizationUtils.calculateMonthlyPayment(amount, monthlyRate, months);

        // Assert
        BigDecimal expectedPayment = new BigDecimal("100.00");
        assertEquals(expectedPayment, monthlyPayment);
    }


    @Test
    @DisplayName("should handle zero or negative months")
    void calculateMonthlyPayment_should_return_zero_for_invalid_months() {
        // Arrange
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal monthlyRate = new BigDecimal("0.015");
        int months = -5;

        // Act
        BigDecimal monthlyPayment = AmortizationUtils.calculateMonthlyPayment(amount, monthlyRate, months);

        // Assert
        BigDecimal expectedPayment = BigDecimal.ZERO;
        assertEquals(expectedPayment, monthlyPayment);
    }


    @Test
    @DisplayName("should handle zero months explicitly")
    void calculateMonthlyPayment_should_return_zero_for_zero_months() {
        // Arrange
        BigDecimal amount = new BigDecimal("1000.00");
        BigDecimal monthlyRate = new BigDecimal("0.015");
        int months = 0;

        // Act
        BigDecimal monthlyPayment = AmortizationUtils.calculateMonthlyPayment(amount, monthlyRate, months);

        // Assert
        BigDecimal expectedPayment = BigDecimal.ZERO;
        assertEquals(expectedPayment, monthlyPayment);
    }
}
