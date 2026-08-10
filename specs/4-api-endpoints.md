# API Endpoints Summary

## 1. Book Service (`http://localhost:8081/api/v1`)

| Feature | Method | Endpoint Path | Request Body | Response (Success) | Description |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **B1** | `POST` | `/books` | `BookCreateRequest` | `201 Created` (`BookResponse`) | Create a new book record in master catalog |
| **B2** | `GET` | `/books` | *None* | `200 OK` (`Array<BookResponse>`) | List all books in catalog |
| **B1** | `GET` | `/books/{isbn}` | *None* | `200 OK` (`BookResponse`) | Get book details by ISBN |
| **B1** | `PUT` | `/books/{isbn}` | `BookUpdateRequest` | `200 OK` (`BookResponse`) | Update book details by ISBN |
| **B1** | `DELETE` | `/books/{isbn}` | *None* | `204 No Content` | Delete a book record by ISBN |

---

## 2. Library Service (`http://localhost:8082/api/v1`)

| Feature | Method | Endpoint Path | Request Body | Response (Success) | Inter-Service REST Dependencies |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **L1** | `GET` | `/libraries/by-isbn/{isbn}` | *None* | `200 OK` (`Array<LibraryAvailabilityResponse>`) | None |
| **L2** | `GET` | `/libraries/{libraryId}/books/{isbn}/availability` | *None* | `200 OK` (`BookAvailabilityResponse`) | None |
| **L3** | `POST` | `/libraries/{libraryId}/books/{isbn}/borrow` | `BorrowBookRequest` | `201 Created` (`BorrowRecordResponse`) | **Book Service**: `GET /books/{isbn}` |
| **L4** | `POST` | `/libraries/{libraryId}/books/{isbn}/return` | `ReturnBookRequest` | `200 OK` (`BorrowRecordResponse`) | None |
