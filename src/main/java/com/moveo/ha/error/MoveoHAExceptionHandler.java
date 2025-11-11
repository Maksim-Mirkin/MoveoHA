package com.moveo.ha.error;

import com.moveo.ha.dto.error.ExceptionDTO;
import com.moveo.ha.dto.error.InternalServerExceptionDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for the Moveo Home Assignment application.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Convert thrown exceptions into consistent API error payloads (DTOs).</li>
 *     <li>Respect {@link ResponseStatus} on custom exceptions extending {@link MoveoHAException}.</li>
 *     <li>Log all handled exceptions with one-line messages:
 *         <ul>
 *             <li>4xx → {@code log.warn}</li>
 *             <li>5xx → {@code log.error}</li>
 *         </ul>
 *     </li>
 * </ul>
 * <p>
 * Rules:
 * <ol>
 *     <li>If exception is a {@link MoveoHAException} and it has {@link ResponseStatus} → use its status.</li>
 *     <li>If exception is a {@link MoveoHAException} but without {@link ResponseStatus} → treat as 500.</li>
 *     <li>Other specific technical exceptions are mapped to 400/403, see handlers below.</li>
 *     <li>Everything else → 500 with {@link InternalServerExceptionDTO}.</li>
 * </ol>
 */
@Slf4j
@ControllerAdvice
public class MoveoHAExceptionHandler {

    private final HttpStatus badRequest = HttpStatus.BAD_REQUEST;
    private final HttpStatus forbidden = HttpStatus.FORBIDDEN;
    private final HttpStatus internalServerError = HttpStatus.INTERNAL_SERVER_ERROR;

    /**
     * Handles custom application exceptions:
     * <ul>
     *   <li>If annotated with {@link ResponseStatus} → uses that status</li>
     *   <li>Otherwise → 500</li>
     * </ul>
     */
    @ExceptionHandler(MoveoHAException.class)
    public ResponseEntity<ExceptionDTO> handleMoveoException(
            MoveoHAException e,
            HandlerMethod method,
            HttpServletRequest request
    ) {
        if (!e.getClass().isAnnotationPresent(ResponseStatus.class)) {
            val dto500 = buildExceptionDTO(e, method, request, internalServerError);
            log.error("[500] {} {} @ {}.{} -> {}", request.getMethod(), request.getRequestURI(),
                    dto500.getController(), dto500.getControllerMethod(), e.getMessage());
            return ResponseEntity.internalServerError().body(dto500);
        }
        val status = extractHttpStatus(e);
        val dto = buildExceptionDTO(e, method, request, status);
        log.warn("[{}] {} {} @ {}.{} -> {}", status.value(), request.getMethod(), request.getRequestURI(),
                dto.getController(), dto.getControllerMethod(), e.getMessage());
        return new ResponseEntity<>(dto, status);
    }

    /**
     * Maps SQL integrity violations (unique/FK/etc.) to 400 BAD_REQUEST.
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public ResponseEntity<ExceptionDTO> handleSqlIntegrity(
            SQLIntegrityConstraintViolationException e,
            HandlerMethod method,
            HttpServletRequest request
    ) {
        val dto = buildExceptionDTO(e, method, request, badRequest);
        log.warn("[400] {} {} @ {}.{} -> {}", request.getMethod(), request.getRequestURI(),
                dto.getController(), dto.getControllerMethod(), e.getMessage());
        return ResponseEntity.badRequest().body(dto);
    }

    /**
     * Handles bean validation errors for request body (@Valid) with detailed field errors → 400.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HandlerMethod method,
            HttpServletRequest request
    ) {
        log.warn("Validation failed at {}.{} path={} errors={}",
                method.getMethod().getDeclaringClass().getSimpleName(),
                method.getMethod().getName(),
                request.getRequestURI(),
                e.getBindingResult()
        );

        var body = new LinkedHashMap<String, Object>();
        body.put("status", 400);
        body.put("error", "Bad Request");
        body.put("message", "Validation failed");
        body.put("path", request.getRequestURI());
        body.put("timestamp", java.time.OffsetDateTime.now().toString());
        body.put("errorCode", "PROJECT_VALIDATION_FAILED");

        var errors = new ArrayList<Map<String, String>>();
        e.getBindingResult().getFieldErrors().forEach(err -> {
            var item = new LinkedHashMap<String, String>();
            item.put("field", err.getField());
            item.put("message", err.getDefaultMessage());
            errors.add(item);
        });
        body.put("errors", errors);

        return ResponseEntity.badRequest().body(body);
    }


    /**
     * Access control failures → 403 FORBIDDEN.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ExceptionDTO> handleAccessDenied(
            AccessDeniedException e,
            HandlerMethod method,
            HttpServletRequest request
    ) {
        val dto = buildExceptionDTO(e, method, request, forbidden);
        log.warn("[403] {} {} @ {}.{} -> {}", request.getMethod(), request.getRequestURI(),
                dto.getController(), dto.getControllerMethod(), e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(dto);
    }

    /**
     * Parameter type mismatches in path/query → 400 BAD_REQUEST.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionDTO> handleTypeMismatch(
            MethodArgumentTypeMismatchException e,
            HandlerMethod method,
            HttpServletRequest request
    ) {
        val dto = buildExceptionDTO(e, method, request, badRequest);
        log.warn("[400] {} {} @ {}.{} -> {}", request.getMethod(), request.getRequestURI(),
                dto.getController(), dto.getControllerMethod(), e.getMessage());
        return ResponseEntity.badRequest().body(dto);
    }

    /**
     * Illegal arguments from controller methods → 400 BAD_REQUEST.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ExceptionDTO> handleIllegalArgument(
            IllegalArgumentException e,
            HandlerMethod method,
            HttpServletRequest request
    ) {
        val dto = buildExceptionDTO(e, method, request, badRequest);
        log.warn("[400] {} {} @ {}.{} -> {}", request.getMethod(), request.getRequestURI(),
                dto.getController(), dto.getControllerMethod(), e.getMessage());
        return ResponseEntity.badRequest().body(dto);
    }

    /**
     * Fallback handler: any other unhandled exception → 500 INTERNAL_SERVER_ERROR.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<InternalServerExceptionDTO> handleGeneric(
            Exception e,
            HandlerMethod method,
            HttpServletRequest request
    ) {
        val dto = buildInternal(e, method, request);
        log.error("[500] {} {} @ {}.{} -> {} ({})",
                request.getMethod(), request.getRequestURI(),
                dto.getController(), dto.getControllerMethod(),
                e.getMessage(), e.getClass().getSimpleName(), e);
        dto.setException(e.getClass().getSimpleName());
        return ResponseEntity.internalServerError().body(dto);
    }

    /**
     * Handle PG-ENUM cast failure → return 500 with user-friendly message.
     * <p>
     * This appears when PostgreSQL column type = task_status ENUM
     * but Hibernate binds value as VARCHAR.
     * <p>
     * Client gets a short clear message, SQL details kept in logs only.
     */
    @ExceptionHandler(InvalidDataAccessResourceUsageException.class)
    public ResponseEntity<ExceptionDTO> handleInvalidDataAccess(
            InvalidDataAccessResourceUsageException e,
            HandlerMethod method,
            HttpServletRequest request
    ) {
        String msg = "Server misconfiguration: enum mapping for Task.status is invalid. Please contact support.";

        if (e.getMessage() != null &&
                e.getMessage().contains("is of type task_status but expression is of type character varying")) {
            msg = "Internal server error: task status mapping is misconfigured. " +
                    "The server tried to save a status value as text instead of the database enum.";
        }

        var dto = buildExceptionDTO(new RuntimeException(msg), method, request, HttpStatus.INTERNAL_SERVER_ERROR);
        log.error("[500] {} {} @ {}.{} -> {}", request.getMethod(), request.getRequestURI(),
                dto.getController(), dto.getControllerMethod(), msg, e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto);
    }

