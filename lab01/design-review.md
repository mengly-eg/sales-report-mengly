# Design Review: Angkor Mart Monthly Sales Report

## 1. Package Responsibilities (Cohesion)

- **`edu.itc.salesreport.model`:** Encapsulates the immutable canonical domain entities, records, and fundamental financial invariants (`BigDecimal` scale 2 arithmetic and non-negative constraints) with zero external project dependencies.
- **`edu.itc.salesreport.ingest`:** Coordinates filesystem scanning, batch row parsing, error cleansing, and progress notification to registered observers via the Observer pattern.
- **`edu.itc.salesreport.render`:** Implements formatting strategies (Strategy pattern) to transform aggregated monthly reports into specific text and tabular representations (TXT, HTML, CSV).

---

## 2. Package Coupling & Instability Metrics

> **Definitions:**
> - **$C_a$ (Afferent Coupling):** Number of external project packages depending on this package.
> - **$C_e$ (Efferent Coupling):** Number of external project packages this package depends on.
> - **$I$ (Instability):** $I = \frac{C_e}{C_a + C_e}$, ranging from $0.0$ (maximally stable, hard to change) to $1.0$ (maximally unstable, easy to change).
> *(Note: Standard JDK library packages are excluded from coupling metrics).*

| Package | Responsibility Summary | $C_a$ | $C_e$ | Instability ($I$) | Formula & Arithmetic |
|:---|:---|:---:|:---:|:---:|:---|
| **`model`** | Core domain records & invariants | 2 | 0 | **0.00** | $I = \frac{0}{2 + 0} = 0.00$ *(Maximally Stable)* |
| **`ingest`** | CSV parsing & observer loader | 1 | 1 | **0.50** | $I = \frac{1}{1 + 1} = 0.50$ *(Balanced)* |
| **`render`** | Output report formatting strategies | 0 | 1 | **1.00** | $I = \frac{1}{0 + 1} = 1.00$ *(Maximally Unstable)* |
| **`salesreport` (root)** | Composition root (`App.java`) | 0 | 1 | **1.00** | $I = \frac{1}{0 + 1} = 1.00$ *(Application Root)* |

---

## 3. Accepted Coupling Analysis

We explicitly accept the efferent coupling from **`render` $\rightarrow$ `model`** (and **`ingest` $\rightarrow$ `model`**):
- **Rationale:** The `model` package has an instability score of $I = 0.00$, making it the most stable architectural core of the entire application. It contains immutable records (`SaleTransaction`, `MonthlyReport`, `BranchSummary`) with zero incoming domain volatility. 
- In accordance with the **Stable Dependencies Principle (SDP)**, dependencies must always point in the direction of stability ($I_{\text{source}} \ge I_{\text{target}}$). Both `render` ($I = 1.0$) and `ingest` ($I = 0.5$) depend on `model` ($I = 0.0$), ensuring an acyclic dependency graph without backward contamination.

---

## 4. Architectural Analysis: Sealed Hierarchy vs. Open Interface for QA-4

Scenario **QA-4 (Modifiability)** requires adding a new report export format in $\le 2$ person-days while touching $\le 2$ existing files.

### Comparison:
1. **Sealed Interface (`sealed interface ReportRenderer permits ...`):**
   - **Advantages:** Enables compile-time exhaustiveness checking in Java pattern matching (`switch` expressions require no `default` clause), guaranteeing that any switch statement covering renderers will break at compile time if an unhandled format is introduced.
   - **Disadvantages:** Violates the Open/Closed Principle (OCP) across module boundaries. Every new renderer requires modifying the `permits` clause of `ReportRenderer` and adding a new case branch to the static `forExtension` factory method.
2. **Open Interface (`public interface ReportRenderer`):**
   - **Advantages:** Better aligns with true pluggability and the Open/Closed Principle. New formats (e.g., PDF or Excel in Lab-08) can be authored in separate packages or loaded dynamically at runtime via `java.util.ServiceLoader` (SPI) without modifying a single line of existing code.

### Conclusion:
For **Release 1**, the sealed hierarchy is sufficient and provides superior type-safety within a single cohesive package. However, to fully satisfy **QA-4** as Angkor Mart scales and third-party or optional export plugins (e.g., Apache POI for XLSX, OpenPDF for PDF) are introduced, transitioning to an **open interface with a dynamic registry / ServiceLoader** will be the superior architectural choice.
