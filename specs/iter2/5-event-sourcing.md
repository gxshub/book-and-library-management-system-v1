# Event Sourcing Specification

## 1. Scope
- This specification defines event sourcing for **Library Service**, where only `InventoryAggregate` is event-sourced.
- Other aggregates (`LoanAggregate`, `HoldQueueAggregate`, `FineAggregate`) remain CRUD-based.

---

## 2. Goals
- Preserve inventory state transitions as immutable events.
- Support inventory availability queries via a projected read model.
- Provide a replayable inventory history for auditing and analysis.

---

## 3. Event Model

### 3.1 Event Fields

| Field | Type | Description |
| :--- | :--- | :--- |
| `eventId` | UUID | Unique event identity. Used for idempotency deduplication. |
| `aggregateId` | String | Identity of the aggregate instance (e.g., `"LIB-001:978-0134685991"`) |
| `aggregateType` | String | Always `"InventoryAggregate"` |
| `eventType` | String | One of the event types listed in §3.2 |
| `aggregateVersion` | long | Monotonically increasing version of the aggregate stream after this event |
| `occurredAt` | timestamp | Wall-clock time the event was produced |
| `payload` | JSON | Event-specific data (state change fields) |

### 3.2 Event Stream Identity
- Stream key = `aggregateType` + `aggregateId`

### 3.3 Event Types for `InventoryAggregate`

| Event Type | Triggering Behavior | Key Payload Fields |
| :--- | :--- | :--- |
| `InventoryInitialized` | First-time creation of inventory record | `libraryId`, `isbn`, `totalCopies`, `availableCopies` |
| `InventoryCopyReserved` | `reserveCopyForLoan()` | `libraryId`, `isbn`, `loanId` |
| `InventoryCopyReleased` | `releaseCopyFromReturn()` | `libraryId`, `isbn`, `loanId` |
| `InventoryAdjusted` | `adjustStock(reason, delta)` | `libraryId`, `isbn`, `delta`, `reasonCode`, `operatorId` |
| `InventoryTransferredOut` | `transferOut(quantity, transferId)` | `libraryId`, `isbn`, `quantity`, `transferId` |
| `InventoryTransferredIn` | `transferIn(quantity, transferId)` | `libraryId`, `isbn`, `quantity`, `transferId` |

---

