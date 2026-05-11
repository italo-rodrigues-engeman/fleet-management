package com.indux.modules.ocf.application.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record OccurrenceExportDTO(
        String id,
        String numeroProtocolo,
        String origem,
        LocalDate dataOcorrencia,
        String colaboradorMatricula,
        String colaboradorNome,
        String colaboradorSetor,
        String solicitanteNome,
        String solicitanteEmail,
        String descricao,
        String causa,
        String status,
        String situacao,
        String prioridade,
        String tipoAtendimento,
        String tipoFluxo,
        String valorContestado,
        String valorPagarDescontar,
        String telefone,
        String tipoBeneficio,
        String motivo,
        LocalDateTime dataAtendimento,
        LocalDateTime dataFim,
        String atendenteRHlocal,
        String atendenteRHmatriz,
        String competencia,
        String descricaoOcorrencia,
        String justificativaOcorrencia,
        Boolean aprovacaoGestor,
        Boolean aprovacaoAnalista,
        Boolean aprovacaoAnalista1,
        String observacaoGestor,
        String observacaoAnalista,
        String observacaoAnalista1,
        String motivoAnalista,
        Boolean pertinente
) {
}


