package com.indux.modules.ocf.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_tipos_reclamacao_ff")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FinanceOccurrenceComplaintType {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private long id;
    @Column(nullable = false, unique = true, name = "nome")
    private String name;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "tb_tipos_join_ff",
            joinColumns = @JoinColumn(name = "tipo_reclamacao_id"),
            inverseJoinColumns = @JoinColumn(name = "tipo_ocorrencia_id")
    )
    @OrderBy("id ASC")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<FinanceOccurrenceType> occurrences = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "tb_tipos_atendimento_join_ff",
            joinColumns = @JoinColumn(name = "tipo_reclamacao_id"),
            inverseJoinColumns = @JoinColumn(name = "tipo_atendimento_id")
    )
    @OrderBy("id ASC")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<FinanceAttendanceType> attendanceTypes = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "tb_motivos_join_ff",
            joinColumns = @JoinColumn(name = "tipo_reclamacao_id"),
            inverseJoinColumns = @JoinColumn(name = "tipo_motivo_id")
    )
    @OrderBy("id ASC")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<FinanceComplaintReason> reasons = new ArrayList<>();

}
