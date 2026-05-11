package com.indux.modules.flash_fuel.domain.dtos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.indux.core.domain.model.modules.AttachmentRecord;
import com.indux.core.infra.exception.module.ModuleBadRequest;
import com.mongodb.lang.Nullable;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record FlashFuelRequest(

        @NotNull(message = "Item obrigatório.", groups = FirstStep.class) Boolean colaborador,
        @NotNull(message = "Item obrigatório.", groups = FirstStep.class) LocalDate dataSolicitacao,
        @Nullable String matriculaSolicitante,
        @NotNull(message = "Item obrigatório.", groups = FirstStep.class) String nomeSolicitante,
        @NotNull(message = "Item obrigatório.", groups = FirstStep.class) String cpfSolicitante,
        @NotNull(message = "Item obrigatório.", groups = FirstStep.class) String gestorFilial,
        @NotNull(message = "Item obrigatório.", groups = FirstStep.class) String regional,
        @NotNull(message = "Item obrigatório.", groups = FirstStep.class) Integer regionalId,
        @NotNull(message = "Item obrigatório.", groups = FirstStep.class) String projetoNome,
        @NotNull(message = "Item obrigatório.", groups = FirstStep.class) Integer projetoId,
        @NotNull(message = "Item obrigatório.", groups = FirstStep.class) String valor,
        @NotNull(message = "Item obrigatório.", groups = FirstStep.class) String motivo,
        @NotNull(message = "Item obrigatório.", groups = FirstStep.class) List<AttachmentRecord> anexos,
        @Nullable List<String> anexosRemover,

        // Campo para valorContrato como String (será convertido internamente)
        @NotNull(message = "Item obrigatório.", groups = FirstStep.class) String valorContrato,

        // Segundo Passo
        Boolean aprovacaoGestor,

        // Terceiro Passo
        Boolean aprovacaoFinanceiro,

        //Quarto passo
        @NotNull(message = "Item obrigatório", groups = FourthStep.class) MultipartFile anexo,

        // Quinto Passo
        @NotNull(message = "Item obrigatório.", groups = FifthStep.class) Boolean avisoRecebimentoFeito,
        String avisoRecebimento,
        String observacao


) {
    public interface FirstStep {
    }

    public interface FourthStep {
    }
    public interface FifthStep {
    }

    public BigDecimal getValorAsBigDecimal() {
        if (valor == null || valor.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }

        try {
            String cleanValue = valor.trim().replace("R$", "").replace(" ", "").trim();

            if (cleanValue.contains(".") && !cleanValue.contains(",")) {
                return new BigDecimal(cleanValue);
            }

            if (cleanValue.contains(",")) {
                String americanFormat = cleanValue.replace(".", "").replace(",", ".");
                return new BigDecimal(americanFormat);
            }

            return new BigDecimal(cleanValue);
        } catch (NumberFormatException e) {
            throw new ModuleBadRequest("Formato de valor inválido: " + valor);
        }
    }

    // Método para converter a String JSON para List<ValorContratoDTO>
    public List<ValorContratoDTO> getValorContratoAsList() {
        if (valorContrato == null || valorContrato.trim().isEmpty()) {
            return List.of();
        }
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(valorContrato, new TypeReference<List<ValorContratoDTO>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Erro ao converter valorContrato: " + e.getMessage());
        }
    }
}