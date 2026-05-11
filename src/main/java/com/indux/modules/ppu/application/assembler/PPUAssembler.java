package com.indux.modules.ppu.application.assembler;

import com.indux.core.application.dto.generic.SimpleEmployeeDTO;
import com.indux.core.application.service.employee.GetEmployeeUseCase;
import com.indux.core.domain.model.employee.EmployeePosition;
import com.indux.core.domain.service.generic.GetPositionService;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.modules.ppu.application.dtos.item.LineDTO;
import com.indux.modules.ppu.application.dtos.item.ServiceItemDTO;
import com.indux.modules.ppu.application.dtos.item.SteelCableDTO;
import com.indux.modules.ppu.application.dtos.item.EquipmentServiceDTO;
import com.indux.modules.ppu.application.dtos.requests.AvailableTypeRequest;
import com.indux.modules.ppu.application.dtos.requests.PPURequest;
import com.indux.modules.ppu.domain.entities.item.CraneControl;
import com.indux.modules.ppu.domain.entities.item.ShiftSchedule;
import com.indux.modules.ppu.domain.entities.item.SteelCableControl;
import com.indux.modules.ppu.domain.entities.item.ServicePosition;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.ppu.AccessoryKitLine;
import com.indux.modules.ppu.domain.entities.ppu.AvailableType;
import com.indux.modules.ppu.domain.entities.ppu.EquipmentLine;
import com.indux.modules.ppu.domain.entities.ppu.LinePPU;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.ppu.SteelCableLine;
import com.indux.modules.ppu.domain.entities.rdo.AccessoryKitDTO;
import com.indux.modules.ppu.infra.mapper.AvailableTypeMapper;
import com.indux.modules.ppu.infra.mapper.PPUEntityMapper;
import com.indux.modules.ppu.infra.mapper.ppu.*;
import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PPUAssembler {

    private final EquipmentLineMapper equipmentLineMapper;
    private final SteelCableMapper steelCableMapper;
    private final LinePPUMapper linePPUMapper;
    private final ServiceLineMapper serviceLineMapper;
    private final AccessoryKitMapper accessoryKitMapper;
    private final PPUEntityMapper ppuMapper;
    private final StorageService storage;
    private final AvailableTypeMapper availableTypeMapper;
    private final GetPositionService<EmployeePosition> getPositionService;
    private final GetEmployeeUseCase getEmployeeUseCase;

    public PPUAssembler(EquipmentLineMapper equipmentLineMapper, SteelCableMapper steelCableMapper, LinePPUMapper linePPUMapper, ServiceLineMapper serviceLineMapper, AccessoryKitMapper accessoryKitMapper, PPUEntityMapper ppuMapper, StorageService storage, AvailableTypeMapper availableTypeMapper, GetPositionService<EmployeePosition> getPositionService, GetEmployeeUseCase getEmployeeUseCase) {
        this.equipmentLineMapper = equipmentLineMapper;
        this.steelCableMapper = steelCableMapper;
        this.linePPUMapper = linePPUMapper;
        this.serviceLineMapper = serviceLineMapper;
        this.accessoryKitMapper = accessoryKitMapper;
        this.ppuMapper = ppuMapper;
        this.storage = storage;
        this.availableTypeMapper = availableTypeMapper;
        this.getPositionService = getPositionService;
        this.getEmployeeUseCase = getEmployeeUseCase;
    }


    public PPUEntity toNewEntity(PPURequest dto, String user) {
        var ppu = ppuMapper.baseForm(dto);
        ppu.setServices(mapServices(dto.servicos()));
        ppu.setSteelCables(mapSteelCables(dto.cabosAcos()));
        ppu.setEquipments(mapEquipments(dto.equipamentos()));
        ppu.setAccessoryKits(mapAccessoryKits(dto.kitAcessorios()));
        ppu.setLines(mapLines(dto.linhas()));

        ppu.setCreatedBy(user);
        ppu.setCreatedAt(LocalDateTime.now());
        ppu.setAvailableType(mapAvailableTypes(dto.disposicaoTipos()));
        ppu.setVersion(1L);
        return ppu;
    }

    public void applyUpdate(PPUEntity ppu, PPURequest updateRecord) {
        ppuMapper.updateFromRequest(updateRecord, ppu);
        updateIfNotNull(updateRecord.servicos(), services -> ppu.setServices(mapServices(services)));
        updateIfNotNull(updateRecord.equipamentos(), items -> ppu.setEquipments(mapEquipments(items)));
        updateIfNotNull(updateRecord.cabosAcos(), items -> ppu.setSteelCables(mapSteelCables(items)));
        updateIfNotNull(updateRecord.kitAcessorios(), items -> ppu.setAccessoryKits(mapAccessoryKits(items)));
        updateIfNotNull(updateRecord.disposicaoTipos(), types -> ppu.setAvailableType(mapAvailableTypes(types)));
        updateIfNotNull(updateRecord.linhas(), items -> ppu.setLines(mapLines(items)));
        updateIfNotNull(updateRecord.controleGuindaste(), controls ->
                ppu.setCraneControls(controls.stream().map(CraneControl::fromDTO).toList()));
        updateIfNotNull(updateRecord.controleCaboAco(), controls ->
                ppu.setSteelCableControls(controls.stream().map(SteelCableControl::fromDTO).toList()));
        updateIfNotNull(updateRecord.horarios(), schedules ->
                ppu.setShiftSchedule(schedules.stream().map(this::mapShiftSchedule).toList()));
    }

    private <T> void updateIfNotNull(T value, Consumer<T> updater) {
        if (value != null) {
            updater.accept(value);
        }
    }

    public List<ServiceLine> mapServices(List<ServiceItemDTO> items) {
        if (items == null || items.isEmpty()) return List.of();
        return items.stream()
                .filter(Objects::nonNull)
                .map(dto -> {
                    var service = serviceLineMapper.toEntity(dto);
                    if (Boolean.TRUE.equals(service.getDisposicao())) {
                        service.setPositions(resolvePositions(dto.cargos()));
                        service.setEmployees(resolveFixedEmployees(dto));
                    } else {
                        service.setEmployees(List.of());
                        service.setPositions(resolvePositions(dto.cargos()));
                    }
                    return service;
                })
                .toList();
    }

    private List<ServicePosition> resolvePositions(List<String> cbos) {
        var result = getPositionService.findByCboCodes(cbos);
        if (result.isEmpty()) return Collections.emptyList();

        var uniqueByCbo = new LinkedHashMap<String, ServicePosition>();

        for (var position : result) {
            var cbo = position.getCodeCbo();
            uniqueByCbo.putIfAbsent(cbo, new ServicePosition(position.getName(), cbo));
        }

        return List.copyOf(uniqueByCbo.values());
    }

    private ShiftSchedule mapShiftSchedule(ShiftSchedule schedule) {
        return new ShiftSchedule(schedule.getTurno(), schedule.getHorarioInicial(),
                schedule.getHorarioInicialAlmoco(), schedule.getHorarioFinalAlmoco(),
                schedule.getHorarioFinal(), schedule.getHorarioNoturnoEstendido());
    }

    private List<BoardedEmployee> resolveFixedEmployees(ServiceItemDTO item) {
        if (!Boolean.TRUE.equals(item.disposicao()) || item.colaboradores() == null || item.colaboradores().isEmpty()) {
            return List.of();
        }

        List<String> registrations = item.colaboradores().stream()
                .map(BoardedEmployee::getRegistration)
                .filter(Objects::nonNull)
                .toList();
        if (registrations.isEmpty()) return Collections.emptyList();

        List<SimpleEmployeeDTO> employeeDTOS = getEmployeeUseCase.findByRegistrations(registrations);

        Map<String, SimpleEmployeeDTO> employeeMap = employeeDTOS.stream()
                .collect(Collectors.toMap(SimpleEmployeeDTO::getRegistration, Function.identity()));

        return item.colaboradores().stream()
                .map(be -> Optional.ofNullable(employeeMap.get(be.getRegistration()))
                        .map(dto -> BoardedEmployee.fromSimpleEmployee(dto, be))
                        .orElse(be))
                .toList();
    }

    public List<EquipmentLine> mapEquipments(List<EquipmentServiceDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream()
                .filter(Objects::nonNull)
                .map(this::mapEquipment)
                .toList();
    }

    public List<LinePPU> mapLines(List<LineDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream()
                .filter(Objects::nonNull)
                .map(this::mapLine)
                .toList();
    }

    public List<AccessoryKitLine> mapAccessoryKits(List<AccessoryKitDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream()
                .filter(Objects::nonNull)
                .map(this::mapAccessoryKit)
                .toList();
    }

    public List<AvailableType> mapAvailableTypes(List<AvailableTypeRequest> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream()
                .filter(Objects::nonNull)
                .map(this::mapAvailableType)
                .toList();
    }

    public List<SteelCableLine> mapSteelCables(List<SteelCableDTO> dtos) {
        if (dtos == null) return Collections.emptyList();
        return dtos.stream()
                .filter(Objects::nonNull)
                .map(this::mapSteelCable)
                .toList();
    }

    public EquipmentLine mapEquipment(EquipmentServiceDTO dto) {
        return equipmentLineMapper.toEntity(dto);
    }

    public LinePPU mapLine(LineDTO dto) {
        return linePPUMapper.toEntity(dto);
    }

    public AccessoryKitLine mapAccessoryKit(AccessoryKitDTO dto) {
        return accessoryKitMapper.toEntity(dto);
    }

    public AvailableType mapAvailableType(AvailableTypeRequest dto) {
        return availableTypeMapper.fromRequest(dto);
    }

    public SteelCableLine mapSteelCable(SteelCableDTO dto) {
        String uri = null;
        if (dto.certificado() != null && !dto.certificado().isEmpty()) {
            Path upload = storage.store(dto.certificado(), "ppu/caboAco/certificados", dto.nome() + LocalDateTime.now().toString().replace(":", "-"));
            uri = storage.getRootLocation().relativize(upload).toString().replace("\\", "/");
        }
        return steelCableMapper.toEntity(dto.copyWith(uri));
    }
}
