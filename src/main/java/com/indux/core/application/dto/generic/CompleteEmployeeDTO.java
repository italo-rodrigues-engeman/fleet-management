package com.indux.core.application.dto.generic;

import com.indux.core.domain.model.employee.Employee;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CompleteEmployeeDTO {
    private UUID id;
    private String cpf;
    private String nome;
    private String telefone;
    private String telefone2;
    private String email_particular;
    private String gestor_id;
    private String grupo_id;
    private String cargo_id;
    private String email_comercial;
    private String centro_custos_id;
    private String filial_id;
    private Integer filial_id_hcm;
    private String nome_filial;
    private String cnpj_filial;
    private String inscricao_municipal_filial;
    private String municipio_filial;
    private String email;
    private LocalDate data_demissao;
    private LocalDate data_admissao;
    private String situacao;
    private LocalDate data_nascimento;
    private Integer rateio_id;
    private String matricula;
    private Boolean exibir_php;
    private String cidade_funcionario;
    private String grau_instrucao;
    private String sexo_colaborador;
    private String nome_mae;
    private String nome_pai;
    private String estado;
    private String regional;
    private Integer quantidade_dependentes;
    private Integer quantidade_cpfs_dependentes;
    private String gestor_interno_contrato;
    private List<String> coordenadores_contrato;
    private String matricula_responsavel_filial;
    private String nome_responsavel_filial;
    private String razao_social;
    private String endereco;
    private String cargoNome;
    private String nomeProjeto;
    private String sispat;
    private List<HistoricoTrabalhoDTO> historico_trabalhos;
    private Long tempo_total_empresa_dias;
    private String tempo_total_empresa_formatado;
    private String cliente;
    private List<EmployeePlanDTO> planos;
    
    // Hierarquia organizacional
    private EmployeeSummaryDTO.HierarchyInfo hierarchy;
    private Boolean pj;
    private String estadoCivil;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HistoricoTrabalhoDTO {
        private String cpf;
        private String matricula;
        private String cargo;
        private String cargo_nome;
        private LocalDate data_admissao;
        private LocalDate data_demissao;
        private String telefone;
        private String email_particular;
    }

    public static CompleteEmployeeDTO fromEntity(Employee e, String gestorInternoContrato, 
            String regional, Integer quantidadeDependentes, Integer quantidadeCpfsDependentes, 
            String matriculaResponsavelFilial, String nomeResponsavelFilial, String razaoSocial, 
            String endereco, String nomeCargo, String nomeProjeto, List<HistoricoTrabalhoDTO> historicoTrabalhos,
            String nomeFilial, String cnpjFilial, String inscricaoMunicipalFilial, String municipioFilial,
            Long tempoTotalEmpresaDias, String tempoTotalEmpresaFormatado, String cliente, List<String> coordenadoresContrato,
            List<EmployeePlanDTO> planos, EmployeeSummaryDTO.HierarchyInfo hierarchy) {
        return CompleteEmployeeDTO.builder()
                .id(e.getId())
                .cpf(e.getCpf())
                .nome(e.getName())
                .telefone(e.getCellphone())
                .telefone2(e.getCellphone2())
                .email_particular(e.getPersonalEmail())
                .cargo_id(e.getPosition())
                .email_comercial(e.getBusinessEmail())
                .centro_custos_id(e.getCostCenterId())
                .filial_id(e.getBranch_id() != null ? e.getBranch_id().toString() : null)
                .filial_id_hcm(e.getFilialIdHcm())
                .nome_filial(nomeFilial)
                .cnpj_filial(cnpjFilial)
                .inscricao_municipal_filial(inscricaoMunicipalFilial)
                .municipio_filial(municipioFilial)
                .data_demissao(e.getTerminationDate())
                .data_admissao(e.getAdmissionDate())
                .situacao(e.getStatusEmployee())
                .data_nascimento(e.getBirthDate())
                .rateio_id(e.getContract_id())
                .matricula(e.getRegistration())
                .cidade_funcionario(e.getCity())
                .grau_instrucao(e.getEducationLevel())
                .sexo_colaborador(e.getGender())
                .nome_mae(e.getMotherName())
                .nome_pai(e.getFatherName())
                .estado(e.getState())
                .gestor_interno_contrato(gestorInternoContrato)
                .coordenadores_contrato(coordenadoresContrato)
                .regional(regional)
                .quantidade_dependentes(quantidadeDependentes)
                .quantidade_cpfs_dependentes(quantidadeCpfsDependentes)
                .matricula_responsavel_filial(matriculaResponsavelFilial)
                .nome_responsavel_filial(nomeResponsavelFilial)
                .razao_social(razaoSocial)
                .endereco(endereco)
                .cargoNome(nomeCargo)
                .nomeProjeto(nomeProjeto)
                .sispat(e.getSISPAT())
                .historico_trabalhos(historicoTrabalhos)
                .tempo_total_empresa_dias(tempoTotalEmpresaDias)
                .tempo_total_empresa_formatado(tempoTotalEmpresaFormatado)
                .cliente(cliente)
                .planos(planos)
                .hierarchy(hierarchy)
                .pj(e.getPj())
                .estadoCivil(e.getMaritalStatus())
                .build();
    }
}
