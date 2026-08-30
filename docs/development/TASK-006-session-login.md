# TASK-006: Session Login

## Concepts Explained

### What Authentication Means
Authentication is the process of verifying who a user is. When you log in with your email and password, the system checks those credentials to confirm your identity.

### Difference Between Authentication and Authorization
Authentication proves **who you are** (e.g., you are Jane Doe). Authorization determines **what you are allowed to do** (e.g., Jane Doe is an ADMIN and can delete users, while John Smith is a CUSTOMER and cannot).

### What an HTTP Session Is
An HTTP Session is a way to store data across multiple requests from the same user. Since HTTP is stateless (each request is independent), a session allows the server to "remember" that a user has logged in as they navigate from page to page.

### Why the Server Stores Session State
Storing session state allows the server to track a user's logged-in status and identity without requiring them to re-enter their password for every single action on the website.

### What a Session Cookie Does
When a session is created, the server sends a unique session ID to the user's browser in a cookie (e.g., `JSESSIONID`). The browser automatically sends this cookie back with every subsequent request, allowing the server to match the request to the correct session data stored in memory.

### What HttpSession Does
In Java Spring Boot, `HttpSession` is the object that manages this server-side session data. You can set attributes (like `userId` or `userRole`) on it, and Spring automatically handles the underlying session ID cookie exchange.

### Why Raw Passwords Are Never Stored in Session
The session should only contain safe, necessary identity information. Storing a raw password in a session creates a severe security risk. If an attacker gains access to the session memory (e.g., through a server vulnerability or memory dump), they could extract the passwords of all currently logged-in users. 

### How BCrypt `matches()` Works
BCrypt uses a one-way mathematical function to hash passwords. Because we cannot reverse the stored hash to see the original password, `matches()` takes the raw password provided during login, hashes it using the same salt included in the stored hash, and checks if the two resulting hashes are identical.

### Why Login Error is Generic
If a user enters a wrong password, or an email that isn't registered, we return a generic "Email or password is incorrect." message. If we instead said "Email not found", an attacker could use the login form to figure out exactly which emails are registered in our system (Account Enumeration).

### Why Logout Should Invalidate Session
Calling `session.invalidate()` completely destroys the session data on the server. If we just removed attributes, the session ID might still be valid. Invalidating it ensures that if an attacker intercepts the session cookie, they cannot use it after the legitimate user has logged out.

### Difference Between Session Auth and JWT
- **Session Auth**: State is stored on the server. The client only holds a session ID. It's easier to manage and instantly revoke (e.g., logout), but can be harder to scale across many servers.
- **JWT (JSON Web Token)**: State is stored on the client within the token itself. The server doesn't need to remember sessions, making it highly scalable, but tokens are harder to revoke instantly before they expire.

## Tests Executed
- `AuthServiceTest`: Valid login, wrong password, unknown email, inactive account, and banned account.
- `LoginControllerTest`: Form rendering, validation failure handling, wrong credentials POST handling, successful session creation and redirect, and logout logic.

## Problems Encountered and Solutions
- `mvn test` execution requires `$env:DB_PASSWORD` to be set since the test environment connects to the real SQL Server for integration tests. Resolved by passing the environment variable directly to the Maven command line.
