package com.indux.modules.ppu.infra.exceptions;

import com.indux.core.application.dto.generic.GenericMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class PPUExceptionsHandler {

    @ExceptionHandler(PPUAlreadyExistsException.class)
    public ResponseEntity<?> threatAlreadyExists(PPUAlreadyExistsException exception) {
        GenericMessage dto = new GenericMessage(exception.getMessage(), HttpStatus.CONFLICT.value());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(dto);

    }

    @ExceptionHandler(MioFailure.class)
    public ResponseEntity<?> threatMioFailure(MioFailure exception) {
        var dto = new MioFailureMessage().fromMioResponse(exception.getMessage(), exception.getPosition(), exception.getMioResponse());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);

    }

}
