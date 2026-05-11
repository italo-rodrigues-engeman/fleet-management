package com.indux.modules.purchase_occurrence.application;

import com.indux.core.domain.model.modules.form.LogFilter;
import com.indux.core.domain.service.occurrence.log.OccurrenceLogService;
import com.indux.modules.purchase_occurrence.domain.entities.log.PurchaseOccurrenceLog;
import com.indux.modules.purchase_occurrence.domain.repository.PurchaseOccurrenceLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaseOccurrenceLogService implements OccurrenceLogService<PurchaseOccurrenceLog> {
    private final PurchaseOccurrenceLogRepository repository;

    public PurchaseOccurrenceLogService(PurchaseOccurrenceLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void delete(PurchaseOccurrenceLog object) {
        repository.delete((object));
    }

    @Override
    public void save(PurchaseOccurrenceLog object) {
    repository.save(object);
    }

    @Override
    public List<PurchaseOccurrenceLog> findByOccurrenceID(String occurrenceId) {
        return List.of();
    }

    @Override
    public List<PurchaseOccurrenceLog> filter(LogFilter filter) {
        return List.of();
    }
}
