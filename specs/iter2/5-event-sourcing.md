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
<!--
## 4. Concurrency and Optimistic Locking

1. Every aggregate instance tracks a `version` (long, starts at 0, increments by 1 per event appended).
2. Every command carries an `expectedVersion` (the version the caller last read).
3. Before appending a new event, the event store checks: `currentVersion == expectedVersion`.
   - If **match**: append succeeds; `version` increments.
   - If **mismatch**: reject with `InventoryConflict` (HTTP 409). The caller must reload and retry.
4. This optimistic locking rule applies to all `InventoryAggregate` commands (L3, L4, L10, L11, L12).

---

## 5. Idempotency

1. Every command request carries a client-generated `commandId` (UUID).
2. Before executing, the command handler checks an **idempotency log** keyed by `commandId`.
   - If `commandId` already processed: return the stored result immediately without re-applying.
   - If not found: process normally, then record `commandId` + result in the idempotency log.
3. This ensures safe retries (e.g., after network timeouts or job scheduler re-runs) without duplicate state changes.

---

## 6. Projection and Read Model Update

1. `InventoryAggregate` events drive an `inventory_read_model` table used by L1/L2 queries.
2. **Phase 1 (current implementation)**: projection update is synchronous, in the same local DB transaction as the event store append.
   - If either the event append or projection update fails, the full transaction rolls back.
   - No partial state is possible.
3. The projector consumes the following events to maintain `inventory_read_model`:

| Event | Read Model Effect |
| :--- | :--- |
| `InventoryInitialized` | Insert row: `libraryId`, `isbn`, `totalCopies`, `availableCopies` |
| `InventoryCopyReserved` | Decrement `availableCopies` by 1 |
| `InventoryCopyReleased` | Increment `availableCopies` by 1 |
| `InventoryAdjusted` | Apply `delta` to `totalCopies` and `availableCopies` per policy |
| `InventoryTransferredOut` | Decrement `totalCopies` and `availableCopies` by `quantity` |
| `InventoryTransferredIn` | Increment `totalCopies` and `availableCopies` by `quantity` |

4. Projectors must be **idempotent**: re-processing the same event must produce the same read model state. Use `eventId` to skip already-applied events during rebuild.

---

## 7. Replay and Snapshot Policy

1. Aggregate state is reconstructed by replaying all events in the stream from the beginning.
2. **Snapshot threshold**: a snapshot is taken every **50 events** on a stream.
   - A snapshot captures the full aggregate state at that version.
   - Snapshots are stored in a separate table alongside the event log (not replacing it).
3. **On aggregate load**:
   - Load the latest snapshot (if any) to get a baseline state and version.
   - Replay only events with `aggregateVersion > snapshotVersion`.
4. Snapshots are advisory — the system must remain correct when falling back to full replay.

---

## 8. Failure and Retry Handling

1. **Transaction boundary (Phase 1)**: event append + projection update are in a single local DB transaction.
   - On failure: both roll back. The command returns an error. The caller may retry using the same `commandId` (idempotent).
2. **Projection rebuild**: if the `inventory_read_model` becomes inconsistent (e.g., data corruption or migration), it can be fully rebuilt by:
   1. Truncating the read model table.
   2. Replaying all events from all inventory streams in `occurredAt` order.
   3. Projector idempotency ensures safe re-application.
3. **Optimistic lock retry guidance**: callers that receive `InventoryConflict` should reload the aggregate, re-validate business intent, and resubmit with the updated `expectedVersion`.

-->

