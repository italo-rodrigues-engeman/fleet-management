package com.indux.modules.ppu.infra.exceptions;

import com.indux.modules.ppu.infra.mio.dto.MioResponse;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MioFailure extends RuntimeException{
    String position;
    MioResponse mioResponse;

    public MioFailure(MioResponse mioResponse, String position, String message) {
        super(message);
        this.position = position;
        this.mioResponse = mioResponse;
    }
    public MioFailure(MioResponse mioResponse, String message) {
        super(message);
        this.mioResponse = mioResponse;
    }

    public MioFailure(String message) {
        super(message);
    }
}
