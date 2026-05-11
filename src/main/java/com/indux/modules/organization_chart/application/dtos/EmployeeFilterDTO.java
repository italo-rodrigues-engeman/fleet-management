package com.indux.modules.organization_chart.application.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeFilterDTO {
    private List<Long> diretoriaId;
    private List<Long> superintendenciaId;
    private List<Long> regionalId;
    private List<Long> setorId;
    private List<Long> contratoId;
    private List<Long> projetoId;
    private String centroCustoHcm;
    private String situacao;
    private List<String> nome;
    private List<String> matricula;
    private List<String> cidade;
    private List<String> estado;
    private List<String> sexo;
    private List<String> grauInstrucao;
    private List<String> dataAdmissao;
    private List<String> dataAdmissaoInicio;
    private List<String> dataAdmissaoFim;
    private List<String> dataNascimento;
    private List<String> contrato;
    private List<String> filialId;
    private List<String> filialIdHcm;
    private List<String> centroCustosId;
    private List<String> cargo;
    private Boolean showFired;
    private List<Integer> dependentes;
}


