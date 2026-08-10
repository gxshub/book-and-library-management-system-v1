# User Stories & Specifications

##  User Story Overview

| Service | User Story ID | User Story Name                 | Role(s)                  |
| :--- |:--------------|:--------------------------------|:-------------------------|
| **Book Service** | B1            | Create/get/update/delete a Book | Customer, Librarian      |
| **Book Service** | B2            | List All Books                  | Customer, Librarian      |
| **Library Service** | L1            | Find Libraries by Book ISBN     | Customer, Librarian      |
| **Library Service** | L2            | Check Book Availability         | Customer, Librarian      |
| **Library Service** | L3            | Borrow a Book                   | Customer, Librarian      |
| **Library Service** | L4            | Return a Book                   | Customer, Librarian      |

## Book Service

### User Story B1. Create/get/update/delete a Book
- **As a** Librarian, **I want to** create a new book record with details such as title, author, and ID (ISBN)
- **As a** Customer / Librarian, **I want to** get a book's information by its ID
- **As a** Librarian, **I want to** update an existing book's information by its ID
- **As a** Librarian, **I want to** delete a book record by its ID.
---

### User Story B2: List All Books
- **As a** Customer / Librarian, **I want to** retrieve a list of all books.

## Library Service

### User Story L1: Find Libraries by Book ISBN
- **As a** Customer / Librarian, **I want to** find libraries in which a book with a specific ISBN is available.

### User Story L2: Check Book Availability 
- **As a** Customer / Librarian, **I want to** check the availability for a specific book in a library.

### User Story L3: Borrow a Book
- **As a** Customer / Librarian, **I want to** borrow a book from a specific library.
- **Scenario**:
  1. The user requests to borrow a book by providing an ISBN and a library ID.
  2. **Inter-Service Validation**: Library Service sends a REST request to Book Service to verify that the book exists (Get a Book).
  3. If valid, Library Service checks local inventory availability and processes the borrowing request.

### User Story L4: Return a Book
- **As a** Customer / Librarian, **I want to** return a borrowed book to the same library.
