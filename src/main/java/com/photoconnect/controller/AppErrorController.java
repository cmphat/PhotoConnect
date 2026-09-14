package com.photoconnect.controller;

import com.photoconnect.dto.ApiResponse;
import com.photoconnect.exception.ErrorCode;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller handling container-level error dispatches to /error.
 * Replaces Spring Boot's default BasicErrorController to ensure uniform
 * API_CONTRACT.md compliance for JSON callers and branded editorial error.jsp for browsers.
 */
@Controller
public class AppErrorController implements ErrorController {

    @RequestMapping("/error")
    public Object handleError(HttpServletRequest request) {
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
        if (statusObj != null) {
            try {
                statusCode = Integer.parseInt(statusObj.toString());
            } catch (NumberFormatException ignored) {
            }
        }

        HttpStatus status = HttpStatus.resolve(statusCode);
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        Object messageObj = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        String message = (messageObj != null && !messageObj.toString().isBlank())
                ? messageObj.toString()
                : getDefaultMessageForStatus(status);

        String errorCode = getErrorCodeForStatus(status);

        if (isJsonRequest(request)) {
            return ResponseEntity.status(status).body(ApiResponse.error(errorCode, message));
        }

        ModelAndView mav = new ModelAndView("error");
        mav.setStatus(status);
        mav.addObject("statusCode", status.value());
        mav.addObject("errorTitle", getTitleForStatus(status));
        mav.addObject("errorMessage", message);
        mav.addObject("errorCode", errorCode);
        return mav;
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        String uri = (String) request.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
        if (uri == null) {
            uri = request.getRequestURI();
        }

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

    private String getTitleForStatus(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "Page Not Found";
            case FORBIDDEN -> "Access Denied";
            case UNAUTHORIZED -> "Authentication Required";
            case BAD_REQUEST -> "Bad Request";
            case METHOD_NOT_ALLOWED -> "Method Not Allowed";
            default -> "Server Error";
        };
    }

    private String getDefaultMessageForStatus(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "The requested page or resource could not be found.";
            case FORBIDDEN -> "You do not have permission to access this resource.";
            case UNAUTHORIZED -> "You must be signed in to perform this action.";
            case BAD_REQUEST -> "The request could not be processed due to invalid syntax or parameters.";
            case METHOD_NOT_ALLOWED -> "The HTTP method used is not supported for this URL.";
            default -> "An unexpected error occurred while processing your request.";
        };
    }

    private String getErrorCodeForStatus(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> ErrorCode.BOOKING_001_NOT_FOUND.getCode();
            case FORBIDDEN, UNAUTHORIZED -> ErrorCode.AUTH_005_ACCESS_DENIED.getCode();
            case BAD_REQUEST -> ErrorCode.SYSTEM_003_VALIDATION_ERROR.getCode();
            default -> ErrorCode.SYSTEM_002_INTERNAL_ERROR.getCode();
        };
    }
}
