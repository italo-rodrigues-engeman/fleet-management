package com.indux.core.application.service.employee;

import com.indux.core.domain.model.employee.EmployeePosition;
import com.indux.core.domain.repository.employee.EmployeePositionRepository;
import com.indux.core.domain.service.generic.GetPositionService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Lazy
public class GetPositionServiceImpl implements GetPositionService<EmployeePosition> {
    private final EmployeePositionRepository repository;

    public GetPositionServiceImpl(EmployeePositionRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<String> findHcmCodesByCboCode(String cboCode) {
        var response = repository.findByCodeCbo(cboCode);
        List<String> hcmCodes = response.stream().map(EmployeePosition::getIdHCM).toList();
        return hcmCodes;
    }

    @Override
    public List<EmployeePosition> findByCboCode(String cboCode) {
        var response = repository.findByCodeCbo(cboCode);
        if (response != null) {
            return response;
        }
        return List.of();
    }

    @Override
    public List<EmployeePosition> findByCboCodes(List<String> cboCodes) {
        var response = repository.findByCodeCboIn(cboCodes);
        if (response != null) {
            return response;
        }
        return List.of();
    }

    @Override
    public List<String> findHCMCodesByCboCodes(List<String> cboCodes) {
        var response = repository.findByCodeCboIn(cboCodes);
        if (response != null) {
            List<String> hcmCodes = response.stream().map(EmployeePosition::getIdHCM).toList();
            return hcmCodes;
        }
        return List.of();
    }
}
