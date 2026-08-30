# TASK-003: SQL Server Database Foundation

## Overview
In this task, we connected our existing PhotoConnect Spring Boot application to a local Microsoft SQL Server database named `PhotoConnect`. This sets up the foundational database infrastructure needed for future features like users, photographers, and bookings.

## Key Concepts Explained

**What is SQL Server?**
Microsoft SQL Server is a relational database management system (RDBMS). It is used to store and retrieve data as requested by our application.

**What is SSMS?**
SSMS (SQL Server Management Studio) is a graphical tool provided by Microsoft for configuring, managing, and administering SQL Server components.

**Difference between SQL Server and SSMS**
SQL Server is the actual database engine that runs in the background to store and manage data. SSMS is just the visual interface you use as a human to interact with SQL Server.

**What is JDBC?**
JDBC (Java Database Connectivity) is an API in Java that defines how a client may access a database. It provides methods to query and update data in a relational database.

**What the Microsoft JDBC Driver does**
The Microsoft JDBC Driver for SQL Server is a software component that allows our Java application to communicate specifically with SQL Server using standard JDBC commands.

**What is a DataSource?**
A `DataSource` is a factory for database connections. It manages the connection details (URL, username, password) and provides the application with a way to obtain a connection to the database.

**What is Spring Data JPA?**
Spring Data JPA adds an extra layer of abstraction on top of standard JPA (Java Persistence API). It significantly reduces boilerplate code required to implement data access layers (repositories) in Spring applications.

**Difference between JPA and SQL Server**
SQL Server is the physical database where data is stored. JPA is the Java specification for mapping Java objects (entities) to those relational database tables (Object-Relational Mapping, or ORM). JPA is the concept, and SQL Server is the destination.

**What is a Database Connection Pool?**
Opening and closing database connections for every request is slow. A connection pool maintains a cache of database connections that can be reused when future requests to the database are required, greatly improving application performance. We use HikariCP, which is the default in Spring Boot.

**Why environment variables are used**
Environment variables are used to store sensitive configuration data (like database passwords) outside of the source code. This prevents secrets from being committed to version control, which is a major security risk. 

**What `ddl-auto=none` means**
This setting tells Hibernate (the JPA implementation) *not* to automatically modify the database schema (e.g., creating or dropping tables). This is safer for production environments and prevents accidental data loss during startup. We will manage the database schema manually or with a migration tool later.

**What `SELECT 1` proves**
A simple `SELECT 1` query is a common way to verify that a database connection is active and can successfully execute a query and return a result. It doesn't rely on any application-specific tables existing.

## Verification

### Tests Executed
- Created `DatabaseConnectionIntegrationTests` to execute `SELECT 1` using `JdbcTemplate`.
- Ran `mvn test` which executed all tests (including `HomeControllerTest`). All tests passed successfully.

### Maven Commands Executed
- `mvn test`: Executed unit and integration tests successfully.
- `mvn clean package`: Built the WAR artifact successfully.
- `mvn spring-boot:run`: Started the application context, embedded Tomcat, and successfully connected to SQL Server using HikariCP.

### Runtime Verification
- Embedded Tomcat started on port 8080.
- Datasource successfully connected to `jdbc:sqlserver://localhost:1433;databaseName=PhotoConnect`.
- Homepage HTTP `GET /` request successfully returned HTTP 200 with the JSP content.

### Problems Encountered and Solutions Applied
- **Problem**: When running `mvn spring-boot:run` for the second time, the web server failed to start because port 8080 was already in use (the previous server from TASK-002 was still hanging).
- **Solution**: Found the lingering process using `netstat` and killed it using `taskkill`. The application then started successfully.
