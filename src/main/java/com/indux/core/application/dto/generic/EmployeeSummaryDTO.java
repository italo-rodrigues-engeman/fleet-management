package com.indux.core.application.dto.generic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.indux.core.domain.model.employee.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeSummaryDTO {
    private java.util.UUID id;
    private String nome;
    private String matricula;
    private String cpf;
    private String contrato;
    private String regional;
    private String status;
    private String cargo;
    private Integer quantidade_dependentes;
    private String grau_instrucao;
    private String tempoTotalEmpresa;
    private Long totalDiasEmpresa;
    private String nomeProjeto;
    private String costCenterName;
    private String cargoNome;
    private Long filialId;
    private String SISPAT;
    private String email_particular;
    private String telefone;
    private String telefone2;
    // Campo interno para armazenar planos (não será serializado no endpoint
    // employee/all)
    @JsonIgnore
    private List<PlanoInfo> planos;

    // Hierarquia organizacional
    private HierarchyInfo hierarchy;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlanoInfo {
        private String tipoPlano;
        private String nomePlano;
        private Integer codigoPlano;
        private String nomeSeguradora;
        private Integer codigoSeguradora;
        private Integer quantidadeDependentes;
        private Boolean temPlanoTitular;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HierarchyInfo {
        private Long projetoId;
        private String projetoNome;
        private Boolean projectStatus;
        private String projetoOs;
        private Integer projetoHcmId;
        private Integer projetoMegaId;
        private Integer projetoCentroCustoHcm;
        private List<Integer> projetoCentroCustoMega;
        private Integer projetoFilialMega;
        private java.util.List<Integer> projetoFiliaisHcm;
        private List<String> projetoFilialHCMName;
        private java.util.List<Integer> projetoFiliaisMega;

        private Long contratoId;
        private String contratoNome;
        private String contratoOs;

        private Long setorId;
        private String setorNome;
        private String setorSigla;
        private ResponsavelInfo setorResponsavel;

        private Long regionalId;
        private String regionalNome;
        private String regionalSigla;
        private ResponsavelInfo regionalResponsavel;

        private Long superintendenciaId;
        private String superintendenciaNome;
        private String superintendenciaSigla;
        private ResponsavelInfo superintendenciaResponsavel;

        private Long diretoriaId;
        private String diretoriaNome;
        private String diretoriaSigla;
        private ResponsavelInfo diretoriaResponsavel;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResponsavelInfo {
        private java.util.UUID id;
        private String nome;
        private String matricula;
        private String email;
        private String cargo;
    }

    public static EmployeeSummaryDTO fromEntity(
            Employee e,
            String regional,
            List<PlanoInfo> planos,
            Long totalDiasEmpresa,
            String costCenterName,
            String nomeCargo) {
        String tempoTotalEmpresa = null;
        if (totalDiasEmpresa != null) {
            long anos = totalDiasEmpresa / 365;
            long meses = (totalDiasEmpresa % 365) / 30;
            tempoTotalEmpresa = String.format("%d anos e %d meses", anos, meses);
        }

        return EmployeeSummaryDTO.builder()
                .id(e.getId())
                .nome(e.getName())
                .matricula(e.getRegistration())
                .cpf(e.getCpf())
                .contrato(e.getCostCenterId())
                .regional(regional)
                .status(e.getStatusEmployee())
                .cargo(e.getPosition())
                .filialId(e.getBranch_id() != null ? e.getBranch_id() : null)
                .quantidade_dependentes(e.getNumberOfDependents())
                .grau_instrucao(e.getEducationLevel())
                .tempoTotalEmpresa(tempoTotalEmpresa)
                .totalDiasEmpresa(totalDiasEmpresa)
                .nomeProjeto(costCenterName)
                .costCenterName(costCenterName)
                .cargoNome(nomeCargo)
                .SISPAT(e.getSISPAT())
                .email_particular(e.getPersonalEmail())
                .telefone(e.getCellphone())
                .planos(planos)
                .build();
    }
}