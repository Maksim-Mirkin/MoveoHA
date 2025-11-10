package com.moveo.ha.error;

import com.moveo.ha.dto.error.ExceptionDTO;
import com.moveo.ha.dto.error.InternalServerExceptionDTO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        // 1) Log verbose developer info
        log.warn("Validation failed at {}.{} path={} errors={}",
                method.getMethod().getDeclaringClass().getSimpleName(),
                method.getMethod().getName(),
                request.getRequestURI(),
                e.getBindingResult()
        );

        // 2) Build client-friendly body
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
