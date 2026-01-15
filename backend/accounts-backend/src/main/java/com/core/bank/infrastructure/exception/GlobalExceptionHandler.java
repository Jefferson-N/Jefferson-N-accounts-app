package com.core.bank.infrastructure.exception;

import com.core.bank.model.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.OffsetDateTime;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        log.warn("Resource not found: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse()
                .code(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException ex, WebRequest request) {
        
        log.warn("Resource already exists: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse()
                .code(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> handleBusinessRuleException(
            BusinessRuleException ex, WebRequest request) {
        
        log.warn("Business rule violation: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse()
                .code(HttpStatus.CONFLICT.value())
                .message(ex.getMessage())
                .timestamp(OffsetDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.warn("Validation failed: {}", ex.getMessage());
        
        String errorMessage = "Datos inválidos. Verifique los campos del formulario.";
        
        if (ex.getBindingResult().hasFieldErrors()) {
            var fieldError = ex.getBindingResult().getFieldError();
            if (fieldError != null) {
                String defaultMessage = fieldError.getDefaultMessage();
                errorMessage = defaultMessage;
            }
        }
        
        ErrorResponse errorResponse = new ErrorResponse()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(errorMessage)
                .timestamp(OffsetDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
            Exception ex, WebRequest request) {
        
        log.error("Unhandled exception occurred", ex);
        
        ErrorResponse errorResponse = new ErrorResponse()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Error procesando la solicitud. Por favor contacte con el administrador del sistema.")
                .timestamp(OffsetDateTime.now());
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
