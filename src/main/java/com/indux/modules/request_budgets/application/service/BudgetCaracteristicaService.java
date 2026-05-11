package com.indux.modules.request_budgets.application.service;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.request_budgets.domain.model.BudgetCaracteristica;
import com.indux.modules.request_budgets.domain.repository.BudgetCaracteristicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetCaracteristicaService {

    private final BudgetCaracteristicaRepository budgetCaracteristicaRepository;

    public BudgetCaracteristica createCaracteristica(BudgetCaracteristica caracteristica) {
        return budgetCaracteristicaRepository.save(caracteristica);
    }

    public List<BudgetCaracteristica> getAllCaracteristicas() {
        return budgetCaracteristicaRepository.findAll();
    }

    public BudgetCaracteristica getCaracteristicaById(String id) {
        return budgetCaracteristicaRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Característica não encontrada"));
    }

    public void deleteCaracteristica(String id) {
        if (!budgetCaracteristicaRepository.existsById(id)) {
            throw new ModuleNotFoundFailure("Característica não encontrada");
        }
        budgetCaracteristicaRepository.deleteById(id);
    }
}

