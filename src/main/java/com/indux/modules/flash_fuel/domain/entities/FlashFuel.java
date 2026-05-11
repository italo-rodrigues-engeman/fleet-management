package com.indux.modules.flash_fuel.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.domain.model.modules.form.FileMetadata;
import com.indux.core.domain.model.modules.form.Form;
import com.indux.core.domain.model.modules.form.StepLog;
import com.indux.modules.flash_fuel.domain.dtos.FlashFuelRequest;
import com.indux.modules.flash_fuel.domain.dtos.ValorContratoDTO;
import com.mongodb.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
@Document("flash_combustivel")
public class FlashFuel extends Form<FlashFuel> {
    @Id
    private String id;
    @JsonProperty("colaborador")
    private Boolean isEmployee;
    @JsonProperty("dataSolicitacao")
    private LocalDate date;
    @JsonProperty("solicitante")
    private SimpleUser applicant;
    @JsonProperty("matriculaRequerente")
    @Nullable
    private String registration;
    @JsonProperty("nomeRequerente")
    private String claimantName;
    @JsonProperty("cpfRequerente")
    private String claimantCPF;
    @JsonProperty("gestorFilial")
    private String gestorFilial;
    @JsonProperty("valor")
    private BigDecimal value;
    @JsonProperty("motivo")
    private String motivo;
    @JsonProperty("anexos")
    List<AttachmentEntity> attachments;
    @JsonProperty("valorContrato")
    private List<ValorContratoDTO> valorContrato;
    // -- SecondStep -- //
    @JsonProperty("aprovacaoGestor")
    private Boolean managerApproval;
    // -- ThirdStep -- //
    @JsonProperty("aprovacaoFinanceiro")
    private Boolean financeApproval;
    // -- FourthStep -- ;;
    @JsonProperty("avisoRecebimentoFeito")
    private Boolean hasReceiptNotice;
    @JsonProperty("avisoRecebimento")
    private String receiptNotice;

    @JsonProperty("pagamento")
    private FileMetadata anexo;

    public static FlashFuel fromFirstStep(FlashFuelRequest request, List<AttachmentEntity> attachments, StepLog log, Long code, SimpleUser applicant) {
        return FlashFuel.builder()
                .regionalId(request.regionalId())
                .regionalNome(request.regional())
                .projectId(request.projetoId())
                .projectName(request.projetoNome())
                .currentStep(2)
                .created_at(Date.from(Instant.now()))
                .final_date(null)
                .applicant(applicant)
                .status(DocumentStatus.ABERTO)
                .stepLog(List.of(log))
                .codeID(code)
                .situacao(DocumentStatus.ANDAMENTO)
                .dataLog(Date.from(Instant.now()))
                .attachments(attachments)
                .isEmployee(request.colaborador())
                .date(request.dataSolicitacao())
                .registration(request.matriculaSolicitante())
                .claimantName(request.nomeSolicitante())
                .claimantCPF(request.cpfSolicitante())
                .gestorFilial(request.gestorFilial())
                .value(request.getValorAsBigDecimal())
                .motivo(request.motivo())
                .valorContrato(request.getValorContratoAsList())
                .build();
    }

    public static FlashFuel fromSecondStep(FlashFuel flashFuel, FlashFuelRequest record){
        flashFuel.setManagerApproval(record.aprovacaoGestor());
        return flashFuel;
    }

    public static FlashFuel fromThirdStep(FlashFuel flashFuel, FlashFuelRequest record){
        flashFuel.setFinanceApproval(record.aprovacaoFinanceiro());
        return flashFuel;
    }

    public static FlashFuel fromFourthStep(FlashFuel flashFuel, FileMetadata request){
        flashFuel.setAnexo(request);
        return flashFuel;
    }

    public static FlashFuel fromFifthStep(FlashFuel flashFuel, FlashFuelRequest record){
        flashFuel.setHasReceiptNotice(record.avisoRecebimentoFeito());
        flashFuel.setReceiptNotice(record.avisoRecebimento());
        return flashFuel;
    }

}


