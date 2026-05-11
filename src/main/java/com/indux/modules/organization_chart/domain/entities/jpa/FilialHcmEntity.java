package com.indux.modules.organization_chart.domain.entities.jpa;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name="tb_filiais_hcm")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilialHcmEntity {

    @Id
    @Column(name = "filial_id")
    private Integer filialId;

    @Column(name = "nome_filial")
    private String nomeFilial;

    @ManyToMany(mappedBy = "filial")
    @JsonIgnore
    private List<ProjectEntity> project;

    @ManyToOne
    @JoinTable(
        name = "tb_organograma_projeto_filial_cc_mega",
        joinColumns = @JoinColumn(name = "filial_id"),
        inverseJoinColumns = @JoinColumn(name = "cc_mega_id")
    )
    private CCMegaEntity ccMega;

    @Column(name = "status")
    private Boolean status;

}
