# Domain Models Specification - Library Service

Changes from `specs/3-domain-models.md`:
- Introduced DDD concepts: entities, value objects, aggregates, domain services, and events
- Replaced v1 CRUD-style models with richer domain models supporting new user stories (L5-L13)
- Added event-driven design for hold allocation workflows

## 1. DDD Building Blocks

### 1.1 Entities
- `LibraryBranch` _(existing `Library` evolution)_
- `InventoryItem` _(existing `BookInventory` evolution)_
- `Loan` _(existing `BorrowRecord` evolution)_
- `HoldRequest`
- `FineLedger`
- `StockMovement`

### 1.2 Value Objects
- `LibraryId`
- `Isbn`
- `CustomerId`
- `CopyCount` (validated non-negative quantity)
- `LoanPeriod` (start, due, renewalCount)
- `Money` (amount, currency)
- `PolicySnapshot` (loan/renew/fine rules used at decision time)
- `HoldPosition` (queue index + placedAt)

### 1.3 Aggregate Roots
1. `InventoryAggregate` (root: `InventoryItem`)
2. `LoanAggregate` (root: `Loan`)
3. `HoldQueueAggregate` (root: `HoldRequest` queue by `LibraryId + Isbn`)
4. `FineAggregate` (root: `FineLedger`)

### 1.4 Domain Services
- `CirculationPolicyService`: renew eligibility, due date calculation, loan caps.
- `FinePolicyService`: overdue/lost/damaged charge calculations.
- `HoldAllocationService`: allocates returned/available copy to queue head.

### 1.5 Domain Events
- `BookBorrowed`
- `BookReturned`
- `LoanRenewed`
- `LoanOverdueFlagged`
- `HoldPlaced`
- `HoldCancelled`
- `InventoryAdjusted`
- `InventoryTransferredOut`
- `InventoryTransferredIn`
- `CopyReportedLost`
- `CopyReportedDamaged`
- `FineAssessed`
- `FinePaid`
- `FineWaived`

---

## 2. Aggregate Specifications

### 2.1 InventoryAggregate (root: `InventoryItem`)

**Purpose**: Maintain stock invariants per `LibraryId + Isbn`.

| Field | Type        | Notes                            |
| :--- |:------------|:---------------------------------|
| `inventoryId` | `String`    | `{libraryId}:{isbn}` (as string) |
| `libraryId` | `LibraryId` | Branch identity                  |
| `isbn` | `Isbn`      | Book identity from Book Service  |
| `totalCopies` | `CopyCount` | Physical holdings                |
| `availableCopies` | `CopyCount` | Borrowable now                   |
| `version` | `long`      | Concurrency control              |

**Invariants**
1. `0 <= availableCopies <= totalCopies`
2. Borrow decreases `availableCopies` by exactly 1.
3. Return increases `availableCopies` by exactly 1 unless copy marked lost/damaged.

**Behaviors**
- `reserveCopyForLoan()`
- `releaseCopyFromReturn()`
- `adjustStock(reason, delta)`
- `transferOut(quantity, transferId)`
- `transferIn(quantity, transferId)`

---

### 2.2 LoanAggregate (root: `Loan`)

**Purpose**: Lifecycle of borrowing transactions.

| Field | Type | Notes |
| :--- | :--- | :--- |
| `loanId` | `String` | Business identity (e.g., REC-xxxx) |
| `libraryId` | `LibraryId` | Borrow branch |
| `isbn` | `Isbn` | Borrowed title |
| `customerId` | `CustomerId` | Borrower |
| `loanPeriod` | `LoanPeriod` | borrowedAt, dueDate, renewalCount |
| `returnedAt` | `LocalDateTime?` | Nullable before return |
| `status` | `LoanStatus` | `BORROWED`, `RETURNED`, `OVERDUE`, `LOST`, `DAMAGED` |
| `policySnapshot` | `PolicySnapshot` | Rules captured at checkout |

**Invariants**
1. Only `BORROWED/OVERDUE` loans can be renewed.
2. Returned loan cannot be renewed or returned again.
3. Lost/damaged loans are terminal unless reconciled by policy workflow.

**Behaviors**
- `markReturned(at)`
- `renew(now, policyDecision)`
- `markOverdue(now)`
- `reportLost(now)`
- `reportDamaged(now)`

---

### 2.3 HoldQueueAggregate (root: hold queue for `LibraryId + Isbn`)

**Purpose**: Reservation queue and allocation order.

| Field | Type | Notes |
| :--- | :--- | :--- |
| `queueKey` | `LibraryId + Isbn` | Aggregate identity |
| `holds` | `List<HoldRequest>` | Ordered queue |

`HoldRequest` entity fields:
- `holdId`, `customerId`, `placedAt`, `status`, `readyAt`, `expiresAt`, `position`.

**Invariants**
1. No duplicate active hold for same customer within same queue.
2. Queue order is FIFO by `placedAt` (stable).
3. Only queue head can be allocated when a copy becomes available.

**Behaviors**
- `placeHold(customerId, at)`
- `cancelHold(holdId, at)`
- `allocateNextAvailableCopy(at)`

---

### 2.4 FineAggregate (root: `FineLedger`)

**Purpose**: Track assessed and settled monetary obligations.

| Field | Type | Notes |
| :--- | :--- | :--- |
| `fineId` | `String` | Identity |
| `loanId` | `String` | Associated loan |
| `customerId` | `CustomerId` | Debtor |
| `assessed` | `Money` | Total charged |
| `paid` | `Money` | Total paid |
| `waived` | `Money` | Total waived |
| `status` | `FineStatus` | `OPEN`, `SETTLED`, `WAIVED` |

**Invariant**
1. `balance = assessed - paid - waived`, and balance cannot be negative.

**Behaviors**
- `assess(amount, reason, at)`
- `recordPayment(amount, at)`
- `waive(amount, reason, operatorId, at)`

---

## 3. Domain Events and Story Mapping

| Event | Raised By | Trigger | Supports Stories |
| :--- | :--- | :--- | :--- |
| `BookBorrowed` | `LoanAggregate` + `InventoryAggregate` | Successful checkout | L3 |
| `BookReturned` | `LoanAggregate` + `InventoryAggregate` | Successful return | L4 |
| `HoldPlaced` / `HoldCancelled` | `HoldQueueAggregate` | Hold lifecycle changes | L5, L6 |
| `LoanRenewed` | `LoanAggregate` | Successful renewal | L7 |
| `LoanOverdueFlagged` | `LoanAggregate` | Due date crossed | L9 |
| `CopyReportedLost`, `CopyReportedDamaged` | `LoanAggregate` | Lost or damaged report | L10 |
| `InventoryAdjusted` | `InventoryAggregate` | Manual stock adjustment | L11 |
| `InventoryTransferredOut`, `InventoryTransferredIn` | `InventoryAggregate` | Cross-branch transfer movements | L12 |
| `FineAssessed`, `FinePaid`, `FineWaived` | `FineAggregate` | Fine lifecycle | L13 |
