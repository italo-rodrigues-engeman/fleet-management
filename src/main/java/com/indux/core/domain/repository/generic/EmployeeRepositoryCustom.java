package com.indux.core.domain.repository.generic;

import com.indux.core.application.dto.generic.EmployeeDTO;

import java.util.List;

public interface EmployeeRepositoryCustom {
    List<EmployeeDTO> search(String filter, boolean showBank, boolean showDemitido);
}
