package com.indux.modules.mobile.infra.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MobilePayrollNotFoundException extends RuntimeException {

    public MobilePayrollNotFoundException(String message) {
        super(message);
    }
}
