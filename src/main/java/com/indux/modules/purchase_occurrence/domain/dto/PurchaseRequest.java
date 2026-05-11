package com.indux.modules.purchase_occurrence.domain.dto;

import com.indux.core.domain.model.modules.AttachmentRecord;
import com.indux.modules.purchase_occurrence.domain.entities.BuyerRecord;
import com.indux.modules.purchase_occurrence.domain.entities.PurchaseOccurrenceCauses;
import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public record PurchaseRequest(
        @NotNull(message = "Deve-se atribuir um valor a pertinência do item.", groups = SecondStep.class) Boolean pertinente,
        @NotNull(message = "Deve-se atribuir um valor a solução da causa da ocorrência.", groups = SecondStep.class) String solucao,
        @Nullable String respostaSuprimentos,
        MultipartFile anexo,
        LocalDate dataVencimento,
        @NotNull(message = "O numéro do pedido é obrigatório.", groups = FirstStep.class) String numeroDoPedido,
        @NotNull(message = "O numero da nota fiscal é obrigatório.", groups = FirstStep.class) String numeroNotaFiscal,
        @NotNull(message = "A causa da ocorrência é obrigatório..", groups = FirstStep.class) PurchaseOccurrenceCauses causas,
        String justificativaSolicitacao,
        @NotNull(message = "O comprador é obrigatório.", groups = FirstStep.class) BuyerRecord comprador,
        @NotNull(message = "É necessário anexar algum arquivo.", groups = FirstStep.class) List<AttachmentRecord> anexos,
        @Nullable String nomeDivergente,
        @Nullable String cnpjDivergente,
        @Nullable String outros,
        @Nullable String observacao,
        @Nullable List<String> anexosRemover
) {

    public interface FirstStep{};
    public interface SecondStep{};
}

//SOLUÇÃO:
//;Alteração do Pedido
//;Alteração da Nota
//;Orientação da Base
//;Outros
