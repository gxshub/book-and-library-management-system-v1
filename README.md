# Book and Library Management System (AI-Driven Development Case Study)

<!--This repository is a **teaching project** that demonstrates a simple **AI-driven development workflow**.  
The goal is to show how requirements can move from written specs to an implemented microservice application, and how that evolution can be taught by comparing branches.
-->
## Changes in Iteration 2

- **CQRS Application Architecture**: Separated Library Service into Command Application Services (state mutation via aggregate roots) and Query Application Services (read-only projections).
- **Event Sourcing for Inventory**: Implemented event sourcing for `InventoryAggregate`, recording immutable state transition events (`InventoryCopyReserved`, `InventoryCopyReleased`, `InventoryAdjusted`, `InventoryTransferredOut`, `InventoryTransferredIn`) and projecting to `inventory_read_model`.
- **Expanded Circulation & Administrative Workflows (L1–L13)**:
  - Reservation Hold Queue management (FIFO position allocation & cancellation).
  - Loan Renewals (max renewal policy enforcement and hold conflict checks).
  - Customer Active Loans and Library Overdue Loans reporting.
  - Exception Reporting (lost or damaged book copy reporting).
  - Stock Management (manual inventory adjustments and inter-branch copy transfers).
  - Financial Ledger (overdue fine assessment, payments, and librarian waivers).

## Project Intention

This case study is designed to teach:

1. How to define requirements first (architecture, user stories, domain model, API contract).
2. How implementation can be developed from those specs using AI-assisted workflows.
3. How to review and explain progress using a **before/after branch comparison**.

## Branches 

Create and compare two branches: **spec-only** and **post-implementation**.

### Spec-Only (Main) Branch
Contains (1) the specification-first artifacts in the `specs/` folder
- `1-technical-architecture.md`
- `2-user-stories.md`
- `3-domain-models.md`
- `4-api-endpoints.md`
- `openapi-contract.yaml`

and (2) the prompt instructions in `agent-prompts/dev-agent.md` for the development agent.
(Some auxiliary files like the Maven wrapper are also included for convenience.)

### Post-Implementation Branch
contains the full implementation with:
  - `book-service` microservice
  - `library-service` microservice
  - Maven multi-module root `pom.xml`


## Agent-Assisted Development 
To implement the application using a coding agent, follow these steps: 
- check out the `main` branch, and
- run your coding agent with the provided prompt instructions in `agent-prompts/dev-agent.md`.

## Running the Implemented Version

Prerequisites:

- Java 21
- Maven (or use the included Maven wrapper)

Check out the `post-implementation` branch.
From the repository root:

```bash
./mvnw clean test
./mvnw spring-boot:run -pl book-service
./mvnw spring-boot:run -pl library-service
```

On Windows PowerShell, use:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run -pl book-service
.\mvnw.cmd spring-boot:run -pl library-service
```

## Implemented Fuctionality

The implemented version is a Spring Boot microservice system for managing books and library borrowing:

- **Book Service** (`http://localhost:8081/api/v1`)
  - Create, read, update, delete books
  - List all books
- **Library Service** (`http://localhost:8082/api/v1`)
  - Find libraries by ISBN
  - Check availability in a branch
  - Borrow and return books
  - Validates book existence via Book Service during borrow flow


