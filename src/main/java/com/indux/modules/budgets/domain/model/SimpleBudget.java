package com.indux.modules.budgets.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import com.indux.core.domain.model.modules.form.StepLog;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "simple_budgets")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SimpleBudget {
    @Id
    private String id;
    private Long clienteId;
    private String clienteNome;
    private Long setorId;
    private String acOs;
    private String setor;
    private String orcamentista;
    private Integer oportunidade;
    @Indexed(unique = true)
    private String nomeOportunidade;
    private String status;
    private String observacao;

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getObservacao() {
        return this.observacao;
    }
    
    private Integer step;
    private List<StepLog> stepLog = new ArrayList<>();
    private List<SimpleBudgetItem> itens = new ArrayList<>();
    @Builder.Default
    private List<SimpleBudgetServiceItem> servicos = new ArrayList<>();
}
