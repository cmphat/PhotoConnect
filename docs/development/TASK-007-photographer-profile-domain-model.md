# TASK-007: Photographer Profile Domain Model

## Concepts Explained

### Why User and PhotographerProfile are Separate Tables
A user might be a customer, an admin, or a photographer. If we put all photographer-specific fields (bio, experience years, pricing, portfolio links) directly into the `users` table, most rows (for customers and admins) would have these fields empty (NULL). Separating the photographer data into `photographer_profiles` keeps the `users` table clean and ensures we only allocate space for photographer details when actually needed.

### What One-to-One Means
A one-to-one relationship means that exactly one record in the `users` table corresponds to exactly one record in the `photographer_profiles` table. A user cannot have multiple photographer profiles, and a profile cannot belong to multiple users.

### What a Foreign Key Means
A foreign key is a column in one table that links to the primary key of another table. In our case, `user_id` in `photographer_profiles` is a foreign key that points to the `id` column in the `users` table. This creates a hard link between the two records, enforcing referential integrity (e.g., you cannot create a profile for a user ID that does not exist).

### What `@OneToOne` Does
In JPA/Hibernate, `@OneToOne` tells the ORM framework that the Entity object should be mapped using a one-to-one database relationship. It allows us to seamlessly navigate from a `PhotographerProfile` object in Java directly to its associated `User` object.

### What `@JoinColumn` Does
`@JoinColumn` specifies the exact physical column in the database table (`user_id`) that will hold the foreign key. It allows us to define constraints like `nullable = false` (a profile must belong to a user) and `unique = true` (enforcing the one-to-one rule at the database level).

### Why `user_id` is Unique
If `user_id` wasn't unique, it would be possible to insert multiple rows in `photographer_profiles` with the same `user_id`, which would violate our one-to-one business rule. The database unique constraint acts as the ultimate safeguard against duplicate profiles for the same user.

### Why BigDecimal is Used for Money
Floating-point data types (`float` and `double`) are designed for scientific calculations where extreme range is needed but slight precision loss is acceptable. When dealing with currency, losing a fraction of a cent due to rounding errors is unacceptable. `BigDecimal` provides exact precision for financial calculations.

### Why Verification Status Begins at PENDING
By default, anyone registering to be a photographer must undergo a manual verification process by an admin (checking ID cards, portfolios, etc.) before their profile is visible to customers. Defaulting to `PENDING` ensures that new profiles are quarantined and not `APPROVED` by accident.

### Why `EnumType.STRING` is Used
By default, JPA saves enums using their ordinal (numeric index) values. If we used ordinals (e.g., `0` for PENDING, `1` for APPROVED) and later added a new status in the middle of the Java enum, all database numbers would suddenly map to the wrong status. Using `EnumType.STRING` saves the literal text (e.g., "PENDING") in the database, making it safe to reorder the Java enum and much easier to read when querying the database manually.

### Why Destructive Cascade is Avoided
If we placed `CascadeType.ALL` or `CascadeType.REMOVE` on the `user` relationship inside `PhotographerProfile`, deleting a profile would automatically delete the user account. We want to be able to delete a photographer's professional profile without destroying their core customer account and login credentials.

## Repository Methods Added
- `findByUserId(Long userId)`: Returns an `Optional<PhotographerProfile>`. Used to fetch a user's profile efficiently by their User ID.
- `existsByUserId(Long userId)`: Returns a boolean. Efficiently checks if a profile exists for a given user without loading the entire profile object into memory.

## Integration Tests
`PhotographerProfileRepositoryIntegrationTests` performs the following checks:
- Saves a `User` (role = PHOTOGRAPHER).
- Saves a `PhotographerProfile` linked to the User.
- Asserts that all fields, especially the default `PENDING` status and `BigDecimal` price, are persisted properly.
- Attempts to save a second profile for the same user and asserts that a `DataIntegrityViolationException` is thrown, proving the unique constraint works.

## Problems Encountered and Solutions
- `mvn spring-boot:run` failed initially because port 8080 was occupied by the previous task's server process. We resolved this by identifying and killing the background process on port 8080 before starting the new application context.
