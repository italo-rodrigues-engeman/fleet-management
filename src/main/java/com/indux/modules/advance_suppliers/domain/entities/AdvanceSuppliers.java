package com.indux.modules.advance_suppliers.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.domain.model.employee.BankDetails;
import com.indux.core.domain.model.employee.Employee;
import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.DocumentStatus;
import com.indux.core.domain.model.modules.form.Form;
import com.indux.core.domain.model.modules.form.StepLog;
import com.indux.modules.advance_suppliers.domain.dto.AdvanceSuppliersRequest;
import com.mongodb.lang.Nullable;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
@Document("adiantamento_de_fornecedores")
@CompoundIndexes({
    @CompoundIndex(name = "etapa_status_payment_idx", def = "{'etapa_atual': 1, 'status': 1, 'paymentDate': 1}"),
    @CompoundIndex(name = "etapa_status_invoice_idx", def = "{'etapa_atual': 1, 'status': 1, 'invoiceDate': 1}"),
    @CompoundIndex(name = "etapa_status_datalog_idx", def = "{'etapa_atual': 1, 'status': 1, 'data_log': 1}")
})
public class AdvanceSuppliers extends Form<AdvanceSuppliers> {

    @Id
    private String id;
    @JsonProperty("solicitante")
    private SimpleUser applicant;

    // first step //
    @JsonProperty("temCnpj")
    private Boolean isCnpj;
    @JsonProperty("tipo_de_envio")
    private BankType type;
    @JsonProperty("cnpj")
    @Nullable
    private String cnpj;
    @JsonProperty("cpf")
    @Nullable
    private String cpf;
    @JsonProperty("dadosBancarios")
    private BankDetails dadosBancarios;
    @JsonProperty("chavePix")
    @Nullable
    private String pixKey;
    @JsonProperty("dataPagamento")
    private LocalDate paymentDate;
    @JsonProperty("dataPrevistaNotaFiscal")
    private LocalDate invoiceDate;
    @JsonProperty("origem")
    private OriginRequest origin;
    @JsonProperty("valor")
    private BigDecimal value;
    @JsonProperty("nomeFornecedor")
    private String supplierName;
    @JsonProperty("anexos")
    private List<AttachmentEntity> attachments;
    @JsonProperty("motivo")
    private String reason;
    @JsonProperty("gestorNome")
    private String managerName;
    @JsonProperty("gestorEmail")
    private String managerEmail;
    @JsonProperty("contrato")
    private String contractName;
    @JsonProperty("numeroContrato")
    @Nullable
    private String contractNumber;
    @JsonProperty("numeroPedido")
    @Nullable
    private String orderNumber;
    @JsonProperty("pagamentoIntegral")
    @Nullable
    private Boolean integralPayment;
    @JsonProperty("valorTeto")
    @Nullable
    private BigDecimal ceilingValue;

    //---second step---//
    @JsonProperty("aprovacaoGestor")
    private Boolean managerApprove;

    //--third step --//
    @JsonProperty("aprovacaoFinanceiro")
    private Boolean financeApprove;
    @JsonProperty("comprovante_pagamento")
    private AttachmentEntity proofOfPayment;

    //--fourth step --//
    @JsonProperty("nota_fiscal")
    private AttachmentEntity invoiceDocument;
    @JsonProperty("avisoRecebimentoFeito")
    private String receiptNotice;



    public static AdvanceSuppliers fromFirstStep(
            AdvanceSuppliersRequest request,
            List<AttachmentEntity> attachments,
            Employee user,
            StepLog log,
            Long code,
            SimpleUser applicant
    ) {
        return AdvanceSuppliers.builder()
                .regionalNome(request.regional())
                .regionalId(request.idRegional())
                .projectId(request.idProjeto())
                .projectName(request.projetoNome())
                .currentStep(request.origem() == OriginRequest.ORDER ? 3:2)
                .contractName(request.contrato())
                .created_at(new Date())
                .status(DocumentStatus.ABERTO)
                .stepLog(List.of(log))
                .codeID(code)
                .situacao(DocumentStatus.ANDAMENTO)
                .dataLog(new Date())
                .applicant(applicant)
                .isCnpj(request.temCnpj())
                .type(request.tipo_de_envio())
                .cnpj(request.cnpj())
                .cpf(request.cpf())
                .dadosBancarios(request.dadosBancarios())
                .pixKey(request.chavePix())
                .paymentDate(request.dataPagamento())
                .invoiceDate(request.dataPrevistaNotaFiscal())
                .origin(request.origem())
                .value(request.valor())
                .supplierName(request.nomeFornecedor())
                .attachments(attachments)
                .reason(request.motivo())
                .managerName(request.gestorNome())
                .managerEmail(request.gestorEmail())
                .contractNumber(request.numeroContrato())
                .orderNumber(request.numeroPedido())
                .regionalId(request.idRegional())
                .integralPayment(request.pagamentoIntegral())
                .ceilingValue(request.valorTeto())
                .build();
    }

    public static AdvanceSuppliers fromSecondStep(
            @Validated(AdvanceSuppliersRequest.SecondStep.class) AdvanceSuppliersRequest request,
            AdvanceSuppliers old
            ) {
        old.setManagerApprove(request.aprovacaoGestor());
        return old;
    }

    public static AdvanceSuppliers fromThirdStep(
            @Validated(AdvanceSuppliersRequest.ThirdStep.class) AdvanceSuppliersRequest request,
            AttachmentEntity attachment,
            AdvanceSuppliers old
    ) {
        old.setFinanceApprove(request.aprovacaoFinanceiro());
        old.setProofOfPayment(attachment);
        return old;
    }

    public static AdvanceSuppliers fromFourthStep(
            @Validated(AdvanceSuppliersRequest.FourthStep.class) AdvanceSuppliersRequest request,
            AttachmentEntity invoiceDocument,
            AdvanceSuppliers old
    ) {
        old.setInvoiceDocument(invoiceDocument);
        old.setReceiptNotice(request.numeroAvisoRecebimento());
        return old;
    }

}
