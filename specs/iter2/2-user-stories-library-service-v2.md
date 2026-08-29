# User Stories - LIBRARY SERVICE

## User Story Overview 

| ID | User Story Name | Primary Role(s) | Goal |
| :--- | :--- | :--- | :--- |
| L1 | Find libraries by book ISBN | Customer, Librarian | Discover branches where a title is available |
| L2 | Check book availability | Customer, Librarian | View available copies and borrowability |
| L3 | Borrow a book | Customer, Librarian | Check out a copy from a branch |
| L4 | Return a book | Customer, Librarian | Return a borrowed copy and update stock |
| L5 | Place a hold (reservation) | Customer | Reserve a book when unavailable |
| L6 | Cancel a hold | Customer | Remove an active reservation |
| L7 | Renew a loan | Customer, Librarian | Extend due date if policy allows |
| L8 | List active loans for a customer | Customer, Librarian | View all current borrowed items |
| L9 | List overdue loans for a library | Librarian | Track overdue items for operations |
| L10 | Report lost or damaged copy | Customer, Librarian | Mark an active loan as exception and update inventory |
| L11 | Adjust inventory stock | Librarian | Add/remove copies due to acquisition, discard, audit |
| L12 | Transfer copies between libraries | Librarian | Move stock across branches |
| L13 | Pay or waive overdue fine | Customer, Librarian | Resolve financial obligations for overdue/lost items |


## User Story Specification

### L1: Find Libraries by Book ISBN
- **As a** Customer / Librarian, **I want to** find libraries in which a book with a specific ISBN is available.

### L2: Check Book Availability
- **As a** Customer / Librarian, **I want to** check the availability for a specific book in a library.

### L3: Borrow a Book
- **As a** Customer / Librarian, **I want to** borrow a book from a specific library.
- **Scenario**:
  1. The user requests to borrow a book by providing an ISBN and a library ID.
  2. **Inter-Service Validation**: Library Service sends a REST request to Book Service to verify that the book exists (Get a Book).
  3. If valid, Library Service checks local inventory availability and processes the borrowing request.

### L4: Return a Book
- **As a** Customer / Librarian, **I want to** return a borrowed book to the same library.

### L5: Place a Hold (Reservation)
- **As a** Customer, **I want to** place a hold for a book at a chosen library when no copy is currently available, **so that** I can receive the next available copy.
- **Acceptance rules**:
  1. Book existence is validated via Book Service.
  2. A customer cannot place duplicate active holds for the same ISBN in the same library.
  3. Hold position is deterministic (FIFO by placed time).

### L6: Cancel a Hold
- **As a** Customer, **I want to** cancel my active hold, **so that** I no longer occupy queue position.
- **Acceptance rules**:
  1. Only active holds are cancellable.
  2. Queue order is recomputed after cancellation.

### L7: Renew a Loan
- **As a** Customer / Librarian, **I want to** renew an active loan, **so that** I can keep the book longer when no waiting hold exists.
- **Acceptance rules**:
  1. Only non-returned loans can be renewed.
  2. Renewal is denied if active hold queue exists for the same ISBN and library.
  3. Renewal count cannot exceed policy maximum.

### L8: List Active Loans for a Customer
- **As a** Customer / Librarian, **I want to** list active loans by customer, **so that** I can track what is currently borrowed.
- **Acceptance rules**:
  1. Result includes due dates and overdue indicators.
  2. Result excludes returned/cancelled loans.

### L9: List Overdue Loans for a Library
- **As a** Librarian, **I want to** view overdue loans for a branch, **so that** I can run reminder and recovery processes.
- **Acceptance rules**:
  1. Overdue determination is based on domain time and loan policy.
  2. Result can be filtered by overdue duration bucket (e.g., >7 days).

### L10: Report Lost or Damaged Copy
- **As a** Customer / Librarian, **I want to** report a borrowed copy as lost or damaged, **so that** circulation status and penalties are handled correctly.
- **Acceptance rules**:
  1. Loan status transitions from BORROWED to LOST or DAMAGED.
  2. Inventory availability is adjusted according to policy.
  3. Fine or replacement charge is generated.

### L11: Adjust Inventory Stock
- **As a** Librarian, **I want to** adjust stock quantities, **so that** inventory reflects acquisitions, audits, and discards.
- **Acceptance rules**:
  1. Adjustment requires reason code and operator metadata.
  2. Available copies must remain within valid boundaries.
  3. Every adjustment creates an auditable stock-change record.

### L12: Transfer Copies Between Libraries
- **As a** Librarian, **I want to** transfer copies from one branch to another, **so that** demand can be balanced.
- **Acceptance rules**:
  1. Source branch must have sufficient transferable copies.
  2. Transfer is recorded as two inventory movements (OUT/IN) under one transaction identity.
  3. Availability consistency is preserved across both branches.

### L13: Pay or Waive Overdue Fine
- **As a** Customer / Librarian, **I want to** settle or waive fines, **so that** account standing is updated.
- **Acceptance rules**:
  1. Fine ledger tracks assessed amount, paid amount, waived amount, and balance.
  2. Waive action requires librarian role and reason.

---

## Key Policy Assumptions
1. Default loan period: 14 days.
2. Renewal limit: 2 times per loan unless overridden by policy profile.
3. Hold queue discipline: FIFO.
4. Overdue fine policy and lost/damaged charges are configurable per library.
