package com.indux.core.domain.model.employee;

import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import jakarta.persistence.*;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_cargos")
public class Cargo {
    @Id
    private Long id;

    @Column(name = "id_hcm")
    private String idHcm;

    @Column(name = "nome_cargo")
    private String nameTitle;

    @Column(name = "nome_cargo_cbo")
    private String nameTitleCbo;

    @Column(name = "cod_cbo")
    private String codeCbo;

    @ManyToMany
    @JoinTable(name = "tb_cargo_filial", joinColumns = @JoinColumn(name = "cargo_id"), inverseJoinColumns = @JoinColumn(name = "filial_id_hcm"))
    private List<FilialHcmEntity> filial;
}