package com.indux.modules.union_registration.application.dto;

import com.indux.modules.union_registration.application.dto.labor_rights.LaborRightsDTO;
import com.indux.modules.union_registration.domain.enums.TipoInstrumento;
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
public class CreateLaborContractRequestDTO {
    
    // 1. Tipo do instrumento (Dropdown) - Obrigatório
    @NotNull(message = "Tipo do instrumento é obrigatório")
    private TipoInstrumento tipoInstrumento;
    
    // 1.1. Apelido (String 100) - Não obrigatório
    private String apelido;
    
    // 2. Número do registro (String 50) - Não obrigatório
    private String numeroRegistro;
    
    // 3. Número da solicitação (String 50) - Não obrigatório
    private String numeroSolicitacao;
    
    
    // 6. Sindicato trabalhadores (Dropdown) - Obrigatório (definido pela URL)
    private String sindicatoTrabalhadoresId;
    
    // 7. Empresas signatárias (Multi-select) - Não obrigatório
    private List<String> empresasSignatarias;
    
    // 8. Data início vigência (Date) - Obrigatório
    @NotNull(message = "Data de início da vigência é obrigatória")
    private LocalDate dataInicioVigencia;
    
    // 9. Data fim vigência (Date) - Obrigatório
    @NotNull(message = "Data de fim da vigência é obrigatória")
    private LocalDate dataFimVigencia;
    
    // 10. Data base (Date) - Obrigatório
    @NotNull(message = "Data base é obrigatória")
    private LocalDate dataBase;
    
    // 11. Abrangência territorial (Dropdown) - Obrigatório
    @NotBlank(message = "Abrangência territorial é obrigatória")
    private String abrangenciaTerritorial;
    
    // 12. UF principal (List<String>) - Obrigatório
    @NotNull(message = "UF principal é obrigatória")
    private List<String> ufPrincipal;
    
    // 13. Município (List<String>) - Não obrigatório
    private List<String> municipio;
    
    // 14. Municípios abrangidos (Multi-select) - Condicional
    private List<String> municipiosAbrangidos;
    
    // 14. Estados adicionais (Multi-select) - Não obrigatório
    private List<String> estadosAdicionais;
    
    // 15. Observações territoriais (Text) - Não obrigatório
    private String observacoesTerritoriais;
    
    // 16. Subcategoria CBO (Dropdown) - Opcional
    private String subcategoriaCBO;
    
    // 18. Funções específicas (Multi-select) - Não obrigatório
    private List<String> funcoesEspecificas;
    
    
    // 20. Exceções/Inclusões (Text) - Não obrigatório
    private String excecoesInclusoes;
    
    // 21. Situação MTE (Dropdown) - Não obrigatório
    private String situacaoMTE;
    
    // 22. Arquivo instrumento (File Upload) - Não obrigatório
    private List<MultipartFile> arquivosInstrumento;
    
    // 23. Link anexo (URL) - Não obrigatório
    private String linkAnexo;
    
    // 24. Resumo da IA (Text ilimitado) - Não obrigatório
    private String resumoIA;
    
    // Campos de Direitos Trabalhistas
    private LaborRightsDTO laborRights;
}
