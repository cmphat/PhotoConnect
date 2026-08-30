# TASK-005: User Registration

## Concepts Explained

### What is a DTO?
A DTO (Data Transfer Object) is a simple object used to transfer data between the client (like a web browser) and the server. In our case, `RegisterRequest` collects the form fields submitted by the user.

### Why RegisterRequest is not a User entity
Entities like `User` map directly to database tables and often contain fields we don't want users to manipulate directly (like `id`, `role`, `status`). By using a separate `RegisterRequest` DTO, we can specify exactly what the user is allowed to submit and apply validation rules specific to registration (like `confirmPassword`), keeping our persistence logic separate from web concerns.

### What Bean Validation does
Bean Validation is a standard way to ensure that the data in our Java objects meets certain rules before we process it. It prevents bad or malicious data from causing errors later.

### What `@Valid` does
The `@Valid` annotation tells Spring to check the incoming `RegisterRequest` against the validation rules (like `@NotBlank` or `@Size`) before handing it over to the controller method. 

### What `BindingResult` does
`BindingResult` holds the outcome of the `@Valid` checks. If validation fails, `BindingResult` contains the specific errors (e.g., "Email is required"), which we can then display back to the user on the form.

### Why Controller should not contain database logic
Controllers are responsible for handling HTTP requests and responses (web routing). Putting database logic in them makes the code hard to test, hard to reuse, and violates the Single Responsibility Principle. We delegate business logic to the `UserService` instead.

### What UserService does
`UserService` acts as a middleman between the web layer and the database layer. It holds the core business rules: checking for duplicate emails, ensuring passwords match, hashing the password, setting default roles, and saving the `User`.

### Why duplicate email is checked in service
While the database has a unique constraint to prevent duplicate emails, checking it in the service allows us to catch the error early and return a friendly, translated message (like `EmailAlreadyExistsException`) instead of a generic database crash.

### What BCrypt is
BCrypt is a powerful, standard password hashing algorithm. It is designed to be slow and computationally expensive to resist brute-force attacks.

### Why passwords must be hashed
Storing plain-text passwords means anyone with database access can see them. Hashing scrambles the password into an unrecognizable string. 

### Why password hashing is one-way
Hashing is irreversible. When a user logs in, we hash the password they typed and compare it to the stored hash. If they match, the password is correct, but we never know the actual password. This protects users even if the database is compromised.

### Why CUSTOMER and ACTIVE are assigned automatically
When a public visitor registers, they should not be able to choose their role (like `ADMIN` or `PHOTOGRAPHER`) for security reasons. We safely default them to a standard `CUSTOMER` with an `ACTIVE` status.

### What PRG (Post/Redirect/Get) means
The Post/Redirect/Get pattern prevents duplicate form submissions. When a `POST` request (like submitting registration) succeeds, the server returns a redirect (`302`) to a `GET` URL (like `/register?success`) instead of rendering the page directly. This ensures that if the user refreshes the page, they simply reload the `GET` request rather than resubmitting the form.

## Tests Executed
- **Service Tests (`UserServiceTest`)**: Verified successful registration, checked BCrypt password hashing logic, verified duplicate email exception, and verified password mismatch exception.
- **Controller Tests (`RegisterControllerTest`)**: Verified GET rendering of the form, POST validation failures (remaining on the form), and POST success (redirecting to `/register?success`).

## Problems Encountered and Solutions
- **Problem**: `mvn test` failed initially because the database password was not provided to the test environment.
  - **Solution**: Executed maven tests with the database password provided via an environment variable (`$env:DB_PASSWORD`).
