package edu.itc.salesreport.model;

import java.time.YearMonth;
import java.util.List;
import java.util.Objects;

/**
 * Consolidated monthly sales report containing individual branch summaries,
 * overall chain summary, and detected data/statistical anomalies.
 */
public record MonthlyReport(YearMonth month, List<BranchSummary> branches,
        BranchSummary chain, List<String> anomalies) {

    public MonthlyReport {
        Objects.requireNonNull(month, "month");
        Objects.requireNonNull(branches, "branches");
        Objects.requireNonNull(chain, "chain");
        Objects.requireNonNull(anomalies, "anomalies");

        // Information hiding: store unmodifiable defensive copies
        branches = List.copyOf(branches);
        anomalies = List.copyOf(anomalies);
    }
}
