package com.indux.modules.ocf.domain.entities.mongo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "alodp_temp")
public class AlodpTempEntity {
    
    @Id
    private String id;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("telefone")
    private String telefone;
    
    @Field("nome-valid")
    private Boolean nomeValid;
    
    @Field("cpf-valid")
    private Boolean cpfValid;
    
    @Field("etapa")
    private String etapa;
    
    @Field("validacao")
    private Boolean validacao;
    
    @Field("status")
    private String status;
    
    @Field("nome_completo")
    private String nomeCompleto;
    
    @Field("cpf")
    private String cpf;
    
    @Field("contrato")
    private String contrato;
    
    @Field("matricula")
    private String matricula;
    
    @Field("flow_locked")
    private Boolean flowLocked;
    
    @Field("hora_ultima_msg")
    private Long horaUltimaMsg;
    
    @Field("responded")
    private Boolean responded;
    
    @Field("ultimo_tema_escolhido")
    private String ultimoTemaEscolhido;
}
