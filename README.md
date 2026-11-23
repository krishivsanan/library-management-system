# Library Management System

Console-based Java 25 + JDBC + MySQL application.

## Requirements
- JDK 25
- MySQL 8.0.16+ (InnoDB; enforced CHECK constraints)
- Maven (or IntelliJ Maven integration)

## First-time database setup
1. Open MySQL Workbench or the MySQL command line.
2. Run the complete script `database/schema.sql`. It creates `library_management` and the four required tables if they do not already exist. It does not drop or alter existing tables.
3. If you already have these tables, compare their columns, constraints and foreign keys with the script before changing anything. `CREATE TABLE IF NOT EXISTS` does not migrate an existing table.

## Database connection
Configure environment variables in IntelliJ Run Configuration for `com.library.app.Main`:
- `LIBRARY_DB_URL` (optional; default `jdbc:mysql://localhost:3306/library_management`)
- `LIBRARY_DB_USER` (optional; default `root`)
- `LIBRARY_DB_PASSWORD` (required)

Do not store the password in source code.

## Build and tests
From the project root run:
```bash
mvn clean test
```
The automated unit tests cover the fine calculation rules and do not require MySQL. An opt-in end-to-end MySQL test covers adding/searching records, issue/return, stock restoration and fine payment. Run it only against a dedicated test database after loading `database/schema.sql`, with `LIBRARY_DB_TESTS=true` and the usual connection variables set. The test creates uniquely named records and deletes those records afterward.

## Run
Run `com.library.app.Main` from IntelliJ, or use Maven/Java with the compiled classes and dependency classpath.

## Features
- Book CRUD, search and available-book listing
- Member registration, update, search and deletion (referential integrity prevents deleting members with issue history)
- Issue books only to ACTIVE members and only when stock is available
- 14-day borrowing period
- Return transaction updates issue status, restores stock and creates any fine atomically
- Fine rate: Rs. 5 per overdue day; pending fine payment
- Reports for currently issued, overdue and returned books
- Return-date entry supports testing an overdue return; press Enter to use today's date

## Important behaviour
- A return date before the issue date is rejected.
- An issue can be returned only once.
- A book's total inventory cannot be reduced below the number of copies currently on loan.
- MySQL integration tests must be run against your own local database; no connection to your computer's MySQL server is available from this environment.
