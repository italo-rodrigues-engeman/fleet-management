package com.indux.modules.union_registration.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnionDetailedWithSummaryDTO {
    private String id;
    private Integer codeID;
    private String nomeCompletoSindicato;
    private String cnpj;
    private String codigoCnes;
    private String tipo;
    private String categoriaRepresentada;
    private String abrangenciaTerritorial;
    private List<String> ufSede;
    private List<String> municipioSede;
    private List<String> ufsAtendidas;
    private List<String> municipiosAtendidos;
    private String observacoesTerritoriais;
    private String logradouro;
    private String numero;
    private String uf;
    private String cep;
    private String cidade;
    private String telefonePrincipal;
    private String telefone2;
    private String emailInstitucional;
    private String email2;
    private String site;
    private String redeSocial;
    private String situacaoMte;
    private LocalDate dataUltimaAtualizacaoMte;
    private String presidenteAtual;
    private LocalDate mandatoInicio;
    private LocalDate mandatoFim;
    private String observacoes;
    private String usuarioCriacao;
    private String statusRegistro;
    private List<LaborContractSummaryDTO> laborContracts;
}
