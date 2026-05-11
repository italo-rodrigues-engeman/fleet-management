package com.indux.modules.organization_chart.domain.entities.jpa;

import com.indux.core.domain.model.employee.Filial;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name="tb_organograma_projeto")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @OneToOne
    @JoinColumn(name = "mega", nullable = false)
    private MegaEntity mega;

    @ManyToOne
    @JoinColumn(name = "hcm", nullable = false)
    private HcmEntity hcm;

    @ManyToMany
    @JoinTable(
        name = "tb_organograma_projeto_filial",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "filial_id")
    )
    private List<FilialHcmEntity> filial;


    @ManyToOne
    @JoinColumn(name = "subordinado", nullable = true)
    private OrganizationEntity subordinate;

    @ManyToOne
    @JoinColumn(name = "contrato", nullable = true)
    private ContractEntity contract;

    @Column(name = "ativo", nullable = true)
    private boolean ativo;
    
    @Column(name = "tipo", nullable = true)
    private String type;
    
    @Column(name = "filial_mega", insertable = false, updatable = false)
    private Long filialMegaId;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "filial_mega", referencedColumnName = "codigo_filial", nullable = true)
    private Filial branch;
}
