package co.com.jhompo.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class AmortizationUtils {


    //FORMULA: Cuota = P * (i(1+i)expo(n) / (1+i)expo(n) − 1)
    // P = Monto
    // i = tasa de interes
    // n numero de periodos (meses)

    public static BigDecimal calculateMonthlyPayment(BigDecimal amount, BigDecimal monthlyRate, int months) {
        if (months <= 0) return BigDecimal.ZERO;
        if (monthlyRate.compareTo(BigDecimal.ZERO) == 0) {
            return amount.divide(BigDecimal.valueOf(months), 2, RoundingMode.HALF_UP);
        }

        BigDecimal onePlusI = monthlyRate.add(BigDecimal.ONE);
        BigDecimal pow = onePlusI.pow(months);
        BigDecimal numerator = amount.multiply(monthlyRate).multiply(pow);
        BigDecimal denominator = pow.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, 2, RoundingMode.HALF_UP);
    }
}
