package com.indux.modules.contracts.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "tb_contratos")
@NoArgsConstructor
@AllArgsConstructor
public class Contract {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "os")
    private String os;
    
    @Column(name = "os_mega")
    private String osMega;
    
    @Column(name = "ano_os")
    private String anoOs;
    
    @Column(name = "cliente")
    private String cliente;
    
    @Column(name = "cod_sap")
    private String codSap;
    
    @Column(name = "data_assinatura")
    private LocalDate dataAssinatura;
    
    @Column(name = "data_fim")
    private LocalDate dataFim;
    
    @Column(name = "porcentagem_reajuste")
    private BigDecimal porcentagemReajuste;
    
    @Column(name = "mes_reajuste")
    private String mesReajuste;
    
    @Column(name = "ativo")
    private Boolean ativo;
    
    @Column(name = "tipo_aditivo")
    private String tipoAditivo;
    
    @Column(name = "regional")
    private String regional;
    
    @Column(name = "gestor_interno_contrato")
    private String gestorInternoContrato;
    
    @Column(name = "gestor_cliente_contrato")
    private String gestorClienteContrato;
    
    @Column(name = "valor_contrato")
    private BigDecimal valorContrato;
    
    @Column(name = "nome_projeto")
    private String nomeProjeto;
    
    @Column(name = "data_conhecida")
    private Boolean dataConhecida;
    
    @Column(name = "mega_id")
    private Long megaId;
    
    @Column(name = "filial_id")
    private Long filialId;
    
    @Column(name = "rateio_id")
    private Long rateioId;
    
    @Column(name = "nome_centro_custos")
    private String nomeCentroCustos;
    
    @Column(name = "descricao_escopo", columnDefinition = "TEXT")
    private String descricaoEscopo;
    
    @Column(name = "id_disciplina")
    private Long idDisciplina;
    
    @Column(name = "id_cliente")
    private Long idCliente;
    
    @Column(name = "gestor_interno_custos")
    private String gestorInternoCustos;
    
    @Column(name = "coordenador_contrato")
    private String coordenadorContrato;
    
    @Column(name = "email_gestor")
    private String emailGestor;
    
    @Column(name = "email_coodenador")
    private String emailCoordenador;
    
    @Column(name = "contato_gestor")
    private String contatoGestor;
    
    @Column(name = "contato_coodenador")
    private String contatoCoordenador;
    
    @Column(name = "data_inicio")
    private LocalDate dataInicio;
    
    @Column(name = "nome")
    private String nome;
    
    @Column(name = "cliente_id")
    private Long clienteId;
    
    @Column(name = "plataforma_id")
    private Long plataformaId;
    
    @Column(name = "icj")
    private String icj;
    
    @PrePersist
    protected void onCreate() {
        if (ativo == null) {
            ativo = false;
        }
        if (dataConhecida == null) {
            dataConhecida = false;
        }
    }
} 