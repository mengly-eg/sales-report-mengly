package edu.itc.salesreport.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Aggregated sales metric for a single product SKU.
 */
public record ProductTotal(String sku, String productName,
        BigDecimal revenue, long quantity) {

    public ProductTotal {
        Objects.requireNonNull(sku, "sku");
        Objects.requireNonNull(productName, "productName");
        Objects.requireNonNull(revenue, "revenue");

        if (revenue.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("revenue cannot be negative");
        }
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity cannot be negative");
        }
    }
}
