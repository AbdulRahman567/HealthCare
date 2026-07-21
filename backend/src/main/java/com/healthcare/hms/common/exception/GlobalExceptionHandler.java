package com.healthcare.hms.common.exception;

import com.healthcare.hms.common.api.ApiErrorResponse;
import com.healthcare.hms.common.api.ErrorDetail;
import com.healthcare.hms.common.exception.auth.AccountNotActiveException;
import com.healthcare.hms.common.exception.auth.AuthenticationException;
import com.healthcare.hms.common.exception.auth.EmailNotVerifiedException;
import com.healthcare.hms.common.exception.auth.ExpiredTokenException;
import com.healthcare.hms.common.exception.auth.ForbiddenException;
import com.healthcare.hms.common.exception.auth.InvalidCredentialsException;
import com.healthcare.hms.common.exception.auth.InvalidTokenException;
import com.healthcare.hms.common.exception.auth.TokenValidationException;
import com.healthcare.hms.common.exception.auth.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Centralized exception-to-API-error mapping. Never exposes stack traces to clients.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<ApiErrorResponse> handleExpiredToken(
            final ExpiredTokenException exception,
            final HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception, request.getRequestURI());
    }

    @ExceptionHandler({InvalidTokenException.class, TokenValidationException.class})
    public ResponseEntity<ApiErrorResponse> handleInvalidToken(
            final AuthenticationException exception,
            final HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception, request.getRequestURI());
    }

    @ExceptionHandler({
            InvalidCredentialsException.class,
            AccountNotActiveException.class,
            EmailNotVerifiedException.class,
            UnauthorizedException.class
    })
    public ResponseEntity<ApiErrorResponse> handleCredentialFailures(
            final AuthenticationException exception,
            final HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception, request.getRequestURI());
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(
            final ForbiddenException exception,
            final HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.FORBIDDEN, exception, request.getRequestURI());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthentication(
            final AuthenticationException exception,
            final HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.UNAUTHORIZED, exception, request.getRequestURI());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            final ResourceNotFoundException exception,
            final HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, exception, request.getRequestURI());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleConflict(
            final ConflictException exception,
            final HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.CONFLICT, exception, request.getRequestURI());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(
            final BusinessException exception,
            final HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            final DataIntegrityViolationException exception,
            final HttpServletRequest request
    ) {
        log.warn("Data integrity violation on {}: {}", request.getRequestURI(), exception.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        "Request conflicts with existing data",
                        "DATA_INTEGRITY_VIOLATION",
                        request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            final AccessDeniedException exception,
            final HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiErrorResponse.of(
                        "Access denied",
                        "ACCESS_DENIED",
                        request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            final MethodArgumentNotValidException exception,
            final HttpServletRequest request
    ) {
        final List<ErrorDetail> errors = exception.getBindingResult().getFieldErrors().stream()
                .map(this::toErrorDetail)
                .toList();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiErrorResponse.of(
                        "Validation failed",
                        "VALIDATION_ERROR",
                        errors,
                        request.getRequestURI()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            final ConstraintViolationException exception,
            final HttpServletRequest request
    ) {
        final List<ErrorDetail> errors = exception.getConstraintViolations().stream()
                .map(violation -> new ErrorDetail(
                        violation.getPropertyPath() == null ? null : violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .toList();
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiErrorResponse.of(
                        "Validation failed",
                        "VALIDATION_ERROR",
                        errors,
                        request.getRequestURI()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(
            final HttpMessageNotReadableException exception,
            final HttpServletRequest request
    ) {
        log.warn("Malformed request body on {}: {}", request.getRequestURI(), exception.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        "Malformed JSON request body",
                        "MALFORMED_REQUEST",
                        request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(
            final MethodArgumentTypeMismatchException exception,
            final HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        "Invalid request parameter",
                        "INVALID_PARAMETER",
                        request.getRequestURI()));
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ApiErrorResponse> handleApplication(
            final ApplicationException exception,
            final HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception, request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            final Exception exception,
            final HttpServletRequest request
    ) {
        log.error("Unhandled exception on {}", request.getRequestURI(), exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(
                        "An unexpected error occurred",
                        "INTERNAL_ERROR",
                        request.getRequestURI()));
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            final HttpStatus status,
            final ApplicationException exception,
            final String path
    ) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(exception.getMessage(), exception.getErrorCode(), path));
    }

    private ErrorDetail toErrorDetail(final FieldError fieldError) {
        return new ErrorDetail(fieldError.getField(), fieldError.getDefaultMessage());
    }
}