    /**
     * Handle any other DB integrity violations (unique, FK, CHECK, etc.) → 400.
     * <p>
     * Client receives a neutral BAD_REQUEST description
     * without exposing DB/SQL internals.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ExceptionDTO> handleDataIntegrityViolation(
            DataIntegrityViolationException e,
            HandlerMethod method,
            HttpServletRequest request
    ) {
        String userMsg = "Bad request: one of the fields violates database constraints.";
        var dto = buildExceptionDTO(new IllegalArgumentException(userMsg), method, request, HttpStatus.BAD_REQUEST);
        log.warn("[400] {} {} @ {}.{} -> {} ({})", request.getMethod(), request.getRequestURI(),
                dto.getController(), dto.getControllerMethod(), userMsg, e.getMessage());
        return ResponseEntity.badRequest().body(dto);
    }

    /**
     * Handle JSON parse errors (e.g. invalid enum values) → 400 BAD_REQUEST.
     * <p>
     * Typical example:
     * "Cannot deserialize value of type TaskStatus from String 'TODOasdasd'"
     * The client gets a user-friendly message instead of raw Jackson trace.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ExceptionDTO> handleJsonParseError(
            HttpMessageNotReadableException e,
            HandlerMethod method,
            HttpServletRequest request
    ) {
        String message = "Invalid request payload.";

        if (e.getMessage() != null &&
                e.getMessage().contains("Cannot deserialize value of type") &&
                e.getMessage().contains("TaskStatus")) {
            message = "Invalid task status. Accepted values: TODO, IN_PROGRESS, DONE.";
        }

        var dto = buildExceptionDTO(
                new IllegalArgumentException(message),
                method,
                request,
                HttpStatus.BAD_REQUEST
        );

        log.warn("[400] {} {} @ {}.{} -> {}", request.getMethod(), request.getRequestURI(),
                dto.getController(), dto.getControllerMethod(), message);
        return ResponseEntity.badRequest().body(dto);
    }


    /**
     * Extract {@link HttpStatus} from {@link ResponseStatus} annotation that is present on custom exceptions.
     */
    private HttpStatus extractHttpStatus(Exception e) {
        val annotation = e.getClass().getAnnotation(ResponseStatus.class);
        return annotation.value();
    }

    /**
     * Builds a uniform {@link ExceptionDTO} for 4xx responses.
     */
    private ExceptionDTO buildExceptionDTO(
            Exception e,
            HandlerMethod method,
            HttpServletRequest request,
            HttpStatus status
    ) {
        val controller = method.getMethod().getDeclaringClass().getSimpleName();
        val methodName = method.getMethod().getName();
        val httpMethod = request.getMethod();
        val path = request.getRequestURI();
        return ExceptionDTO.builder()
                .controller(controller)
                .controllerMethod(methodName)
                .method(httpMethod)
                .path(path)
                .message(e.getMessage())
                .status(status.value())
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    /**
     * Builds a uniform {@link InternalServerExceptionDTO} for 5xx responses.
     */
    private InternalServerExceptionDTO buildInternal(
            Exception e,
            HandlerMethod method,
            HttpServletRequest request
    ) {
        val controller = method.getMethod().getDeclaringClass().getSimpleName();
        val methodName = method.getMethod().getName();
        val httpMethod = request.getMethod();
        val path = request.getRequestURI();
        return new InternalServerExceptionDTO(
                controller,
                methodName,
                httpMethod,
                path,
                e.getMessage(),
                LocalDateTime.now().toString(),
                e.getClass().getSimpleName()
        );
    }
}
