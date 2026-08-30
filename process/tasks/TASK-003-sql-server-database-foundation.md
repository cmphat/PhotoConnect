# TASK-003: SQL Server Database Foundation

## Goal
Connect the existing PhotoConnect Spring Boot application to a real local Microsoft SQL Server database.

## Context
Database name: `PhotoConnect`
SQL Server connection:
Host: localhost
Port: 1433
Authentication: SQL Server Authentication
Username: sa
(Password provided externally)
This task is database infrastructure only. No business entities will be implemented yet.

## Requirements
- Add Spring Data JPA
- Add MSSQL JDBC driver
- Configure datasource using environment variables
- Ensure safe JPA behavior (ddl-auto=none)
- Do NOT store credentials in tracked files

## Files expected to change
- pom.xml
- src/main/resources/application.properties
- src/test/java/com/photoconnect/DatabaseConnectionIntegrationTests.java
- docs/development/TASK-003-sql-server-database-foundation.md
- docs/final/project-status.md

## Checklist
- [x] Inspect .agents instructions
- [x] Inspect current Maven configuration
- [x] Inspect current Spring configuration
- [x] Verify SQL Server connection requirements
- [x] Add Spring Data JPA
- [x] Add Microsoft SQL Server JDBC driver
- [x] Configure datasource
- [x] Keep database credentials outside tracked source files
- [x] Configure safe JPA behavior
- [x] Create database connectivity integration test
- [x] Follow TDD red-green-refactor where applicable
- [x] Run automated tests
- [x] Verify SQL Server connectivity
- [x] Build WAR successfully
- [x] Start application successfully
- [x] Verify existing homepage still responds
- [x] Write development documentation
- [x] Update final project status
- [ ] Await human browser verification

## TDD plan
Write a `DatabaseConnectionIntegrationTests` that executes `SELECT 1;` via `JdbcTemplate`.

## Verification commands
- `mvn test` (with DB env vars)
- `mvn clean package`
- `mvn spring-boot:run`

## Problems encountered
- Encountered "Port 8080 was already in use" from previous task running in background. Used `netstat` to find PID and `taskkill` to stop it.

## Result
Automated tests: PASS
SQL Server connectivity: PASS
Maven build: PASS
Application startup: PASS
Homepage HTTP verification: PASS
Human browser verification: PENDING

## Status
COMPLETE (Automated steps finished, waiting for user verification)
