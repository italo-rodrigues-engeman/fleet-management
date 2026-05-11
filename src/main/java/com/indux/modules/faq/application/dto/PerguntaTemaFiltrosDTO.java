package com.indux.modules.faq.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerguntaTemaFiltrosDTO {
    
    /**
     * Termo de busca para título
     */
    private String q;
    
    /**
     * ID do contrato
     */
    private Long contratoId;
    
    /**
     * ID da regional
     */
    private Long regionalId;
    
    /**
     * Público alvo (INTERNO/EXTERNO)
     */
    private String publico;
    
    /**
     * ID da diretoria
     */
    private Long diretoriaId;
    
    /**
     * ID da superintendência
     */
    private Long superintendenciaId;
    
    /**
     * ID do projeto
     */
    private Long projetoId;
    
    /**
     * IDs das filiais HCM
     */
    private List<Long> filialHcmId;  // Changed to List<Long>
    
    /**
     * Verifica se há algum filtro ativo
     */
    public boolean hasFilters() {
        return q != null && !q.isBlank() ||
               contratoId != null ||
               regionalId != null ||
               publico != null && !publico.isBlank() ||
               diretoriaId != null ||
               superintendenciaId != null ||
               projetoId != null ||
               (filialHcmId != null && !filialHcmId.isEmpty());  // Updated condition
    }
    
    /**
     * Verifica se o filtro de busca por texto está ativo
     */
    public boolean hasSearchTerm() {
        return q != null && !q.isBlank();
    }
    
    /**
     * Verifica se o filtro de público está ativo
     */
    public boolean hasPublicoFilter() {
        return publico != null && !publico.isBlank();
    }
}