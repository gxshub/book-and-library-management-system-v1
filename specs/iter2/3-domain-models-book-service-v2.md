# Domain Models Specification

Inherited from 'specs/3-domain-models.md'.

## 1. Domain Classes and Related User Stories

| Service | User Story | Domain Classes |
| :--- | :--- | :--- |
| **Book Service** | **B1. Create/Get/Update/Delete Book** | `Book` |
| **Book Service** | **B2. List All Books** | `Book` |
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
