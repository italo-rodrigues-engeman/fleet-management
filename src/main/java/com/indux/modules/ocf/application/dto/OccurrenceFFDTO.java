package com.indux.modules.ocf.application.dto;

import com.indux.core.application.dto.generic.BankDetailDTO;
import com.indux.modules.ocf.domain.model.MeiosComunicacao;
import com.indux.modules.ocf.domain.model.Origem;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public record OccurrenceFFDTO(
        String matriculaReclamante,
        Optional<String> grupoResponsavel,
        String observacao,
        Origem origem,
        Optional<MultipartFile> extratoColaborador,
        Optional<MultipartFile> comprovanteDePagamento,
        String causa,
        Optional<Boolean> bancoDadosErrado,
        BankDetailDTO dadosCorretos,
        LocalDate data_ocorrencia,
        String numeroDoProtocolo,
        String telefone,
        String telefoneDeContato,
        String email,
        String tipoDeBeneficio,
        String motivo,
        String valorContestado,
        String valorPagarDescontar,
        String tipoFluxo,
        Boolean aprovacaoGestor,
        String observacaoAnalista1,
        String motivoAnalista,
        Boolean aprovacaoAnalista1,
        String prioridade,
        String tipoAtendimento,
        List<ItemReclamadoDTO> itemsreclamados,
        // Novos campos adicionados
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> dataAtendimento,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Optional<LocalDateTime> dataFim,
        Optional<String> atendenteRHlocal,
        Optional<String> atendenteRHmatriz,
        Optional<String> competencia,
        Optional<String> conversaChat,
        Optional<String> descricaoOcorrencia,
        Optional<String> respostaEmpregado,
        Optional<String> justificativaOcorrencia,
        Optional<MultipartFile> anexoTicket,
        Optional<List<String>> canais,
        Optional<MeiosComunicacao> meiosComunicacao,
        Optional<Boolean> pertinente,
        Optional<String> temperatura
) {
    public static OccurrenceFFDTO forAlert(String registration, String observation, String type, Optional<String> competence) {
        Optional<String> description = Optional.of("Divergencia encontrada na competencia para o funcionario " + observation);
        Optional<MeiosComunicacao> comunicacao = Optional.of(new MeiosComunicacao());
        return new OccurrenceFFDTO(
                registration,
                null,
                observation,
                Origem.SITE,
                Optional.empty(),
                null,
                null,
                Optional.of(false),
                null,
                LocalDate.now(),
                null, null, null, null, null, null, null, null,
                "Divergência de Competência",
                null, null, null, null, null,
                type,
                null,
                Optional.of(LocalDateTime.now()),
                Optional.of(LocalDateTime.now()),
                Optional.of(""),
                Optional.of(""),
                competence,
                Optional.of(observation),
                description,
                Optional.of(""),
                Optional.of(""),
                Optional.empty(),
                null,
                comunicacao,
                Optional.of(false),
                Optional.of("")
        );
    }

    @Override
    public String toString() {
        return "OccurrenceFFDTO{" +
                "matriculaReclamante='" + matriculaReclamante + '\'' +
                ", grupoResponsavel=" + grupoResponsavel +
                ", observacao='" + observacao + '\'' +
                ", origem=" + origem +
                ", extratoColaborador=" + (extratoColaborador.isPresent() ? extratoColaborador.get().getOriginalFilename() : "null") +
                ", comprovanteDePagamento=" + (comprovanteDePagamento.isPresent() ? comprovanteDePagamento.get().getOriginalFilename() : "null") +
                ", causa=" + causa +
                ", bancoDadosErrado=" + bancoDadosErrado +
                ", dadosCorretos=" + dadosCorretos +
                ", data_ocorrencia=" + data_ocorrencia +
                ", numeroDoProtocolo='" + numeroDoProtocolo + '\'' +
                ", telefone='" + telefone + '\'' +
                ", email='" + email + '\'' +
                ", tipoDeBeneficio='" + tipoDeBeneficio + '\'' +
                ", motivo='" + motivo + '\'' +
                ", valorContestado='" + valorContestado + '\'' +
                ", valorPagarDescontar='" + valorPagarDescontar + '\'' +
                ", tipoFluxo='" + tipoFluxo + '\'' +
                ", aprovacaoGestor=" + aprovacaoGestor +
                ", observacaoAnalista1='" + observacaoAnalista1 + '\'' +
                ", motivoAnalista='" + motivoAnalista + '\'' +
                ", aprovacaoAnalista1=" + aprovacaoAnalista1 +
                ", prioridade='" + prioridade + '\'' +
                ", tipoAtendimento='" + tipoAtendimento + '\'' +
                ", itemsreclamados=" + itemsreclamados +
                ", dataAtendimento=" + dataAtendimento +
                ", dataFim=" + dataFim +
                ", atendenteRHlocal=" + atendenteRHlocal +
                ", atendenteRHmatriz=" + atendenteRHmatriz +
                ", competencia=" + competencia +
                ", conversaChat=" + conversaChat +
                ", descricaoOcorrencia=" + descricaoOcorrencia +
                ", respostaEmpregado=" + respostaEmpregado +
                ", justificativaOcorrencia=" + justificativaOcorrencia +
                ", anexoTicket=" + (anexoTicket.isPresent() ? anexoTicket.get().getOriginalFilename() : "null") +
                ", canais=" + canais +
                ", meiosComunicacao=" + meiosComunicacao +
                ", pertinente=" + pertinente +
                ", temperatura=" + temperatura +
                '}';
    }
}

