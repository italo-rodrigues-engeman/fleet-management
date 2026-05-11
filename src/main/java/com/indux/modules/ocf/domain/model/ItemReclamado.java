package com.indux.modules.ocf.domain.model;

import com.indux.core.domain.model.modules.form.FileMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.UUID;

@Document(collection = "item_reclamado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemReclamado {
    @Id
    private String id;
    private String descricao;
    private Integer quantidadeReclamada;
    private BigDecimal valor;
    private String justificativa;
    private String observacao;
    private String respostaColab;
    private FileMetadata anexoInicial;      // Anexo que vem inicialmente
    private FileMetadata anexoAdicional;    // Anexo que será inserido depois
    private String ocorrenciaId; // Referência para a ocorrência

    public ItemReclamado(String descricao, Integer quantidadeReclamada, BigDecimal valor, FileMetadata anexoInicial, String ocorrenciaId) {
        this.id = UUID.randomUUID().toString();
        this.descricao = descricao;
        this.quantidadeReclamada = quantidadeReclamada;
        this.valor = valor;
        this.justificativa = null;
        this.observacao = null;
        this.respostaColab = null;
        this.anexoInicial = anexoInicial;
        this.anexoAdicional = null;
        this.ocorrenciaId = ocorrenciaId;
    }

    public ItemReclamado(String descricao, Integer quantidadeReclamada, BigDecimal valor, FileMetadata anexoInicial, FileMetadata anexoAdicional, String ocorrenciaId) {
        this.id = UUID.randomUUID().toString();
        this.descricao = descricao;
        this.quantidadeReclamada = quantidadeReclamada;
        this.valor = valor;
        this.justificativa = null;
        this.observacao = null;
        this.respostaColab = null;
        this.anexoInicial = anexoInicial;
        this.anexoAdicional = anexoAdicional;
        this.ocorrenciaId = ocorrenciaId;
    }

    public ItemReclamado(String descricao, Integer quantidadeReclamada, BigDecimal valor, String justificativa, String observacao, String respostaColab, FileMetadata anexoInicial, FileMetadata anexoAdicional, String ocorrenciaId) {
        this.id = UUID.randomUUID().toString();
        this.descricao = descricao;
        this.quantidadeReclamada = quantidadeReclamada;
        this.valor = valor;
        this.justificativa = justificativa;
        this.observacao = observacao;
        this.respostaColab = respostaColab;
        this.anexoInicial = anexoInicial;
        this.anexoAdicional = anexoAdicional;
        this.ocorrenciaId = ocorrenciaId;
    }
} 