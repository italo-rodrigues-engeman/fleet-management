package com.indux.modules.ppu.application.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShiftScheduleResponse {
    private String turno;
    private LocalTime horarioInicial;
    private LocalTime horarioInicialAlmoco;
    private LocalTime horarioFinalAlmoco;
    private LocalTime horarioFinal;
    private Boolean horarioNoturnoEstendido;
}

