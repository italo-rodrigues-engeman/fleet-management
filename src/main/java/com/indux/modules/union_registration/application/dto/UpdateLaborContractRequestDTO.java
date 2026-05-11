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
public class UpdateLaborContractRequestDTO {
    
    private TipoInstrumento tipoInstrumento;
    private String apelido;
    private String numeroRegistro;
    private String numeroSolicitacao;
    private String numeroIdentificacaoInterno;
    private String nomeInstrumento;
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
    private List<MultipartFile> arquivosInstrumento;
    private String linkAnexo;
    private String resumoIA;
    
    // Campos de direitos trabalhistas
    private LaborRightsDTO laborRights;
}
