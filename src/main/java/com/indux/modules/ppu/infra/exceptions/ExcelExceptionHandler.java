package com.indux.modules.ppu.infra.exceptions;

import com.indux.core.application.dto.generic.GenericMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExcelExceptionHandler {

    @ExceptionHandler(InvalidExcelFileException.class)
    public ResponseEntity<?> threatInvalidFile(InvalidExcelFileException exception) {
        GenericMessage dto = new GenericMessage(exception.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);

    }

    @ExceptionHandler(MissingHeaderException.class)
    public ResponseEntity<?> threatMissingHeader(MissingHeaderException exception) {
        GenericMessage dto = new GenericMessage(exception.getMessage(), HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);

    }

}
