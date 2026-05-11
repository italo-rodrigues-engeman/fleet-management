package com.indux.modules.ocf.domain.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "notification_schedules")
public class NotificationSchedule {
    
    @Id
    private String id;
    
    @Field(name = "canais")
    private List<String> canais;
    
    @Field(name = "tempo_limite_horas")
    private Double tempoLimiteHoras;
    
    @Field(name = "etapa")
    private Integer etapa;
    
    @Field(name = "user_ids")
    private List<String> userIds;
    
    @Field(name = "texto_notificacao")
    private String textoNotificacao;
    
    @Field(name = "ativo")
    private Boolean ativo;
    
    @Field(name = "criado_em")
    private LocalDateTime criadoEm;
    
    @Field(name = "atualizado_em")
    private LocalDateTime atualizadoEm;
    
    @Field(name = "criado_por")
    private String criadoPor;
    
    @Field(name = "atualizado_por")
    private String atualizadoPor;
    
    @Field(name = "ocorrencias_notificadas")
    private List<String> ocorrenciasNotificadas;
    
    // Construtores
    public NotificationSchedule() {
        this.criadoEm = LocalDateTime.now();
        this.ativo = true;
    }
    
    public NotificationSchedule(List<String> canais, Double tempoLimiteHoras, Integer etapa, List<String> userIds, String textoNotificacao, String criadoPor) {
        this();
        this.canais = canais;
        this.tempoLimiteHoras = tempoLimiteHoras;
        this.etapa = etapa;
        this.userIds = userIds;
        this.textoNotificacao = textoNotificacao;
        this.criadoPor = criadoPor;
    }
    
    // Getters e Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public List<String> getCanais() {
        return canais;
    }
    
    public void setCanais(List<String> canais) {
        this.canais = canais;
    }
    
    public Double getTempoLimiteHoras() {
        return tempoLimiteHoras;
    }
    
    public void setTempoLimiteHoras(Double tempoLimiteHoras) {
        this.tempoLimiteHoras = tempoLimiteHoras;
    }
    
    public Integer getEtapa() {
        return etapa;
    }
    
    public void setEtapa(Integer etapa) {
        this.etapa = etapa;
    }
    
    public List<String> getUserIds() {
        return userIds;
    }
    
    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
    
    public String getTextoNotificacao() {
        return textoNotificacao;
    }
    
    public void setTextoNotificacao(String textoNotificacao) {
        this.textoNotificacao = textoNotificacao;
    }
    
    public Boolean getAtivo() {
        return ativo;
    }
    
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
    
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    
    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
    
    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
    
    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
    
    public String getCriadoPor() {
        return criadoPor;
    }
    
    public void setCriadoPor(String criadoPor) {
        this.criadoPor = criadoPor;
    }
    
    public String getAtualizadoPor() {
        return atualizadoPor;
    }
    
    public void setAtualizadoPor(String atualizadoPor) {
        this.atualizadoPor = atualizadoPor;
    }
    
    public List<String> getOcorrenciasNotificadas() {
        return ocorrenciasNotificadas;
    }
    
    public void setOcorrenciasNotificadas(List<String> ocorrenciasNotificadas) {
        this.ocorrenciasNotificadas = ocorrenciasNotificadas;
    }
    
    @Override
    public String toString() {
        return "NotificationSchedule{" +
                "id='" + id + '\'' +
                ", canais=" + canais +
                ", tempoLimiteHoras=" + tempoLimiteHoras +
                ", etapa=" + etapa +
                ", userIds=" + userIds +
                ", textoNotificacao='" + textoNotificacao + '\'' +
                ", ativo=" + ativo +
                ", criadoEm=" + criadoEm +
                ", atualizadoEm=" + atualizadoEm +
                ", criadoPor='" + criadoPor + '\'' +
                ", atualizadoPor='" + atualizadoPor + '\'' +
                ", ocorrenciasNotificadas=" + ocorrenciasNotificadas +
                '}';
    }
}
