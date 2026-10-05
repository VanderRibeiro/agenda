package com.brunoribeiro.exception.handler;

import com.brunoribeiro.exception.ApplicationException;
import com.brunoribeiro.exception.dto.ErrorResponseDTO;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(java.util.stream.Collectors.joining(", "));

        return buildResponse(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolationException(
            ConstraintViolationException ex,
            WebRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex,
            WebRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "JSON inválido ou campo com formato incorreto",
                request);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ErrorResponseDTO> handleApplicationException(
            ApplicationException ex,
            WebRequest request) {

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(
                Instant.now(),
                ex.getStatus().value(),
                ex.getMessage(),
                extractPath(request)
        );
        return new ResponseEntity<>(errorResponseDTO, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(
            Exception ex,
            WebRequest request) {

        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno do servidor. Contate o administrador.",
                extractPath(request)
        );

        ex.printStackTrace();

        return new ResponseEntity<>(errorResponseDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponseDTO> buildResponse(
            HttpStatus status,
            String message,
            WebRequest request) {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO(
                Instant.now(),
                status.value(),
                message,
                extractPath(request)
        );
        return new ResponseEntity<>(errorResponseDTO, status);
    }

    private String extractPath(WebRequest request) {
        String description = request.getDescription(false);
        return description != null ? description.replace("uri=", "") : "";
    }
}



