# ADR-0002: Daily SFTP Batch Push with Idempotent Reconciliation

Status: Accepted  
Date: 2026-10-18 · Deciders: Mengly Eang, Engineering Team

## Context
Each Angkor Mart branch produces approximately 33,000 daily transaction rows. These records must be transmitted from provincial store locations (PNH, REP, BTB) across public internet links to the central head office in Phnom Penh.
Forces driving this decision:
- **QA-3 (Availability):** Provincial internet links (notably Siem Reap and Battambang) suffer intermittent network interruptions, causing daily files to arrive late.
- **QA-5 (Security):** Transaction and financial data traversing public telecommunications networks must be protected against tampering and eavesdropping.
- **QA-1 (Performance):** The head-office batch engine must access data files locally to achieve high throughput without distributed query latency.

## Decision
We will establish a **Daily SFTP Push** model. At 23:00 each evening, local POS batch scripts at each branch export a single UTF-8 CSV file (`data/<yyyy-MM>/<BRANCH>-<yyyy-MM-dd>.csv`) and push it via SFTP (SSH Key authenticated) directly into the head-office landing zone.

### Partial-Failure & Idempotent Re-Run Policy (QA-3)
1. **Scheduled Batch & Grace Period:** At 06:00 on day 2 of month M+1, the head-office loader inspects the landing zone. If any branch file is missing, the system enters a 15-minute grace period until 06:15.
2. **Partial Report Publication:** If a branch file remains missing at 06:15, the system automatically generates and publishes a **provisional partial report** containing all available branches. The missing branch is explicitly flagged in `MonthlyReport.anomalies()` (e.g., `"MISSING: REP-2026-09-02.csv not received"`).
3. **Late File Arrival & Reconciliation:** When the late file arrives via SFTP, a file watcher triggers an automatic reconciliation job. The batch re-executes, merges the delayed branch records, and publishes an updated consolidated report.
4. **Idempotent Storage:** Re-runs are strictly idempotent. The storage key is the deterministic compound identifier `(branch, date, receipt_no, sku)`. Re-uploading a file or re-running the loader overwrites existing staged records for that specific file instead of appending, preventing duplicate sales or revenue inflation.

## Alternatives considered
- **Central Head-Office Pull (Polling POS Servers):**  
  Attractive because head office controls the collection schedule. Rejected because retail branches sit behind dynamic NAT/firewalls, requiring reverse VPN tunnels or opening inbound firewall ports at every store, creating substantial security vulnerabilities (**QA-5**).
- **Per-Receipt Real-Time Message Streaming (e.g., Apache Kafka / RabbitMQ):**  
  Attractive for instant live visibility. Rejected because intermittent WAN outages in provincial branches would stall point-of-sale checkout registers or require complex distributed offline buffers at every retail checkout terminal.

## Consequences
- `+` **QA-3:** Store checkouts operate 100% offline; network outages do not disrupt store sales. Late files are reconciled automatically without human intervention.
- `+` **QA-5:** SFTP provides proven SSH cryptographic transport security with mutual public-key authentication.
- `−` **QA-3:** Head office cannot actively force a transfer if the remote branch POS server is powered off or disconnected.
- `−` **Data Freshness:** Business stakeholders only receive daily batched data rather than real-time intra-day metrics.

Revisit when: Angkor Mart leadership introduces omnichannel intra-day inventory management requiring live receipt streaming, or when all store branches obtain redundant fiber connections with 99.99% uptime guarantees.
