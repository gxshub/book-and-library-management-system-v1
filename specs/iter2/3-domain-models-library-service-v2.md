# Domain Models Specification - Library Service

Evolution from `specs/iter1/3-domain-models.md`:
- `Library` retained as a plain entity with minimal change.
- `BookInventory` evolved into the aggregate of `InventoryItem`.
- `BorrowRecord` evolved into the aggregate of `Loan`.
- `AvailabilityLog` removed from domain model (as it is a reading projection).
---

## 1. Domain Classes and Related User Stories

### Domain Classes

| Domain Class               | Type | Description                                                     |
|:---------------------------| :--- |:----------------------------------------------------------------|
| `Library`                  | Entity | Library branch with name and location                           |       
| `Customer`                 | Entity| Customer with id and contact                                    |           
| `LibraryId`                | Value Object | Typed, validated library branch identifier                      | 
| `Isbn`                     | Value Object | Typed book identifier                                           | 
| `Contact`                  | Value Object | Customer contact details (email, phone)                        |
| `CustomerId`               | Value Object | Typed customer identifier                                       | 
| `CopyCount`                | Value Object | Non-negative validated copy count                               | 
| `LoanPeriod`               | Value Object | Encapsulates borrowedAt, dueDate, renewalCount                  | 
| `Money`                    | Value Object | Amount + currency for fine calculations                         | 
| `PolicySnapshot`           | Value Object | Loan/renew/fine rules captured at checkout time                 | 
| `InventoryItem`            | Aggregate Root | Stock record per branch+ISBN; enforces copy-count invariants    |
| `Loan`                     | Aggregate Root | Full lifecycle of a borrowing transaction                       | 
| `FineLedger`               | Aggregate Root | Assessed and settled financial obligations for a loan           | 
| `BookBorrowed`             | Domain Event | Emitted by `LoanAggregate` on successful checkout                |
| `BookReturned`             | Domain Event | Emitted by `LoanAggregate` on successful return                  |
| `LoanRenewed`              | Domain Event | Emitted by `LoanAggregate` on successful renewal                 |
| `LoanOverdueFlagged`       | Domain Event | Emitted by `LoanAggregate` when a loan's due date is crossed     |
| `CopyReportedLost`         | Domain Event | Emitted by `LoanAggregate` when a borrowed copy is reported lost |
| `CopyReportedDamaged`      | Domain Event | Emitted by `LoanAggregate` when a copy is reported damaged       |
| `InventoryInitialized`     | Domain Event | Emitted by `InventoryAggregate` on first-time stock creation     |
| `InventoryCopyReserved`    | Domain Event | Emitted by `InventoryAggregate` when a copy is reserved for loan |
| `InventoryCopyReleased`    | Domain Event | Emitted by `InventoryAggregate` when a returned copy is released |
| `InventoryAdjusted`        | Domain Event | Emitted by `InventoryAggregate` on manual stock adjustment       |
| `InventoryTransferredOut`  | Domain Event | Emitted by `InventoryAggregate` when copies leave a branch       |
| `InventoryTransferredIn`   | Domain Event | Emitted by `InventoryAggregate` when copies arrive at a branch   |
| `FinePaid`                 | Domain Event | Emitted by `FineAggregate` when a fine payment is recorded       |
| `CirculationPolicyService` | Domain Service | Due date calculation, renewal eligibility, loan cap enforcement | 
| `FinePolicyService`        | Domain Service | Overdue / lost / damaged charge calculations                    | 

### User Story Mapping

| User Story                                 | Primary Relevant Domain Classes | Triggering Domain Events |
|:-------------------------------------------| :--- | :--- |
| **L1. Find libraries by book ISBN**        | `InventoryItem`, `Library` | None |
| **L2. Check book availability**            | `InventoryItem`, `Library` | None |
| **L3. Borrow a book**                      | `Loan`, `InventoryItem`, `CirculationPolicyService` | `BookBorrowed`, `InventoryCopyReserved` |
| **L4. Return a book**                      | `Loan`, `InventoryItem` | `BookReturned`, `InventoryCopyReleased` |
| **L5. Renew a loan**                       | `Loan`, `CirculationPolicyService` | `LoanRenewed` |
| **L6. List active loans for a customer**   | `Loan` | None |
| **L7. List overdue loans for a library**   | `Loan` | None (Query; `LoanOverdueFlagged` is emitted by a background scheduler via `markOverdue()`, not by this query) |
| **L8. Report lost or damaged copy**        | `Loan`, `InventoryItem`, `FinePolicyService` | `CopyReportedLost` / `CopyReportedDamaged` |
| **L9. Adjust inventory stock**             | `InventoryItem` | `InventoryAdjusted` |
| **L10. Transfer copies between libraries** | `InventoryItem` | `InventoryTransferredOut`, `InventoryTransferredIn` |
| **L11. Pay overdue fine**                  | `FineLedger` | `FinePaid` |


