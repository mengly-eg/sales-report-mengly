# C4 Architecture Notes: Angkor Mart Monthly Sales Report

## C4 Context Diagram Notes (`lab01/c4-context.mmd`)
1. **What the diagram deliberately does not show:**  
   The context diagram deliberately omits internal container boundaries, data storage implementations, database schemas, internal Java components, and intermediate data cleansing steps.
2. **Quality-attribute scenarios served:**  
   The context model directly reflects **QA-3 (Availability)** by explicitly capturing the vulnerable external SFTP boundary where branch network failures delay daily uploads, and **QA-5 (Security)** by partitioning access boundaries between branch managers (restricted to local branch figures) and head-office analysts (full chain visibility).
3. **Open question for the client:**  
   Will the Finance Director require the full monthly PDF report attached directly to the automated completion e-mail, or should the e-mail only contain an authenticated intranet hyperlink to the generated artifact?

---

## C4 Container Diagram Notes (`lab01/c4-container.mmd`)
1. **What the diagram deliberately does not show:**  
   The container diagram deliberately omits internal Java class designs, package modularity (`model`, `ingest`, `engine`, `render`), data structures, and multithreading primitives used during batch aggregation.
2. **Quality-attribute scenarios served:**  
   The **Report Batch Engine** and **Landing Zone** containers directly support **QA-1 (Performance)** by streaming parsed records through memory-efficient batch pipelines without saturating heap memory, while the **Report Batch Engine**'s decoupled export interfaces support **QA-4 (Modifiability)** by enabling new report formats to be added without modifying the core calculation engine.
3. **Open question for the client:**  
   For the **Web Report Portal** accessed by branch managers, should user authentication integrate with an existing central corporate identity provider (e.g., LDAP/OAuth2), or should the initial release rely on basic role-based configuration files?
