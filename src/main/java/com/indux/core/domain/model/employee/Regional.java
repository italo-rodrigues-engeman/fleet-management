package com.indux.core.domain.model.employee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tb_regional")
public class Regional {
    @Id
    private Long id;
    @Column(name = "filial")
    private Integer filial;

    @Column(name = "regional")
    private String regional;

    @Column(name = "matricula_responsavel")
    private String matriculaResponsavel;

    @OneToMany(mappedBy = "regional", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Filial> filiais = new ArrayList<>();
} 