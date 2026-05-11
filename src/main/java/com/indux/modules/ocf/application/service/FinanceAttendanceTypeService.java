package com.indux.modules.ocf.application.service;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ocf.application.dto.AttendanceTypeCreateRequest;
import com.indux.modules.ocf.domain.model.FinanceAttendanceType;
import com.indux.modules.ocf.domain.model.FinanceOccurrenceComplaintType;
import com.indux.modules.ocf.domain.repository.FinanceAttendanceTypeRepository;
import com.indux.modules.ocf.domain.repository.FinanceOccurrenceComplaintTypeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FinanceAttendanceTypeService {
    private final FinanceAttendanceTypeRepository attendanceRepository;
    private final FinanceOccurrenceComplaintTypeRepository complaintRepository;

    public FinanceAttendanceTypeService(FinanceAttendanceTypeRepository attendanceRepository,
                                        FinanceOccurrenceComplaintTypeRepository complaintRepository) {
        this.attendanceRepository = attendanceRepository;
        this.complaintRepository = complaintRepository;
    }

    public List<FinanceAttendanceType> createAttendanceTypes(AttendanceTypeCreateRequest request) {
        List<FinanceAttendanceType> created = new ArrayList<>();
        List<String> names = request.allNames();
        if (names.isEmpty()) {
            return created;
        }

        for (String nome : names) {
            if (nome == null || nome.isBlank()) continue;
            FinanceAttendanceType entity = FinanceAttendanceType.builder()
                    .name(nome.trim())
                    .build();
            created.add(attendanceRepository.save(entity));
        }

        // Relacionar com o tipo de reclamação, se informado
        if (request.ref() != null) {
            FinanceOccurrenceComplaintType complaint = complaintRepository.findById(request.ref())
                    .orElseThrow(() -> new ModuleNotFoundFailure("Tipo de reclamação não encontrado"));
            var list = complaint.getAttendanceTypes();
            list.addAll(created);
            complaint.setAttendanceTypes(list);
            complaintRepository.save(complaint);
        }

        return created;
    }
}

