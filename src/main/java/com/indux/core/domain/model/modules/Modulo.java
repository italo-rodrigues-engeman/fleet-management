package com.indux.core.domain.model.modules;

import com.indux.core.application.dto.module.ModulePermissionInput;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.*;

@Data
@Entity
@Builder
@Table(name = "tb_modulos", uniqueConstraints = @UniqueConstraint(columnNames = "nome"))
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)

public class Modulo {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "modulo_id", nullable = false, updatable = false)
    private UUID id;
    @Column(name = "nome", unique = true, nullable = false)
    private String name;
    @Column(name = "descricao", nullable = true)
    private String description;
    @Column(name = "qtd_etapas")
    private Integer stepsQuantity;
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "tb_modulos_etapas_join", joinColumns = @JoinColumn(name = "modulo_id"), inverseJoinColumns = @JoinColumn(name = "etapa_id"))
    private Set<StepModule> configEtapas = new HashSet<>();
    @OneToMany(mappedBy = "modulo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    @OrderBy("responsable ASC")
    private List<ModulePermission> permissoes = new ArrayList<>();
    @ElementCollection
    @CollectionTable(name = "tb_modulos_gerentes", joinColumns = @JoinColumn(name = "modulo_id"))
    @Column(name = "gerente_id")
    @Builder.Default
    private Set<UUID> gerentes = new HashSet<>();
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    @Column(name = "desativado")
    private boolean desativado;
    @ElementCollection
    @CollectionTable(name = "tb_modulos_setores", joinColumns = @JoinColumn(name = "modulo_id"))
    @Column(name = "setor")
    @Builder.Default
    private Set<Integer> setores = new HashSet<>();

    public void addPermissao(ModulePermission perm) {
        perm.setModulo(this);
        permissoes.add(perm);
    }

    public void addGerente(UUID gerenteId) {
        gerentes.add(gerenteId);
    }

    public void removeGerente(UUID gerenteId) {
        gerentes.remove(gerenteId);
    }

    public boolean isGerente(UUID userId) {
        return gerentes.contains(userId);
    }

    /**
     * Atualiza ou cria permissão para o usuário neste módulo.
     * Se já existir, altera os campos; senão, adiciona nova.
     */
    public void updateOrCreatePermission(UUID userId, ModulePermissionInput in) {
        ModulePermission perm = permissoes.stream()
                .filter(p -> p.getResponsable().equals(userId))
                .findFirst()
                .orElseGet(() -> {
                    ModulePermission novo = ModulePermission.builder()
                            .responsable(userId)
                            .build();
                    novo.setModulo(this);
                    permissoes.add(novo);
                    return novo;
                });
        perm.setGroup(in.isGroup());
        perm.setRegionais(Objects.requireNonNullElse(in.regionais(), Set.of()));
        perm.setStepsAllowed(new ArrayList<>(Objects.requireNonNullElse(in.etapasResponsaveis(), Set.of())));
        perm.setProjetos(in.projetos());
        perm.setActions(in.acoes());
    }
}