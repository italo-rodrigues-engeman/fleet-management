package com.indux.core.infra.exception;

import com.indux.core.application.dto.generic.GenericMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomAdviceHTTPReturn {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> threatIllegalArgumentException(IllegalArgumentException exception) {
        GenericMessage dto = new GenericMessage(exception.getMessage(), 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto);
    }

}