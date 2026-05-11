package com.indux.modules.ocf.application.service;

import com.indux.core.domain.model.modules.form.LogFilter;
import com.indux.core.domain.service.occurrence.log.OccurrenceLogService;
import com.indux.modules.ocf.domain.model.log.FinanceOccurrenceLog;
import com.indux.modules.ocf.domain.repository.log.FinanceOccurrenceLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FinanceOccurrenceLogService implements OccurrenceLogService<FinanceOccurrenceLog> {
    private final FinanceOccurrenceLogRepository repository;

    public FinanceOccurrenceLogService(FinanceOccurrenceLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public void delete(FinanceOccurrenceLog object) {
        repository.delete(object);
    }

    @Override
    public void save(FinanceOccurrenceLog object) {
        repository.save(object);
    }

    @Override
    public List<FinanceOccurrenceLog> findByOccurrenceID(String occurrenceId) {
        return repository.findAllByOccurrenceId(occurrenceId);
    }

    @Override
    public List<FinanceOccurrenceLog> filter(LogFilter filter) {
        return List.of();
    }
}
