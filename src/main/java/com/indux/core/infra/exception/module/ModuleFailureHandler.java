package com.indux.core.infra.exception.module;

import com.indux.core.application.dto.generic.GenericMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ModuleFailureHandler {
    @ExceptionHandler(ModuleNotFoundFailure.class)
    public ResponseEntity<?> threatModuleNotFound(ModuleNotFoundFailure exception) {
        GenericMessage dto = new GenericMessage(exception.getMessage(), 404);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(dto);
    }

    @ExceptionHandler(ModuleFailure.class)
    public ResponseEntity<GenericMessage> threatFailure(ModuleFailure exception) {
        GenericMessage dto = new GenericMessage(exception.getMessage(), 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(dto);
    }

    @ExceptionHandler(ModuleBadRequest.class)
    public ResponseEntity<GenericMessage> threatBadRequest(ModuleBadRequest exception) {
        GenericMessage dto = new GenericMessage(exception.getMessage(), 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }

}
