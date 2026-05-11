package com.indux.modules.union_registration.domain.model;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.StepLog;
import com.indux.modules.union_registration.application.dto.labor_rights.LaborRightsDTO;
import com.indux.modules.union_registration.domain.enums.TipoInstrumento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "labor_contract_addendums")
public class LaborContractAddendum {
    
    @Id
    private String id;
    
    // Referência ao contrato principal
    private String contratoTrabalhistaId;
    
    // Sequência do aditivo (auto-incrementada por contrato)
    private Integer sequencia;
    
    // Campos obrigatórios
    private TipoInstrumento tipo;
    private String titulo; // Título do aditivo
    private String apelido;
    private LocalDate dataInclusao;
    private LocalDate dataInicio; // Data de início da vigência do aditivo
    private LocalDate dataFim; // Data de fim da vigência do aditivo
    private String link;
    private String status;
    
    // Direitos trabalhistas (DTO agrupado)
    private LaborRightsDTO laborRights;
    
    // Campos adicionais do ACT/CCT (opcionais no aditivo)
    private TipoInstrumento tipoInstrumento;
    private String numeroRegistro;
    private String numeroSolicitacao;
    private String sindicatoTrabalhadoresId;
    private List<String> empresasSignatarias;
    private LocalDate dataInicioVigencia;
    private LocalDate dataFimVigencia;
    private LocalDate dataBase;
    private String abrangenciaTerritorial;
    private List<String> ufPrincipal;
    private List<String> municipio;
    private List<String> municipiosAbrangidos;
    private List<String> estadosAdicionais;
    private String observacoesTerritoriais;
    private String subcategoriaCBO;
    private List<String> funcoesEspecificas;
    private String excecoesInclusoes;
    private String situacaoMTE;
    private List<AttachmentEntity> arquivosInstrumento;
    private String linkAnexo;
    private String resumoIA;
    
    // Anexos
    private List<AttachmentEntity> arquivosAnexos;
    
    // Metadados
    private LocalDateTime dataCriacao;
    private String usuarioCriacao;
    private LocalDateTime dataUltimaAtualizacao;
    private String usuarioUltimaAtualizacao;
    private String statusRegistro; // ATIVO, INATIVO
    
    // Auditoria
    private List<StepLog> stepLog;
}
