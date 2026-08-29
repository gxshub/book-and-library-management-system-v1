# CQRS Command and Query Specification

## 1. Purpose
This document defines how Library Service applies CQRS, including:
1. Separation between command and query application services.
2. Write/read model responsibilities.
3. User story mapping (`L1` to `L11`) to command or query.
4. Command and query catalogs.
5. Handler wiring and read model update flow.
6. Consistency model and error model.

---

## 2. CQRS Principles
1. **Commands mutate state** and execute domain invariants through aggregates.
2. **Queries read state** from read models/projections and do not mutate domain state.
3. Write and read models may use different schemas optimized for their workloads.
4. Read side may be eventually consistent with the write side.

---

## 3. Application Service Split

### 3.1 Command Application Service
- Accepts command requests from API endpoints.
- Routes to command handlers.
- Loads and executes aggregate behavior.
- Persists changes (event append or CRUD write).
- Publishes domain events for read model updates and integrations.

### 3.2 Query Application Service
- Accepts read requests from API endpoints.
- Routes to query handlers.
- Reads from projection/read stores only.
- Shapes response DTOs for API contracts.

### Constraint
- Queries must not call domain mutation methods.

---

## 4. Write and Read Models

### 4.1 Write Model (Aggregates)
- Event-sourced aggregate: `InventoryAggregate`.
- CRUD aggregates: `LoanAggregate`, `FineAggregate`.

### 4.2 Read Model (Projections)
- `inventory_read_model`: driven by `InventoryAggregate` events. Used by L1, L2 queries.
- Loan/fine reporting queries read directly from CRUD entity stores.

---

## 5. User Story Classification (L1-L11)

| User Story ID | User Story | CQRS Type | Through Aggregate |
|:--------------| :--- | :--- | :--- |
| L1            | Find libraries by book ISBN | Query | No |
| L2            | Check book availability | Query | No |
| L3            | Borrow a book | Command | Yes |
| L4            | Return a book | Command | Yes |
| L5            | Renew a loan | Command | Yes |
| L6            | List active loans for a customer | Query | No |
| L7            | List overdue loans for a library | Query | No |
| L8            | Report lost or damaged copy | Command | Yes |
| L9            | Adjust inventory stock | Command | Yes |
| L10           | Transfer copies between libraries | Command | Yes |
| L11           | Pay overdue fine | Command | Yes |

---

## 6. Command Catalog

| Command | Target Aggregate(s) | Story | Output Events | Persistence |
| :--- | :--- | :--- | :--- | :--- |
| `BorrowBookCommand` | `LoanAggregate` + `InventoryAggregate` | L3 | `BookBorrowed`, `InventoryCopyReserved` | CRUD + Event store |
| `ReturnBookCommand` | `LoanAggregate` + `InventoryAggregate` | L4 | `BookReturned`, `InventoryCopyReleased` | CRUD + Event store |
| `RenewLoanCommand` | `LoanAggregate` | L5 | `LoanRenewed` | CRUD |
| `ReportLossOrDamageCommand` | `LoanAggregate` + `InventoryAggregate` | L8 | `CopyReportedLost` or `CopyReportedDamaged` | CRUD + Event store |
| `AdjustInventoryCommand` | `InventoryAggregate` | L9 | `InventoryInitialized` (first call) + `InventoryAdjusted` | Event store |
| `TransferInventoryCommand` | `InventoryAggregate` (source + target) | L10 | `InventoryTransferredOut`, `InventoryTransferredIn` | Event store |
| `PayFineCommand` | `FineAggregate` | L11 | `FinePaid` | CRUD |

Command responses return outcome metadata (success/failure, IDs, version). They do not return read model projections.

---

## 7. Query Catalog

| Query | Story | Read Source | Consistency | Notes |
| :--- | :--- | :--- | :--- | :--- |
| `FindLibrariesByIsbnQuery(isbn)` | L1 | `inventory_read_model` | Eventual (bounded) | |
| `CheckBookAvailabilityQuery(libraryId, isbn)` | L2 | `inventory_read_model` | Eventual (bounded) | |
| `ListActiveLoansByCustomerQuery(customerId)` | L6 | Loan CRUD store | Strong (local DB) | Returns loans with status `BORROWED` or `OVERDUE` (API filter `status=ACTIVE`) |
| `ListOverdueLoansByLibraryQuery(libraryId)` | L7 | Loan CRUD store | Strong (local DB) | |
