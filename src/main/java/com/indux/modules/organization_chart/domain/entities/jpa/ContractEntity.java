package com.indux.modules.organization_chart.domain.entities.jpa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.indux.core.domain.model.employee.Filial;
import com.indux.modules.organization_chart.domain.entities.models.ContractType;
import com.indux.modules.organization_chart.domain.entities.models.OrganizationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="tb_organograma_contrato")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ContractEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome", nullable = false)
    private String name;

    @Column(name = "apelido", nullable = true)
    private String nickname;

    @Column(name = "os", nullable = false)
    private String os;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "subordinado", nullable = false)
    private OrganizationEntity subordinate;

    @Column(name = "hierarquia",nullable = false)
    private OrganizationType hierarchy;

    @Column(name = "tipo", nullable = false)
    private ContractType type;

    @Column(name = "filial_mega", insertable = false, updatable = false)
    private Long filialMegaId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "filial_mega", referencedColumnName = "codigo_filial", nullable = true)
    private Filial branch;
}
