package com.indux.modules.purchase_occurrence.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.application.dto.generic.CompleteEmployeeDTO;
import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.domain.model.modules.form.FileMetadata;
import com.indux.core.domain.model.modules.form.Form;
import com.indux.core.domain.model.modules.form.StepLog;
import com.indux.modules.purchase_occurrence.domain.dto.PurchaseRequest;
import com.mongodb.lang.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Document("purchase_occurrence")
public class PurchaseOccurrence extends Form<PurchaseOccurrence> {
    //    ----- PRIMEIRA ETAPA ---------- //
    @Id
    private String id;
    @JsonProperty("dataVencimento")
    private LocalDate dueDate;
    @JsonProperty("numeroDoPedido")
    private String orderNumber;
    @JsonProperty("numeroNotaFiscal")
    private String invoiceNumber;
    @JsonProperty("solicitante")
    private SimpleUser applicant;
    @JsonProperty("causas")
    private PurchaseOccurrenceCauses causes;
    @JsonProperty("anexos")
    private List<AttachmentEntity> archives;
    @JsonProperty("justificativaSolicitacao")
    private String justificationFirstStep;
    @JsonProperty("comprador")
    private BuyerRecord buyer;
    @JsonProperty("nomeDivergente")
    private String divergenceName;
    @JsonProperty("cnpjDivergente")
    private String divergenceCnpj;
    @JsonProperty("outros")
    private String outros;
    //----------SECOND STEP ----------/
    @JsonProperty("pertinente")
    private Boolean relevant;
    @JsonProperty("solucao")
    private String solution;
    @JsonProperty("respostaSuprimentos")
    private String supplyResponse;
    private FileMetadata supplyAttachment;
//    ---------------

    public static PurchaseOccurrence initialStep(PurchaseRequest dto, Employee user, StepLog log, Long code, List<AttachmentEntity> attachments, SimpleUser applicant, CompleteEmployeeDTO employee) {
        return PurchaseOccurrence.builder()
                .regionalId(
                        employee.getHierarchy() != null && employee.getHierarchy().getRegionalId() != null
                                ? employee.getHierarchy().getRegionalId().intValue()
                                : null
                )
                .regionalNome(
                        employee.getHierarchy() != null && employee.getHierarchy().getRegionalNome() != null
                                ? employee.getHierarchy().getRegionalNome()
                                : ""
                )
                .projectId(
                        employee.getHierarchy() != null && employee.getHierarchy().getProjetoId() != null
                                ? employee.getHierarchy().getProjetoId().intValue()
                                : 0
                )
                .projectName(
                        employee.getHierarchy() != null && employee.getHierarchy().getProjetoNome() != null
                                ? employee.getHierarchy().getProjetoNome()
                                : ""
                )
                .currentStep(2)
                .contratoId(user.getContract_id())
                .created_at(Date.from(Instant.now()))
                .final_date(null)
                .status(DocumentStatus.ABERTO)
                .stepLog(List.of(log))
                .codeID(code)
                .situacao(DocumentStatus.ANDAMENTO)
                .dataLog(Date.from(Instant.now()))
                .dueDate(dto.dataVencimento())
                .orderNumber(dto.numeroDoPedido())
                .invoiceNumber(dto.numeroNotaFiscal())
                .justificationFirstStep(dto.justificativaSolicitacao())
                .archives(attachments)
                .buyer(dto.comprador())
                .causes(dto.causas())
                .applicant(applicant)
                .divergenceCnpj(dto.cnpjDivergente())
                .divergenceName(dto.nomeDivergente())
                .outros(dto.outros())
                .build();

    }

    public static PurchaseOccurrence secondStep(PurchaseOccurrence occurrence, PurchaseRequest record, @Nullable  FileMetadata metadata){
        occurrence.setRelevant(record.pertinente());
        occurrence.setSolution(record.solucao());
        occurrence.setSupplyResponse(record.respostaSuprimentos());
        occurrence.setSupplyAttachment(metadata);
        return occurrence;
    }
}
