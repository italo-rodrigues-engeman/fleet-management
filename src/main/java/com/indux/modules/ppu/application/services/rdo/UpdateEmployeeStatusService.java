package com.indux.modules.ppu.application.services.rdo;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.application.services.rdo.helper.LoggerUserHelper;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLogger;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerType;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOSyncLog;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.mio.EmployeeBoardingETL;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import com.indux.modules.ppu.infra.mio.dto.MioResponse;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UpdateEmployeeStatusService {
    
    private final RDORepository rdoRepository;
    private final EmployeeBoardingETL employeeBoardingETL;
    
    public UpdateEmployeeStatusService(
            RDORepository rdoRepository,
            EmployeeBoardingETL employeeBoardingETL) {
        this.rdoRepository = rdoRepository;
        this.employeeBoardingETL = employeeBoardingETL;
    }
    
    public RDOSyncLog updateEmployeeStatus(String rdoId, JwtAuthenticationToken token) {
        LocalDateTime syncStartDate = LocalDateTime.now();
        
        RDOEntity rdo = rdoRepository.findById(rdoId)
                .orElseThrow(() -> new ModuleNotFoundFailure("RDO não encontrada com ID: " + rdoId));
        
        if (rdo.getDate() == null) {
            RDOSyncLog errorLog = RDOSyncLog.builder()
                    .rdoId(rdoId)
                    .syncStartDate(syncStartDate)
                    .syncEndDate(LocalDateTime.now())
                    .success(false)
                    .totalUpdated(0)
                    .totalErrors(1)
                    .employeesUpdated(new ArrayList<>())
                    .errors(List.of("RDO não possui data configurada"))
                    .build();
            return errorLog;
        }
        
        RDOLoggerUser loggerUser = LoggerUserHelper.createUser(token);
        
        RDOSyncLog syncLog = RDOSyncLog.builder()
                .rdoId(rdoId)
                .syncStartDate(syncStartDate)
                .user(loggerUser)
                .employeesUpdated(new ArrayList<>())
                .errors(new ArrayList<>())
                .totalUpdated(0)
                .totalErrors(0)
                .build();
        
        try {
            String rdoDate = rdo.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
            
            List<BoardedEmployee> boardedEmployees = employeeBoardingETL.fetchBoardedEmployees(
                    null, 
                    rdo.getPlatform(), 
                    rdoDate, 
                    rdoDate
            );
            
            List<MioResponse> statusResponses = 
                    employeeBoardingETL.fetchBoardedEmployeesStatus(rdoDate, rdoDate);
            
            Map<String, String> statusByRegistration = statusResponses.stream()
                    .filter(mr -> mr.status() != null && !mr.status().isBlank())
                    .collect(Collectors.groupingBy(
                            MioResponse::matricula,
                            Collectors.collectingAndThen(
                                    Collectors.toList(),
                                    responses -> selectPriorityEvent(responses)
                            )
                    ));

            if (rdo.getServices() == null || rdo.getServices().isEmpty()) {
                syncLog.setSuccess(false);
                syncLog.setSyncEndDate(LocalDateTime.now());
                syncLog.getErrors().add("RDO não possui serviços para atualizar");
                return syncLog;
            }

            int updatedCount = 0;
            int errorCount = 0;

            for (RDOServiceEntity service : rdo.getServices()) {
                if (service == null || service.getRegistration() == null) {
                    continue;
                }

                if (Boolean.TRUE.equals(service.getDisposicao())) {
                    continue;
                }

                String registration = service.getRegistration();
                String previousStatus = service.getStatusEmployee();
                String newStatus = statusByRegistration.get(registration);

                RDOSyncLog.EmployeeStatusUpdate employeeUpdate = RDOSyncLog.EmployeeStatusUpdate.builder()
                        .registration(registration)
                        .name(service.getName())
                        .previousStatus(previousStatus)
                        .newStatus(newStatus)
                        .updated(false)
                        .build();

                if (newStatus != null && !newStatus.equals(previousStatus)) {
                    service.setNewStatusEmployee(newStatus);
                    employeeUpdate.setUpdated(true);
                    updatedCount++;
                    syncLog.getEmployeesUpdated().add(employeeUpdate);
                } else if (newStatus == null) {
                    errorCount++;
                    syncLog.getErrors().add("Status não encontrado no MIO para colaborador: " + registration);
                }
            }

            if (updatedCount > 0) {
                RDOLogger syncLogger = RDOLogger.builder()
                        .wasAnalyzed(true)
                        .isInfoCorrect(true)
                        .action(RDOLoggerType.EDIT)
                        .justification("Sincronização de status de colaboradores do MIO realizada. " +
                                updatedCount + " colaborador(es) atualizado(s).")
                        .date(LocalDateTime.now())
                        .sector("OP")
                        .user(loggerUser)
                        .colaboradoresAtualizados(syncLog.getEmployeesUpdated())
                        .build();

                List<RDOLogger> loggers = rdo.getLoggers() != null
                        ? new ArrayList<>(rdo.getLoggers())
                        : new ArrayList<>();
                loggers.add(syncLogger);
                rdo.setLoggers(loggers);

                rdoRepository.save(rdo);
            }

            syncLog.setSuccess(true);
            syncLog.setTotalUpdated(updatedCount);
            syncLog.setTotalErrors(errorCount);

        } catch (IOException e) {
            syncLog.setSuccess(false);
            syncLog.setTotalErrors(syncLog.getTotalErrors() + 1);
            syncLog.getErrors().add("Erro ao buscar dados do MIO: " + e.getMessage());
        } catch (Exception e) {
            syncLog.setSuccess(false);
            syncLog.setTotalErrors(syncLog.getTotalErrors() + 1);
            syncLog.getErrors().add("Erro inesperado: " + e.getMessage());
        } finally {
            syncLog.setSyncEndDate(LocalDateTime.now());
        }

        return syncLog;
    }

    /**
     * Seleciona o evento com maior prioridade quando há múltiplos eventos para a mesma matrícula.
     * Prioridade: 1. Dobra, 2. Folga Indenizada, 3. Turma de Treinamento, 4. Outros
     */
    private String selectPriorityEvent(List<MioResponse> responses) {
        if (responses.isEmpty()) {
            return null;
        }

        if (responses.size() == 1) {
            return responses.get(0).status();
        }

        return responses.stream()
                .min(Comparator.comparingInt(this::getEventPriority))
                .map(MioResponse::status)
                .orElse(responses.get(0).status());
    }

    /**
     * Retorna a prioridade do evento. Menor número = maior prioridade.
     * 1 = Dobra
     * 2 = Folga Indenizada
     * 3 = Turma de Treinamento
     * 4+ = Outros eventos
     */
    private int getEventPriority(MioResponse response) {
        String evento = response.status();
        if (evento == null) {
            return Integer.MAX_VALUE;
        }
        
        String eventoUpper = evento.toUpperCase().trim();
        
        if (eventoUpper.contains("DOBRA")) {
            return 1;
        } else if (eventoUpper.contains("FOLGA INDENIZADA")) {
            return 2;
        } else if (eventoUpper.contains("TURMA DE TREINAMENTO")) {
            return 3;
        }
        
        return 4;
    }
}

