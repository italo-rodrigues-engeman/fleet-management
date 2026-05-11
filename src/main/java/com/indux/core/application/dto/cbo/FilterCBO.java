package com.indux.core.application.dto.cbo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilterCBO {
    private List<String> codCBO;
    private List<String> nomeCBO;
    private List<Long> diretoriaId;
    private List<Long> superintendenciaId;
    private List<Long> regionalId;
    private List<Long> setorId;
    private List<Long> contratoId;
    private List<Long> projetoId;
    private List<Integer> filialIdHcm;
    private List<String> idHCM;
    private Boolean resumo;
    private String search;
}
