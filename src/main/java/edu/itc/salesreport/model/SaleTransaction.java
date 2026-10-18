package edu.itc.salesreport.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * One line of a branch receipt, exactly as exported by the POS.
 */
public record SaleTransaction(String branch, LocalDate date,
        String receiptNo, String sku, String productName,
        String category, int quantity, BigDecimal unitPrice,
        BigDecimal discount, PaymentMethod paymentMethod) {

    public SaleTransaction {
        Objects.requireNonNull(branch, "branch");
        Objects.requireNonNull(date, "date");
        Objects.requireNonNull(receiptNo, "receiptNo");
        Objects.requireNonNull(sku, "sku");
        Objects.requireNonNull(productName, "productName");
        Objects.requireNonNull(category, "category");
        Objects.requireNonNull(unitPrice, "unitPrice");
        Objects.requireNonNull(discount, "discount");
        Objects.requireNonNull(paymentMethod, "paymentMethod");

        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be > 0");
        }
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("unitPrice must be >= 0");
        }
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("discount must be >= 0");
        }
        BigDecimal lineTotal = BigDecimal.valueOf(quantity).multiply(unitPrice);
        if (discount.compareTo(lineTotal) > 0) {
            throw new IllegalArgumentException("discount cannot exceed line total");
        }
    }

    /**
     * Line revenue = quantity * unitPrice - discount, computed with BigDecimal, scale 2, HALF_UP.
     */
    public BigDecimal revenue() {
        return BigDecimal.valueOf(quantity)
                .multiply(unitPrice)
                .subtract(discount)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
