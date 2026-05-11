package com.indux.modules.ppu.application.dtos.requests;

import com.indux.modules.ppu.domain.entities.rdo.RDOStatusDP;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

public record RDOFilter(
        String ppuId, String plataforma, Long idSequencial,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
        String supervisor, String supervisorNome, String cliente, String regional,
        String competencia, RDOStatusDP statusDP, RDOStatusOP statusOP, String contractProjectName,
        String colaborador, List<Integer> contratos, List<Long> organograma, List<Long> projeto) {

    public RDOFilter withProject(List<Long> project) {
        return new RDOFilter(
                ppuId,
                plataforma,
                idSequencial,
                data,
                dataInicio,
                dataFim,
                supervisor,
                supervisorNome,
                cliente,
                regional,
                competencia,
                statusDP,
                statusOP,
                contractProjectName,
                colaborador,
                contratos,
                organograma,
                project);
    };

    public static RDOFilter ofProject(List<Long> projeto) {
        return new RDOFilter(
                null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null, null, projeto);
    }
}
