package com.indux.core.domain.model.cbo;

import com.indux.core.application.dto.cbo.RelatedPosition;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Document(collection = "cbo_detalhes")
public class CBODetails {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Field("_id")
    private String id;
    @Field("cod_cbo_prefixo")
    private Integer codCBO;
    @Field("cargos_relacionados")
    private List<RelatedPosition> relatedPosition;
    @Field("nome_cargo_prefixo")
    private String nameCBO;
    @Field("desc_sumaria")
    private String description;
    @Field("atividade")
    private String activity;
    @Field("formacao_experiencia")
    private String formation;
    @Field("condicoes_exercicio")
    private String codintion;
    @Field("notas")
    private String notes;
    @Field("recursos_trabalho")
    private String resorces;
    @Field("glossario")
    private String glossary;

}
