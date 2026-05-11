package com.indux.modules.ppu.domain.entities.item;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShiftSchedule {
    private String turno;
    private LocalTime horarioInicial;
    private LocalTime horarioInicialAlmoco;
    private LocalTime horarioFinalAlmoco;
    private LocalTime horarioFinal;
    private Boolean horarioNoturnoEstendido;
}
