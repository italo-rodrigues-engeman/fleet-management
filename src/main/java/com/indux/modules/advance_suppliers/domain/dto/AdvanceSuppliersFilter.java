package com.indux.modules.advance_suppliers.domain.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record AdvanceSuppliersFilter(
        List<String> status,
        List<String> situacoes,
        List<Integer> etapasAtuais,
        Instant criadoDe,
        Instant criadoAte,
        String id,
        Long codeId,
        List<Integer> regionais,
        List<Integer> projetos,

        // 🔥 Específicos do domínio
        String nomeFornecedor,
        String nomeSolicitante,
        String cpf,
        String cnpj,
        String tipoDeEnvio,
        String avisoRecebimento,
        String numeroContrato,
        String numeroPedido,

        String valor,
        LocalDate dataPagamento,
        LocalDate dataPrevistaNotaFiscal,

        Boolean aprovacaoGestor,
        Boolean aprovacaoFinanceiro
) {
    public AdvanceSuppliersFilter withRegionalAndProject(
            List<Integer> regionais,
            List<Integer> projects,
            List<Integer> currentStep
    ) {
        return new AdvanceSuppliersFilter(
                status,
                situacoes,
                List.copyOf(currentStep),
                criadoDe,
                criadoAte,
                id,
                codeId,
                List.copyOf(regionais),
                List.copyOf(projects),
                nomeFornecedor,
                nomeSolicitante,
                cpf,
                cnpj,
                tipoDeEnvio,
                avisoRecebimento,
                numeroContrato,
                numeroPedido,
                valor,
                dataPagamento,
                dataPrevistaNotaFiscal,
                aprovacaoGestor,
                aprovacaoFinanceiro
        );
    }
}
