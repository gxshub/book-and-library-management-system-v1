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


---
<!--
## 8. Handler Wiring

### 8.1 Command Flow
```
Controller
  → CommandApplicationService
    → CommandHandler
      → load Aggregate (from EventStore or CRUDRepository)
      → Aggregate.behavior() — enforces invariants, raises domain events
      → persist: EventStore.append(events) or CRUDRepository.save()
      → ProjectionUpdater.apply(events)  [synchronous, same transaction — Phase 1]
  → return CommandResult (outcome metadata)
```

### 8.2 Query Flow
```
Controller
  → QueryApplicationService
    → QueryHandler
      → ReadModelRepository.query() or CRUDRepository.findBy()
  → return DTO (read model shape)
```

### 8.3 Projection Update (InventoryAggregate)
- Triggered by `InventoryAggregate` events immediately after event store append.
- Updates `inventory_read_model` within the same transaction (Phase 1).
- See `5-event-sourcing.md §6` for the full projection update table.

---

## 9. Consistency Model

| Query | Consistency | Reason |
| :--- | :--- | :--- |
| L1, L2 (inventory queries) | **Eventual** | Read from `inventory_read_model` projection. Phase 1 synchronous update makes lag near-zero in practice. |
| L8, L9 (loan queries) | **Strong** | Read directly from the CRUD loan store written by command handlers. |

Rules:
1. APIs serving L1/L2 should document that results reflect the last committed projection state.
2. Flows where the caller immediately queries after a command (e.g., borrow then check availability) may observe the updated state only if the projection update committed in the same transaction.
3. Optimistic lock conflicts on `InventoryAggregate` must be surfaced as `InventoryConflict` (409) — callers should reload and retry.

---

## 10. Error Model

### Command-Side Errors

| Error Code | Raised By | Scenario |
| :--- | :--- | :--- |
| `InventoryConflict` | Event store | `expectedVersion` mismatch on `InventoryAggregate` append |
| `InventoryInvariantViolation` | `InventoryAggregate` | `availableCopies` would go below 0 or exceed `totalCopies` |
| `LoanPolicyViolation` | `LoanAggregate` / `CirculationPolicyService` | Renewal denied (hold queue exists, max renewals reached, wrong status) |
| `HoldStateViolation` | `HoldQueueAggregate` | Duplicate active hold, or cancel of non-active hold |
| `FineStateViolation` | `FineAggregate` | Payment/waiver exceeds balance, or waiver by non-librarian |
| `ResourceNotFound` | Repository load | Aggregate not found for given ID |

### Query-Side Errors

| Error Code | Raised By | Scenario |
| :--- | :--- | :--- |
| `ResourceNotFound` | Read model repository | No record found for the given identifiers |

All errors map to HTTP responses per `4-api-endpoints-v2.md §4`.

---

## 11. Relationship to Other Specs
- User stories: `specs/iter2/2-user-stories-library-service-v2.md`
- Domain rules and aggregates: `specs/iter2/3-domain-models-library-service-v2.md`
- Event sourcing mechanics and event contracts: `specs/iter2/5-event-sourcing.md`
- API contracts and error HTTP mapping: `specs/iter2/4-api-endpoints-v2.md`
- High-level architecture: `specs/iter2/1-technical-architecture-v2.md`

-->