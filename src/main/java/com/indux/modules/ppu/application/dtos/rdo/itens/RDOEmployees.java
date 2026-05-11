package com.indux.modules.ppu.application.dtos.rdo.itens;

import com.indux.modules.ppu.domain.entities.item.ShiftSchedule;
import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

public record  RDOEmployees(
        @NotNull(message = "A matricula do colaborador deve ser preenchida.", groups = RDOCreate.class)
        String matricula,
        @NotNull(message = "O nome do colaborador deve ser preenchido.", groups = RDOCreate.class)
        String nome,
        String cargoID,
        @NotNull(message = "O nome do cargo do colaborador deve ser preenchido.", groups = RDOCreate.class)
        String cargoNome,
        String sispat,
        ShiftSchedule horario,
        String tipoDia,
        @Nullable LocalTime horaChegadaVoo,
        Boolean sinaleiro,
        Boolean caboTurma,
        Boolean horaExtra,
        Boolean emDobra,
        List<EmployeeOvertime> horasExtras,
        Duration adicionalNoturno,
        Duration horasNormais,
        Duration horasTotais,
        Duration horasExtrasTotais,
        String statusColaborador,
        LocalTime horaSaidaVoo,
        @Nullable String tipoDisposicao
) {
}

