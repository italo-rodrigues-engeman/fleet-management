package com.indux.modules.ppu.presentation.controller;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.ppu.application.assembler.PPUAssembler;
import com.indux.modules.ppu.application.dtos.PPUFilter;
import com.indux.modules.ppu.application.dtos.PPUResponse;
import com.indux.modules.ppu.application.dtos.PPUWithClientNameDTO;
import com.indux.modules.ppu.application.dtos.requests.CreateRDOCoordinatorRequest;
import com.indux.modules.ppu.application.dtos.requests.PPURequest;
import com.indux.modules.ppu.application.projection.PPUPlatforms;
import com.indux.modules.ppu.application.services.ppu.PPUGridCacheService;
import com.indux.modules.ppu.application.services.ppu.PPUService;
import com.indux.modules.ppu.application.services.ppu.available.AvailableHandler;
import com.indux.modules.ppu.application.services.ppu.available.AvailablePeriodHandler;
import com.indux.modules.ppu.application.services.ppu.equipments.EquipmentsPPUHandler;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.ppu.EquipmentLine;
import com.indux.modules.ppu.domain.entities.ppu.PPUEnhancedGridProjection;
import com.indux.modules.ppu.presentation.dtos.AvailablePeriod;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/solicitacoes/ppu")
public class PPUController {
    private final PPUService service;
    private final PPUAssembler assembler;
    private final PPUGridCacheService ppuGridCacheService;
    private final EquipmentsPPUHandler equipmentsHandler;
    private final AvailableHandler availableHandler;
    private final AvailablePeriodHandler availablePeriodHandler;

    public PPUController(PPUService service, PPUAssembler assembler, PPUGridCacheService ppuGridCacheService, EquipmentsPPUHandler equipmentsHandler, AvailableHandler availableHandler, AvailablePeriodHandler availablePeriodHandler) {
        this.service = service;
        this.assembler = assembler;
        this.ppuGridCacheService = ppuGridCacheService;
        this.equipmentsHandler = equipmentsHandler;
        this.availableHandler = availableHandler;
        this.availablePeriodHandler = availablePeriodHandler;
    }

    @PostMapping("/create")
    public ResponseEntity<GenericMessage> createPPU(
            @Validated(PPURequest.OnCreate.class) @RequestBody PPURequest record,
            JwtAuthenticationToken user
    ) {
        PPUEntity entity = assembler.toNewEntity(record, user.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new GenericMessage(service.create(entity, record.contratoID()), HttpStatus.CREATED.value()));
    }

    @GetMapping("/boarded")
    public ResponseEntity<PPUResponse> getForBoardedEmployee(
            @RequestParam(required = false) LocalDate date,
            JwtAuthenticationToken token) throws ExecutionException, InterruptedException, IOException {
        return ResponseEntity
                .ok(service.fetchCurrentPPUBoarded(UUID.fromString(token.getName()), date));
    }

    @PostMapping("/boarded-coordinator")
    public ResponseEntity<PPUResponse> getPPU(
            @RequestParam(required = false) LocalDate date,
            @RequestBody @Validated CreateRDOCoordinatorRequest request,
            JwtAuthenticationToken token) throws IOException, ExecutionException, InterruptedException {
        return ResponseEntity
                .ok(service.fetchCurrentPPUCoordinator(UUID.fromString(token.getName()), date, request));
    }

    @GetMapping("/")
    public ResponseEntity<Page<PPUEnhancedGridProjection>> fetchAll(
            @PageableDefault(sort = "status", direction = Sort.Direction.ASC)
            Pageable pageable) throws IOException {
        return ResponseEntity.ok(ppuGridCacheService.findAllEnhancedGrid(pageable));
    }

    @DeleteMapping("/caching")
    public ResponseEntity<GenericMessage> resetCaching() {
        ppuGridCacheService.clearRequestCache();
        return ResponseEntity.ok(new GenericMessage("Cache apagado com sucesso.", HttpStatus.OK.value()));
    }

    @GetMapping("/caching")
    public ResponseEntity<?> seeCache() {
        return ResponseEntity.ok(ppuGridCacheService.getCacheStats());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<PPUEntity> updatePPU(
            @PathVariable String id,
            @RequestBody PPURequest updateRecord,
            JwtAuthenticationToken token) {
        PPUEntity updatedPPU = service.updatePPU(id, updateRecord, token.getName());
        return ResponseEntity.ok(updatedPPU);
    }

    @GetMapping("/fetch/{id}")
    public ResponseEntity<PPUEntity> findById(@PathVariable String id, @RequestParam(required = false, defaultValue = "false") Boolean fetchTotalBalance) {
        return ResponseEntity.ok(service.findById(id, fetchTotalBalance));
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<PPUWithClientNameDTO>> filterPPUs(
            @ModelAttribute PPUFilter filter,
            @PageableDefault(size = 10, sort = "codeID", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.filterPPUsWithClientName(filter, pageable));
    }

    @GetMapping("/fetch/equipments")
    public ResponseEntity<EquipmentsPPUHandler.EquipmentResult> fetchEquipmentsByPPUId(
            @RequestParam(defaultValue = "") String ppuID
    ) {
        return ResponseEntity.ok(equipmentsHandler.fetchEquipments(ppuID));
    }

    @GetMapping("/fetch/available")
    public ResponseEntity<AvailableHandler.AvailableServiceResponse> fetchAvailableEmployeesService( @RequestParam String ppuID) {
        return ResponseEntity.ok(availableHandler.getAvailableServices(ppuID));
    }

    @PostMapping("/available")
    public ResponseEntity<GenericMessage> addEmployeeInAvailableServicesLater(
            @RequestBody AvailablePeriod request,
            JwtAuthenticationToken jwt
    ) {
        availablePeriodHandler.addMultipleAvailablePeriods(request,jwt.getName());
        return ResponseEntity.ok(new GenericMessage("Colaboradores adicionados com sucesso.", HttpStatus.OK.value()));
    }

    @PutMapping("/available")
    public ResponseEntity<GenericMessage> addEmployeeInAvailableServices(
            @RequestBody UpdateAvailableRequest request,
            @RequestParam String ppuID
    ) {
        availableHandler.addEmployeeAvailable(request.matriculas(), request.idServico(), ppuID);
        return ResponseEntity.ok(new GenericMessage("Colaboradores adicionados com sucesso.", HttpStatus.OK.value()));
    }

    @PutMapping("/equipments")
    public ResponseEntity<GenericMessage> updateEquipments(
            @RequestBody EquipmentRequest request,
            @RequestParam String ppuID,

            JwtAuthenticationToken token) {
        equipmentsHandler.updateEquipments(request.equipments(), ppuID);
        return ResponseEntity.ok(new GenericMessage("Equipamentos atualizados com sucesso.", HttpStatus.CREATED.value()));
    }

    @GetMapping("/platforms")
    public ResponseEntity<PPUPlatforms> findAllPlatformsInPPU(@RequestParam Long projeto){
        return ResponseEntity.ok(service.findPlatformsByProject(projeto));
    }

    public record UpdateAvailableRequest(
            List<AvailableHandler.AvailableEmployee> matriculas,
            String idServico
    ) {
    }

    public record EquipmentRequest(List<EquipmentLine> equipments) {
    }

    @PostMapping("/edit")
    public ResponseEntity<?> editAllPPU(){
        service.updateNames();
        return ResponseEntity.ok("Tudo feito, patrão.");
    }
}
