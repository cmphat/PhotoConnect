package com.photoconnect.exception;

import com.photoconnect.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Centralized global exception handler for PhotoConnect.
 *
 * Implements dual-mode exception handling:
 * 1. REST / API requests (/api/** or Accept: application/json) receive standard ApiResponse<T> JSON envelopes.
 * 2. Web browser requests (HTML) render the branded editorial error.jsp with appropriate status and messaging.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── Domain Exception Handler ─────────────────────────────────────────────

    @ExceptionHandler(PhotoConnectException.class)
    public Object handlePhotoConnectException(PhotoConnectException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        HttpStatus status = errorCode != null ? errorCode.getHttpStatus() : HttpStatus.BAD_REQUEST;
        String message = ex.getMessage();

        log.warn("Domain exception [{}]: {}", errorCode, message);

        if (isJsonRequest(request)) {
            return ResponseEntity.status(status).body(ApiResponse.error(errorCode, message));
        }

        return createErrorModelAndView(status, "Action Failed", message, errorCode != null ? errorCode.getCode() : "ERROR");
    }

    // ── Validation Exception Handlers ────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Validation failed on {}: {}", request.getRequestURI(), fieldErrors);

        if (isJsonRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.validationError("Validation failed for submitted data", fieldErrors));
        }

        return createErrorModelAndView(HttpStatus.BAD_REQUEST, "Validation Error",
                "The submitted form contains validation errors. Please review the inputs and try again.",
                ErrorCode.SYSTEM_003_VALIDATION_ERROR.getCode());
    }

    @ExceptionHandler(BindException.class)
    public Object handleBindException(BindException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        log.warn("Binding error on {}: {}", request.getRequestURI(), fieldErrors);

        if (isJsonRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.validationError("Validation failed for submitted data", fieldErrors));
        }

        return createErrorModelAndView(HttpStatus.BAD_REQUEST, "Validation Error",
                "The submitted form contains invalid data.", ErrorCode.SYSTEM_003_VALIDATION_ERROR.getCode());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Object handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }

        log.warn("Constraint violation on {}: {}", request.getRequestURI(), errors);

        if (isJsonRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.validationError("Constraint violation", errors));
        }

        return createErrorModelAndView(HttpStatus.BAD_REQUEST, "Invalid Input",
                "A submitted value violated system constraints.", ErrorCode.SYSTEM_003_VALIDATION_ERROR.getCode());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument on {}: {}", request.getRequestURI(), ex.getMessage());

        if (isJsonRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ErrorCode.SYSTEM_003_VALIDATION_ERROR, ex.getMessage()));
        }

        return createErrorModelAndView(HttpStatus.BAD_REQUEST, "Invalid Request",
                ex.getMessage(), ErrorCode.SYSTEM_003_VALIDATION_ERROR.getCode());
    }

    // ── HTTP & Framework Handlers ────────────────────────────────────────────

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Method not allowed: {} on {}", ex.getMethod(), request.getRequestURI());

        if (isJsonRequest(request)) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .body(ApiResponse.error("HTTP_405_METHOD_NOT_ALLOWED", ex.getMessage()));
        }

        return createErrorModelAndView(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed",
                "The requested HTTP method " + ex.getMethod() + " is not supported for this action.", "HTTP_405");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        log.warn("Missing parameter '{}' on {}", ex.getParameterName(), request.getRequestURI());

        if (isJsonRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ErrorCode.SYSTEM_003_VALIDATION_ERROR, ex.getMessage()));
        }

        return createErrorModelAndView(HttpStatus.BAD_REQUEST, "Missing Parameter",
                ex.getMessage(), ErrorCode.SYSTEM_003_VALIDATION_ERROR.getCode());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Object handleMaxUploadSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
        log.warn("Upload size exceeded on {}", request.getRequestURI());

        if (isJsonRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(ErrorCode.PORTFOLIO_003_UPLOAD_FAILED, "Uploaded file exceeds maximum allowed size"));
        }

        return createErrorModelAndView(HttpStatus.BAD_REQUEST, "File Too Large",
                "The uploaded file exceeds the platform's maximum allowed file size.",
                ErrorCode.PORTFOLIO_003_UPLOAD_FAILED.getCode());
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public Object handleNotFound(Exception ex, HttpServletRequest request) {
        log.info("Resource not found: {}", request.getRequestURI());

        if (isJsonRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ErrorCode.BOOKING_001_NOT_FOUND.getCode(), "The requested resource was not found"));
        }

        return createErrorModelAndView(HttpStatus.NOT_FOUND, "Page Not Found",
                "The page or resource you requested does not exist or has been moved.", "HTTP_404");
    }

    // ── Global Fallback Handler ──────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public Object handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled server exception on " + request.getRequestURI(), ex);

        if (isJsonRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(ErrorCode.SYSTEM_002_INTERNAL_ERROR,
                            "An unexpected server error occurred. Please try again later."));
        }

        return createErrorModelAndView(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected server error occurred. Our engineering team has been notified.",
                ErrorCode.SYSTEM_002_INTERNAL_ERROR.getCode());
    }

    // ── Helper Methods ───────────────────────────────────────────────────────

    /**
     * Determines whether the incoming request expects a JSON response.
     */
    private boolean isJsonRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        String uri = request.getRequestURI();
        if (uri != null && uri.startsWith("/api/")) {
            return true;
        }

        String accept = request.getHeader("Accept");
        if (accept != null && (accept.contains(MediaType.APPLICATION_JSON_VALUE) || accept.contains("application/json"))) {
            return true;
        }

        String contentType = request.getContentType();
        return contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE);
    }

    private ModelAndView createErrorModelAndView(HttpStatus status, String title, String message, String errorCode) {
        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(status);
        mav.addObject("statusCode", status.value());
        mav.addObject("errorTitle", title);
        mav.addObject("errorMessage", message);
        mav.addObject("errorCode", errorCode);
        return mav;
    }
}
