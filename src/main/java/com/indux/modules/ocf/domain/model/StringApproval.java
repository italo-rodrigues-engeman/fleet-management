package com.indux.modules.ocf.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "string_approvals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StringApproval {
    @Id
    private String id;
    
    private List<String> aprovado;
    private List<String> rejeitado;
    
    public StringApproval(List<String> aprovado, List<String> rejeitado) {
        this.aprovado = aprovado;
        this.rejeitado = rejeitado;
    }
}

