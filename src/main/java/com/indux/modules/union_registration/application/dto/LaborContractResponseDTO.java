package com.indux.modules.union_registration.application.dto;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.StepLog;
import com.indux.modules.union_registration.application.dto.labor_rights.LaborRightsDTO;
import com.indux.modules.union_registration.domain.enums.TipoInstrumento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LaborContractResponseDTO {
    
    private String id;
    private TipoInstrumento tipoInstrumento;
    private String numeroRegistro;
    private String numeroSolicitacao;
    private String numeroIdentificacaoInterno;
    private String nomeInstrumento;
    private String apelido;
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
    private String categoriaPrincipalCBO;
    private String subcategoriaCBO;
    private List<String> funcoesEspecificas;
    private String excecoesInclusoes;
    private String situacaoMTE;
    private List<AttachmentEntity> arquivosInstrumento;
    private String linkAnexo;
    private String resumoIA;
 
    private LaborRightsDTO laborRights;
    
    private LocalDateTime dataCriacao;
    private String usuarioCriacao;
    private String statusRegistro;
    private String nomeSindicato;
    private String siglaSindicato;
    private List<LaborContractAddendumResponseDTO> addendums;
    private Integer ultimaSequenciaAditivo; // Última sequência de aditivo cadastrada
    
    // Auditoria
    private List<StepLog> stepLog;
    private LocalDateTime dataUltimaAtualizacao;
    private String usuarioUltimaAtualizacao;
}
