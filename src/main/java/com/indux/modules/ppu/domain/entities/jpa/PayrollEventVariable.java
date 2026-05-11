package com.indux.modules.ppu.domain.entities.jpa;

import com.indux.modules.ppu.infra.persistence.mongo.YearMonthAttributeConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.time.YearMonth;

@Entity
@Table(name = "tb_eventos_variaveis")
@Data
public class PayrollEventVariable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", nullable = false)
    private Integer code;

    @Column(name = "nome_evento", nullable = false)
    private String name;

    @Column(name = "competencia", nullable = false)
    @Convert(converter = YearMonthAttributeConverter.class)
    private YearMonth competence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evento_base", nullable = false)
    private PayrollEvents payrollEvent;
}
