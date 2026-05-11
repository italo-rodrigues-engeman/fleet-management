package com.indux.modules.ppu.application.mapper;

import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOSyncLog;
import com.indux.modules.ppu.presentation.dtos.RDOSyncLogResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RDOSyncLogMapper {

    public RDOSyncLogResponse toResponse(RDOSyncLog domain) {
        if (domain == null) return null;
        return RDOSyncLogResponse.builder()
                .rdoId(domain.getRdoId())
                .syncStartDate(domain.getSyncStartDate())
                .syncEndDate(domain.getSyncEndDate())
                .success(domain.getSuccess())
                .totalUpdated(domain.getTotalUpdated())
                .totalErrors(domain.getTotalErrors())
                .employeesUpdated(mapEmployees(domain.getEmployeesUpdated()))
                .errors(domain.getErrors())
                .user(mapUser(domain.getUser()))
                .build();
    }

    private List<RDOSyncLogResponse.EmployeeStatusUpdateResponse> mapEmployees(List<RDOSyncLog.EmployeeStatusUpdate> employees) {
        if (employees == null) return List.of();
        return employees.stream()
                .map(e -> RDOSyncLogResponse.EmployeeStatusUpdateResponse.builder()
                        .registration(e.getRegistration())
                        .name(e.getName())
                        .previousStatus(e.getPreviousStatus())
                        .newStatus(e.getNewStatus())
                        .updated(e.getUpdated())
                        .build())
                .toList();
    }

    private RDOSyncLogResponse.UserResponse mapUser(RDOLoggerUser user) {
        return RDOSyncLogResponse.UserResponse.builder()
                .id(user.getId().toString())
                .name(user.getName())
                .build();
    }
}
