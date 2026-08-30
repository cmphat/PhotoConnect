# TASK-004: User Domain Model

## Overview
In this task, we designed and implemented the foundational `User` entity for PhotoConnect and successfully mapped it to the `users` table in our SQL Server database using Spring Data JPA. We also created a Spring Data Repository to handle standard CRUD operations.

## Key Concepts Explained

**What is an Entity?**
An Entity in Java is simply a lightweight, plain Java class that represents a domain model object in your application. For us, the `User` class is our entity.

**How an Entity maps to a database table**
Using Object-Relational Mapping (ORM) through JPA, the properties of our Java `User` class map directly to the columns of the `users` table in SQL Server.

**What `@Entity` does**
It's an annotation that tells Hibernate (our JPA implementation) that this Java class is an entity and should be managed for persistence.

**What `@Table` does**
While `@Entity` maps the class, `@Table` lets us specify the exact name of the database table (in this case, `users`). Without it, JPA might default to the class name (`user`), which is sometimes a reserved keyword in SQL.

**What `@Id` means**
It designates the primary key of the entity, uniquely identifying each record in the database table.

**What `@GeneratedValue` does**
It configures the way the primary key is generated. It tells JPA we don't want to assign IDs manually; the database should handle it.

**Why `IDENTITY` fits SQL Server identity columns**
The `GenerationType.IDENTITY` strategy maps perfectly to SQL Server's auto-incrementing `IDENTITY` column feature. SQL Server handles the ID generation under the hood when a new row is inserted.

**What `@Column` does**
It allows us to fine-tune the database column mapping. We can specify constraints like `nullable = false`, `unique = true`, maximum `length`, and a specific column `name`.

**Why email is unique**
In our system, users will likely log in with their email address, so we need to ensure that no two users share the same email.

**Why role/status use `EnumType.STRING`**
By default, JPA stores enums as integer ordinals (0, 1, 2). If we ever add a new enum value in the middle, or remove one, the ordinals shift, corrupting our database records. `EnumType.STRING` forces JPA to store the actual text values (`CUSTOMER`, `PHOTOGRAPHER`, etc.), which is much safer and more readable in the database.

**Difference between Java enum and SQL text value**
A Java enum is a type-safe list of predefined constants in our code. The SQL text value is the literal string (e.g., `"CUSTOMER"`) stored in the database column. `EnumType.STRING` bridges this gap.

**What `JpaRepository` provides**
By extending `JpaRepository`, Spring Data JPA automatically generates the implementation for our `UserRepository` interface at runtime. It gives us out-of-the-box methods for saving, deleting, and finding users without writing any SQL.

**What `findByEmail` does**
This is a Spring Data JPA "query method". By simply naming the method `findByEmail`, Spring automatically understands we want to query the database by the `email` field and generates the appropriate `SELECT * FROM users WHERE email = ?` query.

**What `existsByEmail` does**
Similarly, it generates an optimized query to check if any user with a given email exists, returning a simple boolean. It's faster than fetching the whole user object if we just want to check availability.

**What `ddl-auto=update` does**
This setting tells Hibernate to inspect our Entity classes at startup, compare them to the current database schema, and automatically execute SQL `CREATE` or `ALTER` commands to synchronize the database with our code.

**Why it is development-only**
`ddl-auto=update` is great for rapid local development because it automatically builds our tables. However, in production, it's extremely dangerous. It can cause unexpected downtime or data loss if it incorrectly alters a table. In production, we will use explicit migration scripts (like Flyway or Liquibase).

## Verification

### Tests Executed
- Created `UserRepositoryIntegrationTests`.
- Tested saving a user, reading a user by email, verifying the `existsByEmail` query, and confirming Enum persistence.
- Ran `mvn test` which successfully passed all unit and integration tests.

### Database Result
- The `users` table was automatically created in the SQL Server `PhotoConnect` database, including all columns: `id`, `email`, `password`, `full_name`, `phone`, `role`, `status`, `created_at`, and `updated_at`.
- **Manual Verification**: The table structure and columns were successfully verified in SQL Server Management Studio (SSMS) by the human user.

### Problems Encountered and Solutions Applied
- **Problem**: In a previous run, the embedded Tomcat server didn't cleanly shut down, leaving port 8080 bound.
- **Solution**: Executed `netstat -ano | Select-String ":8080"` to find the blocking PID and used `taskkill /F /PID <pid>` to forcefully terminate it before restarting Spring Boot.
