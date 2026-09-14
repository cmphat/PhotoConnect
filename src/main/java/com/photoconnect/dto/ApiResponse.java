package com.photoconnect.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.photoconnect.exception.ErrorCode;

import java.util.Map;

/**
 * Standardized JSON API response envelope according to docs/API_CONTRACT.md.
 *
 * @param <T> Payload data type
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private String errorCode;
    private String message;
    private Map<String, String> errors;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, T data, String errorCode, String message, Map<String, String> errors) {
        this.success = success;
        this.data = data;
        this.errorCode = errorCode;
        this.message = message;
        this.errors = errors;
    }

    // ── Static Factory Helpers ───────────────────────────────────────────────

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, "OK", null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, null, message, null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, errorCode.getCode(), errorCode.getDefaultMessage(), null);
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        String finalMsg = (message != null && !message.isBlank()) ? message : errorCode.getDefaultMessage();
        return new ApiResponse<>(false, null, errorCode.getCode(), finalMsg, null);
    }

    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return new ApiResponse<>(false, null, errorCode, message, null);
    }

    public static <T> ApiResponse<T> validationError(String message, Map<String, String> errors) {
        String finalMsg = (message != null && !message.isBlank()) ? message : ErrorCode.SYSTEM_003_VALIDATION_ERROR.getDefaultMessage();
        return new ApiResponse<>(false, null, ErrorCode.SYSTEM_003_VALIDATION_ERROR.getCode(), finalMsg, errors);
    }

    // ── Getters and Setters ──────────────────────────────────────────────────

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
