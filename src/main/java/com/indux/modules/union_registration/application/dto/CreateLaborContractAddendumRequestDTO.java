package com.indux.modules.union_registration.application.dto;

import com.indux.modules.union_registration.application.dto.labor_rights.LaborRightsDTO;
import com.indux.modules.union_registration.domain.enums.TipoInstrumento;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLaborContractAddendumRequestDTO {
    
    // Campos obrigatórios
    @NotNull(message = "Tipo do instrumento é obrigatório")
    private TipoInstrumento tipo;
    
    @NotBlank(message = "O título do aditivo é obrigatório.")
    private String titulo; // Título do aditivo
    
    private String apelido; // Apelido do aditivo (opcional)
    
    @NotNull(message = "A data de inclusão do aditivo é obrigatória.")
    private LocalDate dataInclusao;
    
    @NotNull(message = "A data de início da vigência do aditivo é obrigatória.")
    private LocalDate dataInicio; // Data de início da vigência do aditivo
    
    @NotNull(message = "A data de fim da vigência do aditivo é obrigatória.")
    private LocalDate dataFim; // Data de fim da vigência do aditivo
    
    @NotBlank(message = "O link do aditivo é obrigatório.")
    private String link;
    
    @NotBlank(message = "O status do aditivo é obrigatório.")
    private String status;
    
    // Campos opcionais - todos os campos de direitos trabalhistas
    private LaborRightsDTO laborRights;
    
    // Anexos opcionais
    private List<MultipartFile> arquivosAnexos;
    
    @AssertTrue(message = "Data de início deve ser anterior à data de fim")
    public boolean isDataInicioBeforeDataFim() {
        if (dataInicio == null || dataFim == null) {
            return true; // Deixar outras validações @NotNull tratarem os nulos
        }
        return dataInicio.isBefore(dataFim);
    }

    // Campos adicionais para alinhar com o cadastro de ACT/CCT (mantidos opcionais)
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
