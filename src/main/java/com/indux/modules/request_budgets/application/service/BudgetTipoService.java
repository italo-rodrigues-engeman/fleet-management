package com.indux.modules.request_budgets.application.service;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.request_budgets.domain.model.BudgetTipo;
import com.indux.modules.request_budgets.domain.repository.BudgetTipoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BudgetTipoService {

    private final BudgetTipoRepository budgetTipoRepository;

    public BudgetTipo createTipo(BudgetTipo tipo) {
        return budgetTipoRepository.save(tipo);
    }

    public List<BudgetTipo> getAllTipos() {
        return budgetTipoRepository.findAll();
    }

    public BudgetTipo getTipoById(String id) {
        return budgetTipoRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Tipo não encontrado"));
    }

    public void deleteTipo(String id) {
        if (!budgetTipoRepository.existsById(id)) {
            throw new ModuleNotFoundFailure("Tipo não encontrado");
        }
        budgetTipoRepository.deleteById(id);
    }
}

