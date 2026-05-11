package com.indux.core.application.dto.generic;

import java.util.List;

public record EmployeeFilter(
        List<String> matricula,
        List<String> nome,
        List<String> cidade,
        List<String> estado,
        List<String> sexo,
        List<String> grauInstrucao,
        List<String> dataAdmissao,
        List<String> dataAdmissaoInicio,
        List<String> dataAdmissaoFim,
        List<String> dataNascimento,
        List<String> dataNascimentoInicio,
        List<String> dataNascimentoFim,
        List<String> contrato,
        List<String> filialId,
        List<String> filialIdHcm,
        List<String> centroCustosId,
        List<String> cargo,
        Boolean showFired,
        List<String> situacao,
        List<Integer> dependentes,
        List<Long> diretoriaId,
        List<Long> superintendenciaId,
        List<Long> regionalId,
        List<Long> setorId,
        List<Long> contratoId,
        List<Long> projetoId,
        Long tempoTrabalhoMinDias,
        Long tempoTrabalhoMaxDias) {

}
