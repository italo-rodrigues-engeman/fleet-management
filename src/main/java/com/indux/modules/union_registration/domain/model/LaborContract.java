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
@Document(collection = "labor_contracts")
public class LaborContract {
    
    @Id
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
    private List<AttachmentEntity> arquivoInstrumento;
    private String linkAnexo;
    private String resumoIA;
    private LaborRightsDTO laborRights;
    private LocalDateTime dataCriacao;
    private String usuarioCriacao;
    private String statusRegistro;
    
    // Auditoria
    private List<StepLog> stepLog;
    private LocalDateTime dataUltimaAtualizacao;
    private String usuarioUltimaAtualizacao;
    
    public LaborContract(TipoInstrumento tipoInstrumento, String numeroIdentificacaoInterno, String nomeInstrumento,
                        String sindicatoTrabalhadoresId, LocalDate dataInicioVigencia, LocalDate dataFimVigencia,
                        LocalDate dataBase, String abrangenciaTerritorial, List<String> ufPrincipal,
                        String categoriaPrincipalCBO, String usuarioCriacao) {
        this.tipoInstrumento = tipoInstrumento;
        this.numeroIdentificacaoInterno = numeroIdentificacaoInterno;
        this.nomeInstrumento = nomeInstrumento;
        this.sindicatoTrabalhadoresId = sindicatoTrabalhadoresId;
        this.dataInicioVigencia = dataInicioVigencia;
        this.dataFimVigencia = dataFimVigencia;
        this.dataBase = dataBase;
        this.abrangenciaTerritorial = abrangenciaTerritorial;
        this.ufPrincipal = ufPrincipal;
        this.categoriaPrincipalCBO = categoriaPrincipalCBO;
        this.usuarioCriacao = usuarioCriacao;
        this.dataCriacao = LocalDateTime.now();
        this.statusRegistro = "ATIVO";
    }
}
