package com.indux.modules.flash_fuel.domain.dtos;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record FlashFuelFilter(
        List<String> status,
        List<String> situacoes,
        List<Integer> etapasAtuais,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
        LocalDateTime criadoDe,
        LocalDateTime criadoAte,
        Integer id,
        String usuarioLogadoID,
        List<Integer> regionaisId,
        List<String> regionais,
        List<Integer> projetcs,
        //--------do modulo------//
        String nomeRequerente,
        String nomeSolicitante, //quem abriu a solicitação
        String matriculaRequerente,
        String valor,
        String avisoRecebimento
) {
    public FlashFuelFilter withRegionaisAndProjetcs(List<Integer> regionaisId, List<Integer> projetcs, List<Integer> currentStep) {
        return new FlashFuelFilter(
                status,
                situacoes,
                List.copyOf(currentStep),
                data,
                dataInicio,
                dataFim,
                criadoDe,
                criadoAte,
                id,
                usuarioLogadoID,
                List.copyOf(regionaisId),
                regionais,
                List.copyOf(projetcs),
                nomeRequerente,
                nomeSolicitante,
                matriculaRequerente,
                valor,
                avisoRecebimento
        );
    }


}
