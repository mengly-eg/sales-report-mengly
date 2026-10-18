package edu.itc.salesreport.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Summary metrics aggregated for a single branch or across the entire retail chain.
 */
public record BranchSummary(String branch, BigDecimal revenue,
        BigDecimal discounts, long receipts,
        Map<String, BigDecimal> revenueByCategory,
        List<ProductTotal> topProducts,
        Map<PaymentMethod, BigDecimal> revenueByPayment) {

    public BranchSummary {
        Objects.requireNonNull(branch, "branch");
        Objects.requireNonNull(revenue, "revenue");
        Objects.requireNonNull(discounts, "discounts");
        Objects.requireNonNull(revenueByCategory, "revenueByCategory");
        Objects.requireNonNull(topProducts, "topProducts");
        Objects.requireNonNull(revenueByPayment, "revenueByPayment");

        if (revenue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("revenue cannot be negative");
        }
        if (discounts.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("discounts cannot be negative");
        }
        if (receipts < 0) {
            throw new IllegalArgumentException("receipts cannot be negative");
        }

        // Information hiding: store unmodifiable defensive copies
        revenueByCategory = Map.copyOf(revenueByCategory);
        topProducts = List.copyOf(topProducts);
        revenueByPayment = Map.copyOf(revenueByPayment);
    }

    /**
     * Average basket value = revenue / receipts, scale 2, HALF_UP.
     * Returns 0.00 when there are no receipts.
     */
    public BigDecimal averageBasket() {
        if (receipts == 0) {
            return new BigDecimal("0.00");
        }
        return revenue.divide(BigDecimal.valueOf(receipts), 2, RoundingMode.HALF_UP);
    }
}
