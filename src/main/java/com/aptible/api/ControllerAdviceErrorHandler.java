package com.aptible.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
@Slf4j
class ControllerAdviceErrorHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception exception) {
       log.error("error", exception);
       return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
               .body(new Models.ErrorMessage(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ApiResponse> handleException(AuthorizationDeniedException exception) {
        log.warn("warn", exception);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new Models.ErrorMessage(exception.getMessage(), HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(io.jsonwebtoken.ExpiredJwtException.class)
    public ResponseEntity<ApiResponse> handleException(io.jsonwebtoken.ExpiredJwtException exception) {
       log.warn("warn", exception);
       return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
               .body(new Models.ErrorMessage(exception.getMessage(), HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiResponse> handleException(ResponseStatusException exception) {
        log.warn("warn", exception);
        return ResponseEntity.status(exception.getStatusCode())
                .body(new Models.ErrorMessage(exception.getMessage(), HttpStatus.valueOf(exception.getStatusCode().value())));
    }
}