---

## 2. DDD Building Blocks

### 2.1 Entities

#### 2.1.1 `Library`

| Field | Type | Description | Constraints |
| :--- | :--- | :--- | :--- |
| `id` | `LibraryId` | Unique branch identifier | Required |
| `name` | `String` | Name of the library | Required |
| `location` | `String` | Physical address | Required |

#### 2.1.2 `Customer`

| Field | Type | Description | Constraints |
| :--- | :--- | :--- | :--- |
| `id` | `CustomerId` | Unique customer identifier | Required |
| `name` | `String` | Full name of the customer | Required |
| `contact` | `Contact` | Email and phone contact details | Required |

---

### 2.2 Value Objects

#### 2.2.1 `LibraryId`

| Field | Type | Description |
| :--- | :--- | :--- |
| `value` | `String` | Underlying identifier (e.g., `"LIB-001"`) |

#### 2.2.2 `Isbn`

| Field | Type | Description |
| :--- | :--- | :--- |
| `value` | `String` | 13-digit ISBN string |

#### 2.2.3 `CustomerId`

| Field | Type | Description |
| :--- | :--- | :--- |
| `value` | `String` | Underlying customer identifier |

#### 2.2.4 `Contact`

| Field | Type | Description |
| :--- | :--- | :--- |
| `email` | `String` | Customer email address |
| `phone` | `String` | Customer phone number |

#### 2.2.5 `CopyCount`

| Field | Type | Description | Constraints |
| :--- | :--- | :--- | :--- |
| `value` | `int` | Number of copies | `value >= 0` |

#### 2.2.6 `LoanPeriod`

| Field | Type | Description |
| :--- | :--- | :--- |
| `borrowedAt` | `LocalDateTime` | Date/time the book was checked out |
| `dueDate` | `LocalDateTime` | Date/time the book is due for return |
| `renewalCount` | `int` | Number of times the loan has been renewed |

#### 2.2.7 `Money`

| Field | Type | Description |
| :--- | :--- | :--- |
| `amount` | `BigDecimal` | Monetary amount (non-negative) |
| `currency` | `String` | ISO 4217 currency code (e.g., `"AUD"`) |

#### 2.2.8 `PolicySnapshot`

| Field | Type | Description |
| :--- | :--- | :--- |
| `loanPeriodDays` | `int` | Default loan duration in days |
| `maxRenewals` | `int` | Maximum number of renewals allowed |
| `dailyOverdueFine` | `Money` | Fine charged per overdue day |
| `lostCopyCharge` | `Money` | Replacement charge for a lost copy |
| `damagedCopyCharge` | `Money` | Penalty charge for a damaged copy |

---

### 2.3 Aggregates

#### 2.3.1 `InventoryAggregate` (root: `InventoryItem`)

| Field | Type | Description | Constraints |
| :--- | :--- | :--- | :--- |
| `inventoryId` | `String` | Composite key `{libraryId}:{isbn}` | Primary identifier |
| `libraryId` | `LibraryId` | Owning branch | Required |
| `isbn` | `Isbn` | Book identity (from Book Service) | Required |
| `totalCopies` | `CopyCount` | Total physical copies held | `>= 0` |
| `availableCopies` | `CopyCount` | Copies available to borrow | `0 <= available <= total` |
| `version` | `long` | Optimistic concurrency version | Auto-incremented |

**Invariants**
1. `0 <= availableCopies <= totalCopies`
2. A borrow decreases `availableCopies` by exactly 1.
3. A return increases `availableCopies` by exactly 1, unless the copy is lost or damaged.

**Behaviors**
- `initialize(totalCopies)` — emits `InventoryInitialized` (first-time creation of the aggregate stream)
- `reserveCopyForLoan()` — emits `InventoryCopyReserved`
- `releaseCopyFromReturn()` — emits `InventoryCopyReleased`
- `adjustStock(reason, delta)` — emits `InventoryAdjusted`
- `transferOut(quantity, transferId)` — emits `InventoryTransferredOut`
- `transferIn(quantity, transferId)` — emits `InventoryTransferredIn`

#### 2.3.2 `LoanAggregate` (root: `Loan`)

