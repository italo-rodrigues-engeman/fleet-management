package com.indux.core.domain.repository.cbo;

import com.indux.core.application.dto.cbo.FilterFuncao;
import com.indux.core.domain.model.cbo.FuncaoHCM;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FuncaoRepositoryCustom {
    Page<FuncaoHCM> getWithFilterFuncao(FilterFuncao filter, Pageable pageable);
}
