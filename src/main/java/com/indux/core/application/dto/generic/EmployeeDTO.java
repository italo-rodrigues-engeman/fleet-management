package com.indux.core.application.dto.generic;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.indux.core.domain.model.employee.ContractProject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class EmployeeDTO {
    // Construtor para JPQL (19 parâmetros incluindo centro_custos_id, filial_id_hcm
    // e data_nascimento)
    public EmployeeDTO(UUID id, String matricula, String cpf, String name,
            String email_particular, String email_comercial, String cargo,
            String telefone, String telefone2, Long filial_id, String filial_nome,
            String statusColaborador, BankDetailDTO dadosBancarios,
            ContractProject contrato, String sispat,
            String centro_custos_id, Integer filial_id_hcm,
            java.time.LocalDate data_nascimento, java.time.LocalDate data_admissao) {
        this.id = id;
        this.matricula = matricula;
        this.cpf = cpf;
        this.name = name;
        this.email_particular = email_particular;
        this.email_comercial = email_comercial;
        this.cargo = cargo;
        this.telefone = telefone;
        this.telefone2 = telefone2;
        this.filial_id = filial_id;
        this.filial_nome = filial_nome;
        this.statusColaborador = statusColaborador;
        this.dadosBancarios = dadosBancarios;
        this.contrato = contrato;
        this.sispat = sispat;
        this.centro_custos_id = centro_custos_id;
        this.filial_id_hcm = filial_id_hcm != null ? filial_id_hcm.toString() : null;
        this.data_nascimento = data_nascimento != null ? data_nascimento.toString() : null;
        this.data_admissao = data_admissao != null ? data_admissao.toString() : null;
    }

    private UUID id;
    private String matricula, cpf, name;
    private String email_particular;
    private String email_comercial;
    private String cargo, telefone, telefone2;
    private Long filial_id;
    private String filial_nome;
    private String statusColaborador;
    private BankDetailDTO dadosBancarios;
    private ContractProject contrato;
    private String sispat;
    private String centro_custos_id;

    // Campos adicionais do banco de dados
    private String cargo_id;
    private String data_demissao;
    private String data_admissao;
    private String situacao;
    private String data_nascimento;
    private String rateio_id;
    private String cidade_funcionario;
    private String sexo_colaborador;
    private String grau_instrucao;
    private String nome_mae;
    private String nome_pai;
    private String estado;
    private String qtd_dependentes;
    private String filial_id_hcm;

    // Hierarquia organizacional
    private EmployeeSummaryDTO.HierarchyInfo hierarchy;
}