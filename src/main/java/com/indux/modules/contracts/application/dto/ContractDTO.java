package com.indux.modules.contracts.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractDTO {
    private Long id;
    private String os;
    private String osMega;
    private String anoOs;
    private String cliente;
    private String codSap;
    private LocalDate dataAssinatura;
    private LocalDate dataFim;
    private BigDecimal porcentagemReajuste;
    private String mesReajuste;
    private Boolean ativo;
    private String tipoAditivo;
    private String regional;
    private String gestorInternoContrato;
    private String gestorClienteContrato;
    private BigDecimal valorContrato;
    private String nomeProjeto;
    private Boolean dataConhecida;
    private Long megaId;
    private Long filialId;
    private Long rateioId;
    private String nomeCentroCustos;
    private String descricaoEscopo;
    private Long idDisciplina;
    private Long idCliente;
    private String gestorInternoCustos;
    private String coordenadorContrato;
    private String emailGestor;
    private String emailCoordenador;
    private String contatoGestor;
    private String contatoCoordenador;
    private LocalDate dataInicio;
    private String nome;
    private Long clienteId;
    private Long plataformaId;
    private String icj;
} 