| Field | Type | Description | Constraints |
| :--- | :--- | :--- | :--- |
| `loanId` | `String` | Business identity (e.g., `"REC-xxxx"`) | Primary identifier |
| `libraryId` | `LibraryId` | Branch where book was borrowed | Required |
| `isbn` | `Isbn` | Borrowed title | Required |
| `customerId` | `CustomerId` | Borrower | Required |
| `loanPeriod` | `LoanPeriod` | borrowedAt, dueDate, renewalCount | Required |
| `returnedAt` | `LocalDateTime?` | Actual return date/time | Null until returned |
| `status` | `LoanStatus` | `BORROWED`, `RETURNED`, `OVERDUE`, `LOST`, `DAMAGED` | Default: `BORROWED` |
| `policySnapshot` | `PolicySnapshot` | Policy rules captured at checkout | Required |

**Invariants**
1. Only `BORROWED` or `OVERDUE` loans may be renewed.
2. A returned loan cannot be renewed or returned again.
3. `LOST` and `DAMAGED` are terminal statuses.

**Behaviors**
- `borrow(isbn, customerId, policySnapshot, at)` — emits `BookBorrowed`
- `markReturned(at)` — emits `BookReturned`
- `renew(now, policyDecision)` — emits `LoanRenewed`
- `markOverdue(now)` — emits `LoanOverdueFlagged`
- `reportLost(now)` — emits `CopyReportedLost`
- `reportDamaged(now)` — emits `CopyReportedDamaged`

#### 2.3.3 `FineAggregate` (root: `FineLedger`)

| Field | Type | Description | Constraints |
| :--- | :--- | :--- | :--- |
| `fineId` | `String` | Fine identifier | Primary identifier |
| `loanId` | `String` | Associated loan | Required |
| `customerId` | `CustomerId` | Debtor | Required |
| `assessed` | `Money` | Total amount charged | `>= 0` |
| `paid` | `Money` | Total amount paid | `>= 0` |
| `status` | `FineStatus` | `OPEN`, `SETTLED` | Default: `OPEN` |

**Invariant**
1. `balance = assessed − paid` and `balance >= 0`.

**Behaviors**
- `assess(amount, reason, at)`
- `recordPayment(amount, at)` — emits `FinePaid`

---

### 2.4 Domain Services

#### 2.4.1 `CirculationPolicyService`

| Method | Parameters | Returns | Description |
| :--- | :--- | :--- | :--- |
| `calculateDueDate` | `borrowedAt: LocalDateTime, snapshot: PolicySnapshot` | `LocalDateTime` | Computes due date from borrow time and policy |
| `isRenewalAllowed` | `loan: Loan, now: LocalDateTime` | `boolean` | Checks renewal count and overdue status against policy |
| `captureSnapshot` | `libraryId: LibraryId` | `PolicySnapshot` | Reads current policy rules and freezes them at checkout |

#### 2.4.2 `FinePolicyService`

| Method | Parameters | Returns | Description |
| :--- | :--- | :--- | :--- |
| `calculateOverdueFine` | `loan: Loan, now: LocalDateTime` | `Money` | Daily fine × overdue days from `policySnapshot` |
| `calculateLostCharge` | `snapshot: PolicySnapshot` | `Money` | Replacement charge from `policySnapshot` |
| `calculateDamagedCharge` | `snapshot: PolicySnapshot` | `Money` | Damage penalty from `policySnapshot` |

---

### 2.5 Domain Events

#### 2.5.1 `BookBorrowed`

| Field | Type | Description |
| :--- | :--- | :--- |
| `loanId` | `String` | The loan created |
| `libraryId` | `LibraryId` | Branch where checkout occurred |
| `isbn` | `Isbn` | Borrowed title |
| `customerId` | `CustomerId` | Borrower |
| `occurredAt` | `LocalDateTime` | Event timestamp |

#### 2.5.2 `BookReturned`

| Field | Type | Description |
| :--- | :--- | :--- |
| `loanId` | `String` | The loan closed |
| `libraryId` | `LibraryId` | Branch where return occurred |
| `isbn` | `Isbn` | Returned title |
| `customerId` | `CustomerId` | Borrower |
| `occurredAt` | `LocalDateTime` | Event timestamp |

#### 2.5.3 `LoanOverdueFlagged`

