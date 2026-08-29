# API Endpoints Summary 

## 1. Book Service (`http://localhost:8081/api/v1`)

Book Service APIs remain unchanged from v1.

| Feature | Method | Endpoint Path | Request Body | Response (Success) | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **B1** | `POST` | `/books` | `BookCreateRequest` | `201 Created` (`BookResponse`) | Create a new book record in master catalog |
| **B2** | `GET` | `/books` | *None* | `200 OK` (`Array<BookResponse>`) | List all books in catalog |
| **B1** | `GET` | `/books/{isbn}` | *None* | `200 OK` (`BookResponse`) | Get book details by ISBN |
| **B1** | `PUT` | `/books/{isbn}` | `BookUpdateRequest` | `200 OK` (`BookResponse`) | Update book details by ISBN |
| **B1** | `DELETE` | `/books/{isbn}` | *None* | `204 No Content` | Delete a book record by ISBN |

---

## 2. Library Service (`http://localhost:8082/api/v1`)

| Feature | CQRS Type | Method | Endpoint Path | Request Body | Response (Success) | Inter-Service REST Dependencies |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **L1** | Query | `GET` | `/libraries/by-isbn/{isbn}` | *None* | `200 OK` (`Array<LibraryAvailabilityResponse>`) | None |
| **L2** | Query | `GET` | `/libraries/{libraryId}/books/{isbn}/availability` | *None* | `200 OK` (`BookAvailabilityResponse`) | None |
| **L3** | Command | `POST` | `/libraries/{libraryId}/books/{isbn}/borrow` | `BorrowBookRequest` | `201 Created` (`LoanResponse`) | **Book Service**: `GET /books/{isbn}` |
| **L4** | Command | `POST` | `/libraries/{libraryId}/books/{isbn}/loans/{loanId}/return` | `ReturnBookRequest` | `200 OK` (`LoanResponse`) | None |
| **L5** | Command | `POST` | `/libraries/{libraryId}/books/{isbn}/loans/{loanId}/renew` | `RenewLoanRequest` | `200 OK` (`LoanResponse`) | None |
| **L6** | Query | `GET` | `/customers/{customerId}/loans?status=ACTIVE` | *None* | `200 OK` (`Array<LoanResponse>`) | None |
| **L7** | Query | `GET` | `/libraries/{libraryId}/loans/overdue` | *None* | `200 OK` (`Array<LoanResponse>`) | None |
| **L8** | Command | `POST` | `/libraries/{libraryId}/books/{isbn}/loans/{loanId}/report-loss-or-damage` | `LossDamageReportRequest` | `200 OK` (`LoanResponse`) | None |
| **L9** | Command | `POST` | `/libraries/{libraryId}/books/{isbn}/inventory/adjust` | `InventoryAdjustmentRequest` | `200 OK` (`InventoryAdjustmentResponse`) | None |
| **L10** | Command | `POST` | `/inventory/transfers` | `InventoryTransferRequest` | `202 Accepted` (`InventoryTransferResponse`) | None |
| **L11** | Command | `POST` | `/fines/{fineId}/payments` | `FinePaymentRequest` | `200 OK` (`FineResponse`) | None |
## 3. Endpoint Naming and Contract Rules

1. Command endpoints use verbs as trailing actions only when state transition intent is explicit (e.g., `/renew`, `/adjust`).
2. Query endpoints stay resource-oriented and side-effect free.
3. Command responses return mutation outcome objects; query responses return read-model DTOs.
4. For InventoryAggregate commands (L9, L10, and borrow/return inventory mutations), persistence follows event sourcing phase-1 design in `specs/iter2/5-event-sourcing.md`.

---

## 4. Error Response Contract (Summary)

| Error Code | HTTP Status | Applies To | Meaning |
| :--- | :--- | :--- | :--- |
| `ResourceNotFound` | `404` | Commands + Queries | Requested book/library/loan/fine not found |
| `ValidationFailed` | `400` | Commands + Queries | Invalid request shape or parameters |
| `InventoryInvariantViolation` | `409` | L3, L4, L8, L9, L10 | Inventory rule violation (e.g., availability below zero) |
| `InventoryConflict` | `409` | L9, L10 (+ inventory mutations in L3/L4) | Concurrent update/version conflict |
| `LoanPolicyViolation` | `409` | L3, L5, L8 | Borrow/renew/loss-damage transition denied by policy |
| `FineStateViolation` | `409` | L11 | Payment exceeds outstanding balance |
