package com.indux.modules.modulo_mega.application.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class ItemFilter {
	
    private List<Integer> codigoItem;
    private String nomeItem;
	
    private List<Integer> codigoGrupo;
    private Integer codigoFornecedor;
    private Integer nomeFornecedor;
    private String nomeGrupo;
    
    private String statusItem;
    private BigDecimal precoMedMin;
    private BigDecimal precoMedMax;
    private Integer qtdePedidosMin;
    private Integer qtdePedidosMax;
    private BigDecimal qtdeComprasMin;
    private BigDecimal qtdeComprasMax;
    

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate cadastroStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate cadastroEndDate;

    private String tipoItem;
    private String unidadeMedida;
    
	

}