| Field | Type | Description |
| :--- | :--- | :--- |
| `loanId` | `String` | The overdue loan |
| `customerId` | `CustomerId` | Borrower |
| `libraryId` | `LibraryId` | Branch of the loan |
| `isbn` | `Isbn` | Overdue title |
| `dueDate` | `LocalDateTime` | Original due date |
| `occurredAt` | `LocalDateTime` | Event timestamp |

#### 2.5.4 `CopyReportedLost`

| Field | Type | Description |
| :--- | :--- | :--- |
| `loanId` | `String` | The affected loan |
| `libraryId` | `LibraryId` | Branch of the loan |
| `isbn` | `Isbn` | Lost title |
| `customerId` | `CustomerId` | Responsible borrower |
| `occurredAt` | `LocalDateTime` | Event timestamp |

#### 2.5.5 `CopyReportedDamaged`

| Field | Type | Description |
| :--- | :--- | :--- |
| `loanId` | `String` | The affected loan |
| `libraryId` | `LibraryId` | Branch of the loan |
| `isbn` | `Isbn` | Damaged title |
| `customerId` | `CustomerId` | Responsible borrower |
| `occurredAt` | `LocalDateTime` | Event timestamp |

#### 2.5.6 `InventoryAdjusted`

| Field | Type | Description |
| :--- | :--- | :--- |
| `libraryId` | `LibraryId` | Branch where adjustment occurred |
| `isbn` | `Isbn` | Adjusted title |
| `delta` | `int` | Change in total copies (positive = added, negative = removed) |
| `reasonCode` | `String` | Reason for adjustment (e.g., `"ACQUISITION"`, `"DISCARD"`, `"AUDIT"`) |
| `operatorId` | `String` | Librarian who performed the adjustment |
| `occurredAt` | `LocalDateTime` | Event timestamp |

#### 2.5.7 `InventoryTransferredOut`

| Field | Type | Description |
| :--- | :--- | :--- |
| `transferId` | `String` | Shared identity linking OUT and IN events |
| `sourceLibraryId` | `LibraryId` | Branch copies are leaving |
| `targetLibraryId` | `LibraryId` | Branch copies are going to |
| `isbn` | `Isbn` | Transferred title |
| `quantity` | `int` | Number of copies transferred |
| `occurredAt` | `LocalDateTime` | Event timestamp |

#### 2.5.8 `InventoryTransferredIn`

| Field | Type | Description |
| :--- | :--- | :--- |
| `transferId` | `String` | Shared identity linking OUT and IN events |
| `sourceLibraryId` | `LibraryId` | Branch copies came from |
| `targetLibraryId` | `LibraryId` | Branch copies are arriving at |
| `isbn` | `Isbn` | Transferred title |
| `quantity` | `int` | Number of copies transferred |
| `occurredAt` | `LocalDateTime` | Event timestamp |

#### 2.5.9 `LoanRenewed`

| Field | Type | Description |
| :--- | :--- | :--- |
| `loanId` | `String` | The renewed loan |
| `customerId` | `CustomerId` | Borrower |
| `newDueDate` | `LocalDateTime` | Extended due date after renewal |
| `renewalCount` | `int` | Updated renewal count |
| `occurredAt` | `LocalDateTime` | Event timestamp |

#### 2.5.10 `InventoryInitialized`

| Field | Type | Description |
| :--- | :--- | :--- |
| `libraryId` | `LibraryId` | Branch where inventory was created |
| `isbn` | `Isbn` | Title being stocked |
| `totalCopies` | `int` | Initial total copies |
| `availableCopies` | `int` | Initial available copies |
| `occurredAt` | `LocalDateTime` | Event timestamp |

#### 2.5.11 `InventoryCopyReserved`

| Field | Type | Description |
| :--- | :--- | :--- |
| `libraryId` | `LibraryId` | Branch where reservation occurred |
| `isbn` | `Isbn` | Reserved title |
| `loanId` | `String` | The loan that triggered the reservation |
| `occurredAt` | `LocalDateTime` | Event timestamp |

#### 2.5.12 `InventoryCopyReleased`

| Field | Type | Description |
| :--- | :--- | :--- |
| `libraryId` | `LibraryId` | Branch where release occurred |
| `isbn` | `Isbn` | Released title |
| `loanId` | `String` | The loan that was closed |
| `occurredAt` | `LocalDateTime` | Event timestamp |

#### 2.5.13 `FinePaid`

| Field | Type | Description |
| :--- | :--- | :--- |
| `fineId` | `String` | The fine ledger entry |
| `customerId` | `CustomerId` | Payer |
| `amount` | `Money` | Amount paid in this transaction |
| `occurredAt` | `LocalDateTime` | Event timestamp |
