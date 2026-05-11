package com.indux.modules.faq.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerguntaFiltrosDTO {
    
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
     * ID do tema
     */
    private Long temaId;
    
    /**
     * ID do setor
     */
    private Long setorId;
    
    /**
     * Categorias da pergunta
     */
    
    private List<String> categoria;
    
    /**
     * Status da pergunta
     */
    private String status;
    
    /**
     * Data de início para filtro por período
     */
    private LocalDate dataInicio;
    
    /**
     * Data de fim para filtro por período
     */
    private LocalDate dataFim;
    
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
               temaId != null ||
               setorId != null ||
               categoria != null && !categoria.isEmpty() ||
               status != null && !status.isBlank() ||
               dataInicio != null ||
               dataFim != null ||
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
    
    /**
     * Verifica se o filtro de categoria está ativo
     */
    public boolean hasCategoriaFilter() {
        return categoria != null && !categoria.isEmpty();
    }
    
    /**
     * Verifica se o filtro de status está ativo
     */
    public boolean hasStatusFilter() {
        return status != null && !status.isBlank();
    }
    
    /**
     * Verifica se há filtro por período
     */
    public boolean hasDateFilter() {
        return dataInicio != null || dataFim != null;
    }
}