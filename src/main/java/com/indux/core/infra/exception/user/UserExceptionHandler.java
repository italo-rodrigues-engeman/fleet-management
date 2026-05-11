package com.indux.core.infra.exception.user;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.core.infra.exception.NotFoundBankDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserExceptionHandler {
    @ExceptionHandler(UserBadCredentials.class)
    public ResponseEntity<?> threatBadCredentials(UserBadCredentials exception) {
        GenericMessage dto = new GenericMessage(exception.getMessage(), 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }

    @ExceptionHandler(NotFoundEmployee.class)
    public ResponseEntity<?> threatNotFoundEmployee(NotFoundEmployee exception) {
        GenericMessage dto = new GenericMessage(exception.getMessage(), 404);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(dto);

    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<?> threatUserAlreadyExists(UserAlreadyExistsException exception) {
        GenericMessage dto = new GenericMessage(exception.getMessage(), 409);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(dto);
    }

    @ExceptionHandler(ValidatePasswordFailure.class)
    public ResponseEntity<?> threatValidPasswordFailure(ValidatePasswordFailure exception) {
        GenericMessage dto = new GenericMessage(exception.getMessage(), 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);

    }

    @ExceptionHandler(TokenEventFailure.class)
    public ResponseEntity<?> threatForgetPasswordFailure(TokenEventFailure exception) {
        GenericMessage dto = new GenericMessage(exception.getMessage(), 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);

    }

    @ExceptionHandler(UserDisabledException.class)
    public ResponseEntity<?> threatForgetPasswordFailure(UserDisabledException exception) {
        GenericMessage dto = new GenericMessage(exception.getMessage(), 401);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(dto);

    }

    @ExceptionHandler(NotFoundBankDetails.class)
    public ResponseEntity<?> threatNotFoundBank(NotFoundBankDetails exception) {
        GenericMessage dto = new GenericMessage(exception.getMessage(), 404);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(dto);
    }
}
