package com.indux.modules.ppu.application.services.ppu.available;

import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.clients.domain.model.Client;
import com.indux.modules.clients.domain.repository.ClientRepository;
import com.indux.modules.ppu.application.dtos.AuthorContext;
import com.indux.modules.ppu.application.services.rdo.RDOService;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.domain.strategy.LineStrategiesApplier;
import com.indux.modules.ppu.infra.mapper.BoardedEmployeeMapper;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import com.indux.modules.ppu.presentation.dtos.AvailablePeriod;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * <b>AvailablePeriodHandler</b> trata da adição de colaboradores a disposição
 * nos RDOs, porém, age depois do lançamento dos RDOS e não anteriormente, como
 * definido por contrato.
 * </p>
 * O AvailablePeriodHandler age posteriormente o lançamento do RDO - ação no
 * RDOEntity,
 * diferenciando de AvailableHandler, que é anteriormente - ação no PPUEntity.
 */
@Component()
public class AvailablePeriodHandler {
    private final RDORepository rdoRepository;
    private final PPURepository ppuRepository;
    private final BoardedEmployeeMapper boardedMapper;
    private final GetEmployeeUseCase getEmployeeUseCase;
    private final LineStrategiesApplier lineApplier;
    private final ClientRepository clientRepository;
    private final UserService userService;
    private static final String DEFAULT_DESCRIPTION = "A DISPOSIÇÃO POSTERIOR";
    private static final long MAX_DAYS_RANGE = 32;
    private static final int MAX_REGISTRATIONS = 100;


    public AvailablePeriodHandler(RDORepository rdoRepository, PPURepository ppuRepository,
                                  BoardedEmployeeMapper boardedMapper, GetEmployeeUseCase getEmployeeUseCase, LineStrategiesApplier lineApplier, ClientRepository clientRepository, UserService userService) {
        this.rdoRepository = rdoRepository;
        this.ppuRepository = ppuRepository;
        this.boardedMapper = boardedMapper;
        this.getEmployeeUseCase = getEmployeeUseCase;
        this.lineApplier = lineApplier;
        this.clientRepository = clientRepository;
        this.userService = userService;
    }

    //todo: amenizar a função, ainda tem muita atividade pra o mesma função
    public void addMultipleAvailablePeriods(AvailablePeriod request, String creatorId) {
        validateRequestVolume(request);
        var response = validateAndResolve(request);
        var creator = userService.getEmployeeFromUser(UUID.fromString(creatorId));
        var author = new AuthorContext(creator.getMatricula(), creator.getName());
        var employeesDTOs = getEmployeeUseCase.findByRegistrations(request.registrations());
        var boardedEmployee = boardedMapper.fromSimpleEmployeeDTOList(employeesDTOs);

        if (employeesDTOs.size() != request.registrations().size()) {
            throw new ModuleFailure("Um ou mais colaboradores não foram encontrados na base de dados.");
        }

        var rdos = rdoRepository.findAllByPlatformAndPpuIdAndDateBetween(request.platform(), request.ppuId(),
                request.start(), request.end());
        var rdoByDate = rdos.stream()
                .collect(Collectors.toMap(RDOEntity::getDate, Function.identity(), (a, b) -> a));
        var allDates = enumerateDates(request.start(), request.end());
        var rdosToSave = new ArrayList<RDOEntity>();

        for (var date : allDates) {
            var rdo = rdoByDate.get(date);
            if (rdo == null) {
                Client client = clientRepository.findById(response.ppu().getClientId()).orElse(null);
                var basicRDO = createBasicRDO(request,response.ppu(), date, author);
                basicRDO.setSequentialId(calculateNextSequenceId(request.platform(), request.ppuId(), date));
                var services = createRDOServices(boardedEmployee, response.serviceLine());
                basicRDO.setServices(services);
                basicRDO.setClientName(client != null ? client.getName() : "Cliente não informado");
                lineApplier.apply(basicRDO, response.ppu());
                rdosToSave.add(basicRDO);
            } else {
                var existingRegistrations = rdo.getServices().stream()
                        .map(RDOServiceEntity::getRegistration)
                        .collect(Collectors.toSet());

                var servicesToAdd = boardedEmployee.stream()
                        .filter(b -> !existingRegistrations.contains(b.getRegistration()))
                         .map(b -> RDOServiceEntity.availableFromServiceLine(response.serviceLine(), b))
                        .toList();

                rdo.getServices().addAll(servicesToAdd);
                rdosToSave.add(rdo);
            }
        }
        rdoRepository.saveAll(rdosToSave);

    }

