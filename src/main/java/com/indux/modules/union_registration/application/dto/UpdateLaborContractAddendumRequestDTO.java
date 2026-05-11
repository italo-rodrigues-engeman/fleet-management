package com.indux.modules.union_registration.application.dto;

import com.indux.modules.union_registration.application.dto.labor_rights.LaborRightsDTO;
import com.indux.modules.union_registration.domain.enums.TipoInstrumento;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLaborContractAddendumRequestDTO {
    
    // Campos opcionais - todos podem ser atualizados
    // Nota: sequencia não pode ser atualizada manualmente, é auto-gerenciada
    private TipoInstrumento tipo;
    private String titulo;
    private String apelido;
    private LocalDate dataInclusao;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private String link;
    private String status;
    
    // Direitos trabalhistas
    private LaborRightsDTO laborRights;
    
    // Anexos (substituem completamente os anteriores se fornecidos)
    private List<MultipartFile> arquivosAnexos;
    
    // Campos adicionais alinhados com ACT/CCT
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
    private List<MultipartFile> arquivosInstrumento;
    private String linkAnexo;
    private String resumoIA;
}


