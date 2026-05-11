package com.indux.modules.modulo_mega.domain.entities.purchase_process;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Supplier {
    private Integer supplierCod;
    private String supplierName;
    private String cnpj;
}
