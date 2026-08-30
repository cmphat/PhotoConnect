# TASK-002: JSP Home Page

## Overview
This document covers the implementation of the first working JSP home page for PhotoConnect. It proves the integration of Spring Boot, WAR packaging, and JSP/JSTL rendering works as expected.

## Implementation Details

### Configuration
* **Dependencies**: Added `tomcat-embed-jasper` and `jakarta.servlet.jsp.jstl-api` (along with the `org.glassfish.web` implementation) to `pom.xml` to enable JSP rendering with Spring Boot 3 / Jakarta.
* **View Resolver**: Configured `spring.mvc.view.prefix=/WEB-INF/views/` and `spring.mvc.view.suffix=.jsp` in `application.properties`. This allows Spring MVC to automatically resolve the view name `index` to the physical file path `src/main/webapp/WEB-INF/views/index.jsp`.

### Source Code
* **Directory structure**: Created `src/main/webapp/WEB-INF/views/` directory. JSP files must reside in `WEB-INF` in a Java web application to prevent direct unauthorized access via the browser; they should only be served via Controller routing.
* **Controller**: Created `HomeController` with a single mapping: `GET /` resolving to `"index"`.
* **View**: Implemented `index.jsp` containing the PhotoConnect hero section and navigation bar. Integrated Bootstrap CSS/JS via CDN for responsive styling. 

### Testing (TDD)
* Wrote `HomeControllerTest` using `MockMvc` first. Running `mvn test` initially failed as the controller hadn't been written (verifying the RED state). 
* Implemented the Controller and JSP, then ran `mvn test` again, which passed (GREEN).

## Verification
* **Test Command**: `mvn test` 
* **Build Command**: `mvn clean package`
* **Run Command**: `mvn spring-boot:run`
* **Final URL**: `http://localhost:8080/`

Manual verification confirmed that a `GET` request to `http://localhost:8080/` returns `HTTP 200 OK` and correctly renders the HTML content mapped to the JSP file.

## Issues Encountered
* None. The setup integrated smoothly.
