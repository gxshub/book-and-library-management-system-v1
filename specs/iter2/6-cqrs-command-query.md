# CQRS Command and Query Specification

## 1. Purpose
This document defines how Library Service applies CQRS, including:
1. Separation between command and query application services.
2. Write/read model responsibilities.
3. User story mapping (`L1` to `L13`) to command or query.
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
- CRUD aggregates: `LoanAggregate`, `HoldQueueAggregate`, `FineAggregate`.

### 4.2 Read Model (Projections)
- `inventory_read_model`: driven by `InventoryAggregate` events. Used by L1, L2 queries.
- Loan/hold/fine reporting queries read directly from CRUD entity stores.

---

## 5. User Story Classification (L1-L13)

| User Story ID | User Story | CQRS Type | Through Aggregate |
| :--- | :--- | :--- | :--- |
| L1 | Find libraries by book ISBN | Query | No |
| L2 | Check book availability | Query | No |
| L3 | Borrow a book | Command | Yes |
| L4 | Return a book | Command | Yes |
| L5 | Place a hold | Command | Yes |
| L6 | Cancel a hold | Command | Yes |
| L7 | Renew a loan | Command | Yes |
| L8 | List active loans for a customer | Query | No |
| L9 | List overdue loans for a library | Query | No |
| L10 | Report lost or damaged copy | Command | Yes |
| L11 | Adjust inventory stock | Command | Yes |
| L12 | Transfer copies between libraries | Command | Yes |
| L13 | Pay or waive overdue fine | Command | Yes |

---

## 6. Command Catalog

| Command | Target Aggregate(s) | Story | Output Events | Persistence |
| :--- | :--- | :--- | :--- | :--- |
| `BorrowBookCommand` | `LoanAggregate` + `InventoryAggregate` | L3 | `BookBorrowed`, `InventoryCopyReserved` | CRUD + Event store |
| `ReturnBookCommand` | `LoanAggregate` + `InventoryAggregate` | L4 | `BookReturned`, `InventoryCopyReleased` | CRUD + Event store |
| `PlaceHoldCommand` | `HoldQueueAggregate` | L5 | `HoldPlaced` | CRUD |
| `CancelHoldCommand` | `HoldQueueAggregate` | L6 | `HoldCancelled` | CRUD |
| `RenewLoanCommand` | `LoanAggregate` | L7 | `LoanRenewed` | CRUD |
| `ReportLossOrDamageCommand` | `LoanAggregate` + `InventoryAggregate` | L10 | `CopyReportedLost` or `CopyReportedDamaged` | CRUD + Event store |
| `AdjustInventoryCommand` | `InventoryAggregate` | L11 | `InventoryAdjusted` | Event store |
| `TransferInventoryCommand` | `InventoryAggregate` (source + target) | L12 | `InventoryTransferredOut`, `InventoryTransferredIn` | Event store |
| `PayFineCommand` | `FineAggregate` | L13 | `FinePaid` | CRUD |
| `WaiveFineCommand` | `FineAggregate` | L13 | `FineWaived` | CRUD |

Command responses return outcome metadata (success/failure, IDs, version). They do not return read model projections.

---

## 7. Query Catalog

| Query | Story | Read Source | Consistency |
| :--- | :--- | :--- | :--- |
| `FindLibrariesByIsbnQuery(isbn)` | L1 | `inventory_read_model` | Eventual (bounded) |
| `CheckBookAvailabilityQuery(libraryId, isbn)` | L2 | `inventory_read_model` | Eventual (bounded) |
| `ListActiveLoansByCustomerQuery(customerId)` | L8 | Loan CRUD store | Strong (local DB) |
| `ListOverdueLoansByLibraryQuery(libraryId)` | L9 | Loan CRUD store | Strong (local DB) |
