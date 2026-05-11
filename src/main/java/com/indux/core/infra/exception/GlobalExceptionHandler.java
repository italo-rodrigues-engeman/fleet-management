package com.indux.core.infra.exception;


import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.infra.exception.module.ForbiddenModuleAccessException;
import com.indux.core.infra.filestorage.exception.StorageException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ForbiddenModuleAccessException.class)
    public ResponseEntity<GenericMessage> handleForbiddenModuleAccess(ForbiddenModuleAccessException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new GenericMessage(ex.getMessage(), 403));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GenericMessage> handleMalformedJson(HttpMessageNotReadableException ex) {
        String detail = Optional.ofNullable(ex.getMostSpecificCause())
                .map(Throwable::getMessage)
                .orElse(ex.getMessage());
        return ResponseEntity
                .badRequest()
                .body(new GenericMessage("Corpo de requisição inválido: " + detail, 400));
    }


    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<GenericMessage> handleInvalidFormat(InvalidFormatException ex) {
        String path = ex.getPath().stream()
                .map(ref -> ref.getFieldName())
                .collect(Collectors.joining("."));
        String detail = String.format("Campo '%s' recebeu valor '%s' com tipo inválido", path, ex.getValue());
        return ResponseEntity
                .badRequest()
                .body(new GenericMessage(detail, 400));
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<GenericMessage> handleStorageException(StorageException ex) {
        return ResponseEntity
                .internalServerError()
                .body(new GenericMessage(ex.getMessage(), 500));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existingValue, newValue) -> existingValue));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("message", "Falha na validação dos dados");
        response.put("status", 400);
        response.put("errors", errors);

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<GenericMessage> handleMissingParams(MissingServletRequestParameterException ex) {
        String detail = ex.getParameterName() + " é obrigatório";
        return ResponseEntity
                .badRequest()
                .body(new GenericMessage(detail, 400));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<GenericMessage> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detail = String.format("Parâmetro '%s' tipo inválido. Esperado: %s",
                ex.getName(),
                ex.getRequiredType().getSimpleName());
        return ResponseEntity
                .badRequest()
                .body(new GenericMessage(detail, 400));
    }



    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<GenericMessage> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        String detail = "Método '" + ex.getMethod() + "' não é permitido nesta URL";
        HttpHeaders headers = new HttpHeaders();
        headers.setAllow(ex.getSupportedHttpMethods());
        return new ResponseEntity<>(
                new GenericMessage(detail, 405),
                headers,
                HttpStatus.METHOD_NOT_ALLOWED
        );
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<GenericMessage> handleBindException(BindException ex) {
        String details = ex.getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity
                .badRequest()
                .body(new GenericMessage("Erro de binding de parâmetros: " + details, 400));
    }

}