package com.indux.modules.modulo_mega.persistence.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SupplierDocument{
    private Integer supplierCod;
    private String supplierName;
    private String cnpj;
}
