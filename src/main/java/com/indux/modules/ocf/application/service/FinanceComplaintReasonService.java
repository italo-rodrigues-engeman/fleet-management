package com.indux.modules.ocf.application.service;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ocf.application.dto.ComplaintReasonCreateRequest;
import com.indux.modules.ocf.domain.model.FinanceComplaintReason;
import com.indux.modules.ocf.domain.model.FinanceOccurrenceComplaintType;
import com.indux.modules.ocf.domain.repository.FinanceComplaintReasonRepository;
import com.indux.modules.ocf.domain.repository.FinanceOccurrenceComplaintTypeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FinanceComplaintReasonService {
    private final FinanceComplaintReasonRepository reasonRepository;
    private final FinanceOccurrenceComplaintTypeRepository complaintRepository;

    public FinanceComplaintReasonService(FinanceComplaintReasonRepository reasonRepository,
                                         FinanceOccurrenceComplaintTypeRepository complaintRepository) {
        this.reasonRepository = reasonRepository;
        this.complaintRepository = complaintRepository;
    }

    public List<FinanceComplaintReason> createReasons(ComplaintReasonCreateRequest request) {
        List<FinanceComplaintReason> created = new ArrayList<>();
        List<String> names = request.allNames();
        if (names.isEmpty()) return created;

        for (String name : names) {
            if (name == null || name.isBlank()) continue;
            FinanceComplaintReason r = FinanceComplaintReason.builder().name(name.trim()).build();
            created.add(reasonRepository.save(r));
        }

        if (request.ref() != null) {
            FinanceOccurrenceComplaintType complaint = complaintRepository.findById(request.ref())
                    .orElseThrow(() -> new ModuleNotFoundFailure("Tipo de reclamação não encontrado"));
            var list = complaint.getReasons();
            list.addAll(created);
            complaint.setReasons(list);
            complaintRepository.save(complaint);
        }
        return created;
    }

    public List<FinanceComplaintReason> findAll() {
        return reasonRepository.findAll();
    }
}

