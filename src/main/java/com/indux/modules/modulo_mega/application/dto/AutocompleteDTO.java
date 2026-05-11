package com.indux.modules.modulo_mega.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor 
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AutocompleteDTO {
    
    private String id;
    private String label;
    private String status;      
    private Boolean statusGroup; 
    
    public AutocompleteDTO(Object id, String label) {
        this.id = id != null ? id.toString() : null;
        this.label = label;
    }

    public AutocompleteDTO(Object id, String label, String status) {
        this.id = id != null ? id.toString() : null;
        this.label = label; 
        this.status = status;
    }

    /**
     * Novo construtor para satisfazer a busca de Grupos
     * O Hibernate passará: [Integer, String, Boolean]
     */
    public AutocompleteDTO(Integer id, String label, Boolean statusGroup) {
        this.id = id != null ? id.toString() : null;
        this.label = label;
        this.statusGroup = statusGroup;
        // Se quiser que o campo 'status' (String) também seja preenchido:
        this.status = statusGroup != null ? (statusGroup ? "Ativo" : "Inativo") : null;
    }
}