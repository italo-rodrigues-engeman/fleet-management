package com.indux.core.domain.model.employee;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "tb_filiais")
public class Filial {
    @Id
    @Column(name = "codigo_filial")
    private Long branchId;
    @Column(name = "numemp")
    private Long companyId;
    @Column(name = "razao_social")
    private String corporateName;
    @Column(name = "nome_filial")
    private String branchName;
    @Column(name = "matricula_responsavel_filial")
    private String matricula_responsavel_filial;
    @Column(name = "nome_responsavel_filial")
    private String nome_responsavel_filial;
    @Column(name = "endereco")
    private String endereco;
    @Column(name = "cnpj")
    private String cnpj;
    @Column(name = "inscricao_municipal")
    private String municipalRegistration;
    @Column(name = "filial_municipio")
    private String branchCity;
    
    @Column(name = "filial_mega")
    private Integer filialMega;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "Id_regional", referencedColumnName = "id")
    @JsonIgnore
    private Regional regional;
}
