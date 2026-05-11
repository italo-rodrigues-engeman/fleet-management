package com.indux.core.domain.model.employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "tb_cargos")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeePosition {
    @Id
    @Column(name = "id")
    private Long id;
    @Column(name = "id_hcm")
    private String idHCM;
    @Column(name = "nome_cargo")
    private String name;
    @Column(name = "cod_cbo")
    private String codeCbo;

}
