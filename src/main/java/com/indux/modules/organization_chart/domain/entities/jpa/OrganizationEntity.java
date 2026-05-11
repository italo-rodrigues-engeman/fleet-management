package com.indux.modules.organization_chart.domain.entities.jpa;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.indux.core.domain.model.employee.Employee;
import com.indux.modules.organization_chart.domain.entities.models.OrganizationSubType;
import com.indux.modules.organization_chart.domain.entities.models.OrganizationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="tb_organograma")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "cargo", nullable = false)
    private String position;

    @Column(name = "sigla", nullable = false)
    private String acronym;

    @ManyToOne
    @JoinColumn(name = "colaborador", nullable = false)
    private Employee collaborator;

    @Column(name = "observacao", nullable = true)
    private String observation;

    @Column(name = "ativo",nullable = false)
    private boolean  active;

    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "subordinado", nullable = true)
    private OrganizationEntity subordinate;

    @Column(name = "hierarquia",nullable = true)
    private OrganizationType hierarchy;

    @Column(name = "tipo", nullable = false)
    private OrganizationType type;

    @Column(name = "sub_tipo", nullable = true)
    private OrganizationSubType subType;
}
