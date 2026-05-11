package com.indux.modules.alpar.application.service;

import com.indux.modules.alpar.persistence.model.AlparAuditLog;
import com.indux.modules.alpar.persistence.repository.AlparAuditRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AlparAuditService {

    private final AlparAuditRepository repository;

    public AlparAuditService(AlparAuditRepository repository) {
        this.repository = repository;
    }

    @Async
    public void record(AlparAuditLog log) {
        repository.save(log);
    }
}
