# Domain Models Specification

## 1. Domain Classes and Related User Stories

| Service | User Story | Domain Classes |
| :--- | :--- | :--- |
| **Book Service** | **B1. Create/Get/Update/Delete Book** | `Book` |
| **Book Service** | **B2. List All Books** | `Book` |
| **Library Service** | **L1. Find Libraries by ISBN** | `Library`, `BookInventory` |
| **Library Service** | **L2. Check Book Availability** | `BookInventory`, `AvailabilityLog` |
| **Library Service** | **L3. Borrow a Book** | `BookInventory`, `BorrowRecord`, `AvailabilityLog` |
| **Library Service** | **L4. Return a Book** | `BookInventory`, `BorrowRecord`, `AvailabilityLog` |
---

## 2. Book Service Domain Model

The Book Service manages the central master catalog of books.

### 2.1 `Book`
Represents a book entry in the catalog.

| Field Name | Type | Description | Constraints / Notes |
| :--- | :--- | :--- | :--- |
| `isbn` | `String` | Unique ISBN identifier for the book | Primary Identifier |
| `title` | `String` | Title of the book | Required |
| `author` | `String` | Author of the book | Required |

---

## 3. Library Service Domain Model

The Library Service manages library branches, physical inventory, borrowing/return transactions, and availability change tracking.

### 3.1 `Library`
Represents a physical or logical library branch.

| Field Name | Type | Description | Constraints / Notes |
| :--- | :--- | :--- | :--- |
| `id` | `String` / `Long` | Unique library identifier | Primary Identifier |
| `name` | `String` | Name of the library | Required |
| `location` | `String` | Physical address or location | Required |

---

### 3.2 `BookInventory` 
Tracks the stock level and availability of a specific book (by ISBN) within a specific library.

| Field Name | Type | Description | Constraints / Notes |
| :--- | :--- | :--- | :--- |
| `id` | `String` / `Long` | Unique inventory record identifier | Primary Identifier |
| `libraryId` | `String` / `Long` | Reference to the `Library` | Required |
| `isbn` | `String` | Reference to the `Book` ISBN | Required |
| `totalCopies` | `int` | Total physical copies owned | Non-negative |
| `availableCopies` | `int` | Copies currently available to borrow | `0 <= availableCopies <= totalCopies` |

---

### 3.3 `BorrowRecord` 
Represents a borrowing transaction when a customer borrows a book from a library and returns it.

| Field Name | Type | Description | Constraints / Notes |
| :--- | :--- | :--- | :--- |
| `id` | `String` / `Long` | Unique borrowing record identifier | Primary Identifier |
| `libraryId` | `String` / `Long` | Library where book was borrowed | Required |
| `isbn` | `String` | ISBN of the borrowed book | Required |
| `customerId` | `String` | ID/Username of the borrowing customer | Required |
| `borrowedAt` | `LocalDateTime` | Date and time when borrowed | Set on creation |
| `dueDate` | `LocalDateTime` | Due date for return | Business calculation |
| `returnedAt` | `LocalDateTime` | Date and time when returned | Null until returned |
| `status` | `BorrowStatus` (Enum) | Status (`BORROWED`, `RETURNED`, `OVERDUE`) | Default: `BORROWED` |

---

### 3.4 `AvailabilityLog` 
Tracks historical changes in book availability within a library (e.g., when a book is borrowed, returned, or stock is updated).

| Field Name | Type | Description | Constraints / Notes |
| :--- | :--- | :--- | :--- |
| `id` | `String` / `Long` | Unique log entry identifier | Primary Identifier |
| `libraryId` | `String` / `Long` | Library where change occurred | Required |
| `isbn` | `String` | ISBN of the book | Required |
| `previousAvailableCopies` | `int` | Count before change | Non-negative |
| `newAvailableCopies` | `int` | Count after change | Non-negative |
| `changeReason` | `ChangeReason` (Enum) | Reason (`BORROW`, `RETURN`, `STOCK_ADJUSTMENT`) | Required |
| `timestamp` | `LocalDateTime` | Date and time of the change | Auto-generated timestamp |


