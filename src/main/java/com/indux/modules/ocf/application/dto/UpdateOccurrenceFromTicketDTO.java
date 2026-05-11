package com.indux.modules.ocf.application.dto;

import com.indux.modules.ocf.domain.model.MeiosComunicacao;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public record UpdateOccurrenceFromTicketDTO(
        String occurrenceId, // ID da ocorrência a ser atualizada
        Optional<String> descricao,
        Optional<String> descricaoOcorrencia,
        Optional<String> respostaEmpregado,
        Optional<String> justificativaOcorrencia,
        Optional<String> causas,
        Optional<String> prioridade,
        Optional<String> tipoFluxo,
        Optional<String> tipoAtendimento,
        Optional<String> motivo,
        Optional<String> tipoBeneficio,
        Optional<String> competencia,
        Optional<String> conversaChat,
        Optional<String> pertinente,
        Optional<String> atendenteRHMatriz,
        Optional<String> atendenteRHlocal,
        Optional<String> emailPessoal,
        Optional<String> emailComercial,
        Optional<String> telefone2,
        Optional<MultipartFile> extratoColaborador,
        Optional<MultipartFile> comprovanteDePagamento,
        Optional<MultipartFile> anexoTicket,
        List<ItemReclamadoDTO> itemsreclamados,
        Optional<List<String>> canais,
        Optional<MeiosComunicacao> meiosComunicacao,
        Optional<String> temperatura
) {
    public static UpdateOccurrenceFromTicketDTO fromOccurrenceFFDTO(OccurrenceFFDTO dto, String occurrenceId) {
        return new UpdateOccurrenceFromTicketDTO(
                occurrenceId,
                Optional.ofNullable(dto.observacao()), // descricao
                dto.descricaoOcorrencia(),
                dto.respostaEmpregado(),
                dto.justificativaOcorrencia(),
                Optional.ofNullable(dto.causa()), // causas
                Optional.ofNullable(dto.prioridade()),
                Optional.ofNullable(dto.tipoFluxo()),
                Optional.ofNullable(dto.tipoAtendimento()),
                Optional.ofNullable(dto.motivo()),
                Optional.ofNullable(dto.tipoDeBeneficio()),
                dto.competencia(),
                dto.conversaChat(),
                dto.pertinente().map(Object::toString), // pertinente - convertido para String
                dto.atendenteRHmatriz(),
                dto.atendenteRHlocal(),
                Optional.empty(), // emailPessoal - não existe no OccurrenceFFDTO
                Optional.ofNullable(dto.email()), // emailComercial
                Optional.empty(), // telefone2 - não existe no OccurrenceFFDTO, usar telefone se necessário
                dto.extratoColaborador(),
                dto.comprovanteDePagamento(),
                dto.anexoTicket(),
                dto.itemsreclamados(),
                Optional.empty(), // canais - não existe no OccurrenceFFDTO
                dto.meiosComunicacao(),
                Optional.empty() // temperatura - não existe no OccurrenceFFDTO
        );
    }
} 