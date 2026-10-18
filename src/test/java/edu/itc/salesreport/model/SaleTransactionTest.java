package edu.itc.salesreport.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SaleTransactionTest {

    @Test
    void roundsHalfUpToTwoDecimalPlaces() {
        // 3 * 0.335 = 1.005 -> 1.01 (scale 2, RoundingMode.HALF_UP)
        var tx = new SaleTransaction("PNH", LocalDate.of(2026, 9, 1), "PNH-001",
                "SKU-9999", "Test Item", "Stationery",
                3, new BigDecimal("0.335"), new BigDecimal("0.00"),
                PaymentMethod.CASH);

        assertEquals(new BigDecimal("1.01"), tx.revenue());
    }

    @Test
    void rejectsInvalidInvariants() {
        assertThrows(IllegalArgumentException.class, () ->
                new SaleTransaction("PNH", LocalDate.of(2026, 9, 1), "PNH-001",
                        "SKU-1", "Item", "Grocery", 0, new BigDecimal("5.00"),
                        BigDecimal.ZERO, PaymentMethod.CASH),
                "quantity must be > 0");

        assertThrows(IllegalArgumentException.class, () ->
                new SaleTransaction("PNH", LocalDate.of(2026, 9, 1), "PNH-001",
                        "SKU-1", "Item", "Grocery", 2, new BigDecimal("5.00"),
                        new BigDecimal("12.00"), PaymentMethod.CASH),
                "discount cannot exceed line total");

        assertThrows(NullPointerException.class, () ->
                new SaleTransaction(null, LocalDate.of(2026, 9, 1), "PNH-001",
                        "SKU-1", "Item", "Grocery", 1, new BigDecimal("5.00"),
                        BigDecimal.ZERO, PaymentMethod.CASH));
    }

    @Test
    void branchSummaryTopProductsIsUnmodifiable() {
        var products = new ArrayList<ProductTotal>();
        products.add(new ProductTotal("SKU-1001", "Jasmine Rice 5kg", new BigDecimal("13.00"), 2));

        var summary = new BranchSummary("PNH", new BigDecimal("100.00"), new BigDecimal("5.00"), 10,
                Map.of("Grocery", new BigDecimal("100.00")),
                products,
                Map.of(PaymentMethod.CASH, new BigDecimal("100.00")));

        var topProducts = summary.topProducts();
        assertThrows(UnsupportedOperationException.class, () ->
                topProducts.add(new ProductTotal("SKU-2001", "Water", new BigDecimal("1.00"), 1)));

        // Mutating the original input list does not affect the stored summary list
        products.add(new ProductTotal("SKU-2001", "Water", new BigDecimal("1.00"), 1));
        assertEquals(1, summary.topProducts().size());
    }

    @Test
    void branchSummaryAverageBasketComputesCorrectly() {
        var summary = new BranchSummary("REP", new BigDecimal("150.50"), BigDecimal.ZERO, 3,
                Map.of(), List.of(), Map.of());
        assertEquals(new BigDecimal("50.17"), summary.averageBasket());

        var emptySummary = new BranchSummary("BTB", BigDecimal.ZERO, BigDecimal.ZERO, 0,
                Map.of(), List.of(), Map.of());
        assertEquals(new BigDecimal("0.00"), emptySummary.averageBasket());
    }
}
