package com.gridline.dtoutage.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateActiveOutageException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateOutage(DuplicateActiveOutageException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body(
                HttpStatus.CONFLICT, ex.getMessage(), Map.of("existingOutageRef", ex.getExistingOutageRef())));
    }

    /**
     * Belt-and-braces: even if application code somehow bypasses the
     * pre-check, the DB's partial unique index (idx_one_active_outage_per_dt)
     * throws this on the INSERT itself. We translate it the same way as the
     * explicit check above, since from the client's point of view it's the
     * same conflict.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause().getMessage();
        if (message != null && message.contains("idx_one_active_outage_per_dt")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    body(HttpStatus.CONFLICT, "This DT already has an active outage.", null));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                body(HttpStatus.CONFLICT, "The request conflicts with existing data.", null));
    }

    @ExceptionHandler(InvalidRestorationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRestoration(InvalidRestorationException ex) {
        return ResponseEntity.badRequest().body(body(HttpStatus.BAD_REQUEST, ex.getMessage(), null));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body(HttpStatus.NOT_FOUND, ex.getMessage(), null));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                body(HttpStatus.FORBIDDEN, "You don't have permission to do that.", null));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
        Map<String, Object> body = body(HttpStatus.BAD_REQUEST, "Validation failed.", null);
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    private Map<String, Object> body(HttpStatus status, String message, Map<String, Object> extra) {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", Instant.now().toString());
        map.put("status", status.value());
        map.put("error", status.getReasonPhrase());
        map.put("message", message);
        if (extra != null) {
            map.putAll(extra);
        }
        return map;
    }
}
