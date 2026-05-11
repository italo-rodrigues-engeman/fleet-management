package com.indux.modules.ppu.presentation.dtos;

import com.indux.core.infra.exception.module.ModuleFailure;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

public record MissingRDORequest(
        Long contract,
        Long project,
        List<String> platforms,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
) {

    public void validate() {
        if (startDate != null && startDate.isAfter(this.endDate)) {
            throw new ModuleFailure("Dia inicial deve ser igual ou anterior ao dia final.");
        }
    }

}
