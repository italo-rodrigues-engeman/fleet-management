package com.indux.modules.ppu.infra.mio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

//timesheet
@Builder
public record MioResponse(
        @JsonProperty("Matrícula")
        String matricula,
        @JsonProperty("Nome Completo")
        String nome,
        @JsonProperty("Evento")
        String status, //status
        LocalDate dataInicio,
        LocalDate dataFim,
        String ordem
) {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    public static MioResponse fromJson(Map<String, Object> json) {
        String matricula = (String) json.getOrDefault("Matrícula", null);
        String nome = (String) json.getOrDefault("Nome Completo", null);
        String status = (String) json.getOrDefault("Evento", null);
        String dataInicioStr = (String) json.getOrDefault("Data Início", null);
        String dataFimStr = (String) json.getOrDefault("Data Fim", null);
        String ordem = (String) json.getOrDefault("Ordem", null);

        LocalDate dataInicio = parseIsoDate(dataInicioStr).orElse(null);
        LocalDate dataFim = parseIsoDate(dataFimStr).orElse(null);

        return new MioResponse(matricula, nome, status, dataInicio, dataFim, ordem);
    }

    private static Optional<LocalDate> parseIsoDate(String raw) {
        if (raw == null || raw.isBlank()) return Optional.empty();
        try {
            return Optional.of(LocalDate.parse(raw.trim(), ISO));
        } catch (Exception e) {
            return Optional.empty();
        }
    }


    /**
     * Retorna a prioridade do evento. Menor número = maior prioridade.
     * 1 = Dobra
     * 2 = Folga Indenizada
     * 3 = Turma de Treinamento
     * 4+ = Outros eventos
     */
    public int getEventPriority() {
        if (this.status == null) {
            return Integer.MAX_VALUE;
        }
        String eventoUpper = this.status.toUpperCase().trim();

        if (eventoUpper.contains("DOBRA")) {
            return 1;
        } else if (eventoUpper.contains("FOLGA INDENIZADA")) {
            return 2;
        } else if (eventoUpper.contains("TURMA DE TREINAMENTO")) {
            return 3;
        }

        return 4;
    }
}
