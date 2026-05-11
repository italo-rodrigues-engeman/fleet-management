package com.indux.core.domain.model.employee;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Table(name = "tb_endereco_funcionarios")
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column(name = "matricula")
    private String registration;
    @Column(name = "data")
    private Date date;
    @Column(name = "tipo_logradouro")
    private String type;
    @Column(name = "rua")
    private String street;
    @Column(name = "numero")
    private String number;
    @Column(name = "complemento")
    private String complement;
    @Column(name = "bairro")
    private String neighborhood;
    @Column(name = "cidade")
    private String city;
    @Column(name = "estado")
    private String state;
    private String cep;
}