    private List<LocalDate> enumerateDates(LocalDate start, LocalDate end) {
        long days = ChronoUnit.DAYS.between(start, end);
        return LongStream.rangeClosed(0, days).mapToObj(start::plusDays).toList();
    }

    private record Validated(PPUEntity ppu, ServiceLine serviceLine){}

    private Validated validateAndResolve(AvailablePeriod request) {
        if (request.start().isAfter(request.end())) throw new ModuleFailure("Data de início não pode ser posterior à data de fim");
        var ppu = ppuRepository.findById(request.ppuId()).orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada para essa solicitação."));
        if (!ppu.getPlatforms().contains(request.platform())) throw new ModuleFailure("Plataforma não encontrada para a PPU solicitada.");
        var serviceLine = ppu.getServices().stream().filter(s -> s.getId().equals(request.serviceId()))
                .findFirst()
                .orElseThrow(() -> new ModuleFailure("Serviço não encontrado para a PPU solicitada."));
        if (!Boolean.TRUE.equals(serviceLine.getDisposicao())) throw new ModuleFailure("Serviço mencionado não é configurada para a disposição.");
        return new Validated(ppu, serviceLine);
    }

    private RDOEntity createBasicRDO(AvailablePeriod request, PPUEntity ppu, LocalDate date, AuthorContext author) {
        return RDOEntity.createBasic(
                ppu,
                request.platform(),
                date,
                DEFAULT_DESCRIPTION,
                author.registration(),
                author.name());
    }

    List<RDOServiceEntity> createRDOServices(List<BoardedEmployee> boardedEmployees, ServiceLine serviceLine){
        var rdoService = new ArrayList<RDOServiceEntity>();
        for(var boarded : boardedEmployees) {
            rdoService.add(RDOServiceEntity.availableFromServiceLine(serviceLine, boarded));
        }
        return rdoService;
    }

    private void validateRequestVolume(AvailablePeriod request) {
        var days = ChronoUnit.DAYS.between(request.start(), request.end()) + 1;
        if (days > MAX_DAYS_RANGE) {
            throw new ModuleFailure("Período excede o limite permitido de " + MAX_DAYS_RANGE + " dias.");
        }
        if (request.registrations().size() > MAX_REGISTRATIONS) {
            throw new ModuleFailure("Quantidade de colaboradores excede o limite permitido de " + MAX_REGISTRATIONS + ".");
        }
    }


    //todo: codigo duplicado do rdo service, corrigir posteriormente
    public Long calculateNextSequenceId(String platform, String ppuId, LocalDate date) {
        var previousEntity = rdoRepository.findTopByPlatformAndPpuIdAndDateBeforeOrderByDateDesc(platform, ppuId, date).orElse(null);
        if (previousEntity == null)
            return 1L;

        int quantityMissing = getDatesWithoutRDOByPlatform(platform, ppuId, previousEntity.getDate(), date).size();

        return previousEntity.getSequentialId() + quantityMissing;
    }

    public List<LocalDate> getDatesWithoutRDOByPlatform(String platform, String ppuId, LocalDate startDate, LocalDate endDate) {
        List<RDOEntity> rdosToAnalyze = rdoRepository.findAllByPlatformAndPpuIdAndDateBetween(platform, ppuId, startDate, endDate);

        Set<LocalDate> existingDates = rdosToAnalyze.stream()
                .map(RDOEntity::getDate)
                .collect(Collectors.toSet());

        List<LocalDate> allDates = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            allDates.add(date);
        }

        return allDates.stream()
                .filter(d -> !existingDates.contains(d))
                .toList();
    }
}
