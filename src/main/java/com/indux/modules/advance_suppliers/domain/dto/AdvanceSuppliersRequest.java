package com.indux.modules.advance_suppliers.domain.dto;

import com.indux.core.domain.model.employee.BankDetails;
import com.indux.core.domain.model.modules.AttachmentRecord;
import com.indux.modules.advance_suppliers.domain.entities.BankType;
import com.indux.modules.advance_suppliers.domain.entities.OriginRequest;
import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record AdvanceSuppliersRequest(
// first step //
        @NotNull(message = "Item obrigatório", groups = FirstStep.class) Boolean temCnpj,
        @NotNull(message = "Item obrigatório", groups = FirstStep.class) Integer idRegional,
        @NotNull(message = "Item obrigatório", groups = FirstStep.class) Integer idProjeto,
        @NotNull(message = "Item obrigatório", groups = FirstStep.class) String projetoNome,
        String contrato,
        Integer contratoID,
        @Nullable String numeroContrato,
        @Nullable String numeroPedido,
        @Nullable BankType tipo_de_envio,
        @Nullable String cnpj,
        @Nullable String cpf,
        @Nullable BankDetails dadosBancarios,
        @Nullable String chavePix,
        @Nullable LocalDate dataPagamento,
        @NotNull(message = "Item obrigatório", groups = FirstStep.class) LocalDate dataPrevistaNotaFiscal,
        @NotNull(message = "Item obrigatório", groups = FirstStep.class) OriginRequest origem,
        @NotNull(message = "Item obrigatório", groups = FirstStep.class) BigDecimal valor,
        @Nullable String nomeFornecedor,
        @Nullable List<AttachmentRecord> anexos,
        @Nullable List<String> anexosRemover,
        @Nullable String motivo,
        @NotNull(message = "Item obrigatório", groups = FirstStep.class) String gestorNome,
        @NotNull(message = "Item obrigatório", groups = FirstStep.class) String gestorEmail,
        @Nullable String regional,
        @Nullable Boolean pagamentoIntegral,
        @Nullable BigDecimal valorTeto,
        //---second step---//
        @NotNull(message = "Item obrigatório", groups = SecondStep.class) Boolean aprovacaoGestor,

        //---terceira step---//
        @NotNull(message = "Item obrigatório", groups = ThirdStep.class) Boolean aprovacaoFinanceiro,
        @NotNull(message = "Item obrigatório", groups = ThirdStep.class) AttachmentRecord comprovante_pagamento,

        //--fourth step --//
        @Nullable AttachmentRecord nota_fiscal,
        @NotNull(message = "Item obrigatório", groups = FourthStep.class) String numeroAvisoRecebimento,

        //general//
        String observacao
) {
    public interface FirstStep{};
    public interface SecondStep{};
    public interface ThirdStep{};
    public interface FourthStep{};

}

