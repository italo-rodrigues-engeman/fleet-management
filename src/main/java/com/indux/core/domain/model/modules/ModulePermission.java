package com.indux.core.domain.model.modules;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.*;

@Data
@Entity
@Builder
@Table(
        name = "tb_permissoes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_modulo_responsavel",
                columnNames = {"modulo_id", "responsavel"}
        )
)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ModulePermission {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "permissao_id", nullable = false, updatable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @EqualsAndHashCode.Include
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modulo_id", nullable = false)
    @JsonIgnore
    private Modulo modulo;

    @EqualsAndHashCode.Include
    @Column(name = "responsavel", nullable = false)
    private UUID responsable;

    @Transient
    private String responsableName;

    @Column(name = "grupo", nullable = false)
    private boolean isGroup;

    @ElementCollection
    @CollectionTable(
            name = "tb_permissoes_filiais",
            joinColumns = @JoinColumn(name = "permissao_id")
    )
    @Column(name = "filial_id")
    @Deprecated
    private Set<Integer> filiais = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "tb_permissoes_contratos",
            joinColumns = @JoinColumn(name = "permissao_id")
    )
    @Column(name = "contrato_id")
    @Deprecated
    private Set<Integer> contratos = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "tb_permissoes_etapas",
            joinColumns = @JoinColumn(name = "permissao_id")
    )
    @Column(name = "etapa")
    private List<Integer> stepsAllowed = new ArrayList<>();

    @ElementCollection
    @CollectionTable(
            name = "tb_permissoes_acoes",
            joinColumns = @JoinColumn(name = "permissao_id")
    )
    @Column(name = "acao_id")
    private Set<Integer> actions = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "tb_permissoes_projetos",
            joinColumns = @JoinColumn(name = "permissao_id")
    )
    @Column(name = "projeto_id")
    private Set<Integer> projetos = new HashSet<>();


    @ElementCollection
    @CollectionTable(
            name = "tb_permissoes_regionais",
            joinColumns = @JoinColumn(name = "permissao_id")
    )
    @Column(name = "regional_id")
    private Set<Integer> regionais = new HashSet<>();

}