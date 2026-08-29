# Technical Architecture (Iteration 2)

## Microservice Architecture
This application contains two **Microservice Modules**:
- **Book Service**
- **Library Service**

## Layered Architecture
Each microservice module is structured into four distinct layers:
1. **Presentation Layer** (Controllers)
2. **Service Layer** (Business Logic)
3. **Domain Layer** (Core Domain Models & Rules)
4. **Data Access Layer** (Repositories)

## CQRS View (Library Service)
Library Service applies CQRS with logical separation inside the same service module:
1. **Command application service**: handles state-changing use cases, executes aggregate rules, and persists writes.
2. **Query application service**: handles read-only use cases from read models/projections.
3. **Projection update flow**: command-side domain events update read-side views used by query handlers.
4. **Aggregate usage boundary**: only command handlers execute aggregate behavior; query handlers do not load aggregates for business mutation paths.

## Repository Structure
The project is structured as a multi-module repository:
- `book-service`: Module for the Book Service
- `library-service`: Module for the Library Service

## Technology Stack
- **JDK & Build**: Java 21, Apache Maven (Multi-module POM)
- **Framework**: Spring Boot
    - **Controller Layer**: `@RestController`
    - **Service Layer**: `@Service`
    - **Domain Layer**: `@Entity`
    - **Data Access Layer**: `@Repository`
- **Database**: H2 in-memory database for development
- **Unit & Integration Testing**: `MockMvc`, `@SpringBootTest`, `JUnit 5`
