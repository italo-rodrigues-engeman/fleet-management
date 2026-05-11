package com.indux.modules.ppu.presentation.controller;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.ppu.application.dtos.rdo.RDORecord;
import com.indux.modules.ppu.application.dtos.rdo.RDOResponse;
import com.indux.modules.ppu.application.dtos.requests.CalculatorRequest;
import com.indux.modules.ppu.application.dtos.requests.RDOFilter;
import com.indux.modules.ppu.application.dtos.response.CalculatorResponse;
import com.indux.modules.ppu.application.dtos.response.RDOPageResponse;
import com.indux.modules.ppu.application.dtos.response.RDOUpdaterResponse;
import com.indux.modules.ppu.application.mapper.RDOSyncLogMapper;
import com.indux.modules.ppu.application.services.rdo.GetMissingRDOsUseCase;
import com.indux.modules.ppu.application.services.rdo.RDOService;
import com.indux.modules.ppu.application.services.rdo.UpdateEmployeeStatusService;
import com.indux.modules.ppu.application.services.rdo.helper.LoggerUserHelper;
import com.indux.modules.ppu.application.services.rdo.helper.WorkHoursHelper;
import com.indux.modules.ppu.application.services.rdo.operation.DuplicateRDOUseCase;
import com.indux.modules.ppu.application.services.rdo.scripts.ScriptsVersion;
import com.indux.modules.ppu.presentation.dtos.MissingRDOResponse;
import com.indux.modules.ppu.presentation.dtos.MissingRDORequest;
import com.indux.modules.ppu.presentation.dtos.RDOSyncLogResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/solicitacoes/ppu/rdo")
public class RDOController {
    private final RDOService service;
    private final GetMissingRDOsUseCase missingUseCase;
    private final DuplicateRDOUseCase duplicateUseCase;
    private final WorkHoursHelper workHoursHelper;
    private final ScriptsVersion scriptsVersion;
    private final UpdateEmployeeStatusService updateEmployeeStatusService;
    private final RDOSyncLogMapper syncLogMapper;

    public RDOController(RDOService service, GetMissingRDOsUseCase missingUseCase, DuplicateRDOUseCase duplicateUseCase, WorkHoursHelper workHoursHelper, ScriptsVersion scriptsVersion, UpdateEmployeeStatusService updateEmployeeStatusService, RDOSyncLogMapper syncLogMapper) {
        this.service = service;
        this.missingUseCase = missingUseCase;
        this.duplicateUseCase = duplicateUseCase;
        this.workHoursHelper = workHoursHelper;
        this.scriptsVersion = scriptsVersion;
        this.updateEmployeeStatusService = updateEmployeeStatusService;
        this.syncLogMapper = syncLogMapper;
    }

    @PostMapping("/create")
    public ResponseEntity<CreateRDOResponse> createRDO(@RequestBody RDORecord record, JwtAuthenticationToken token) {
        var created = service.create(record, UUID.fromString(token.getName()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateRDOResponse(
                        created.sequentialCode().toString(),
                        HttpStatus.CREATED.value(),
                        created.id(),
                        created.sequentialCode()
                ));
    }

    @GetMapping("/get-ppu")
    public ResponseEntity<Page<RDOResponse>> fetchAllByPPU(
            @PageableDefault(sort = "status", direction = Sort.Direction.ASC) JwtAuthenticationToken jwt,
            Pageable pageable,
            @RequestParam String id
    ) throws IOException {
        return ResponseEntity.ok(service.fetchAllByPPU(id, pageable));
    }

    @GetMapping("/get-code")
    public ResponseEntity<RDOResponse> fetchByCodeAndPlatform(
            @PageableDefault(sort = "status", direction = Sort.Direction.ASC) JwtAuthenticationToken jwt,
            Pageable pageable,
            @RequestParam Long code,
            @RequestParam String platform
    ) throws IOException {
        return ResponseEntity.ok(service.fetchByCodeAndPlatform(code, platform));
    }

    @GetMapping("/")
    public ResponseEntity<Page<RDOResponse>> fetchAll(
            @PageableDefault(sort = "status", direction = Sort.Direction.ASC) JwtAuthenticationToken jwt,
            Pageable pageable) throws IOException {
        return ResponseEntity.ok(service.findAll(pageable));
    }

    /**
     * GET /api/rdo/missing
     *
     * @param request é um request com os dados necessários.
     * @return lista de dias (yyyy-MM-dd) que não têm RDO
     */
    @GetMapping("/missing")
    public ResponseEntity<GetMissingRDOsUseCase.ResponseMissingDTO> getMissing(
            MissingRDORequest request
    ) {
        request.validate();
        var missing = missingUseCase.execute(request);
        return ResponseEntity.ok(missing);
    }

    @GetMapping("/filter")
    public ResponseEntity<RDOPageResponse> filterRDO(
            RDOFilter filter,
            @RequestParam Boolean isRh,
            Pageable pageable
    ) {
        var result = service.filterRDO(filter, pageable, isRh);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/fetch/{id}")
    public ResponseEntity<RDOResponse> fetchByID(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(service.fetchByID(id));
    }

    @GetMapping("/duplicate/{id}")
    public ResponseEntity<RDOUpdaterResponse> duplicate(
            @PathVariable String id,
            @RequestParam LocalDate date,
            JwtAuthenticationToken jwt
    ) throws IOException, ExecutionException, InterruptedException {
        return ResponseEntity.ok(duplicateUseCase.execute(date, id, jwt.getName()));
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    public ResponseEntity<GenericMessage> deleteRDO(@PathVariable String id) {
        service.deleteRDO(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new GenericMessage("RDO deletado com sucesso.", HttpStatus.OK.value()));
    }

    @GetMapping("/missing/pdf")
    public ResponseEntity<byte[]> getMissingPdf(MissingRDORequest request, JwtAuthenticationToken token) {

        String userName = (String) token.getTokenAttributes().get("name");
        byte[] pdf = missingUseCase.generateReport(request, userName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=missing-rdo.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/calculate")
    public ResponseEntity<CalculatorResponse> calculate(@RequestBody @Valid CalculatorRequest request) {
        return ResponseEntity.ok(workHoursHelper.compute(request.getSchedule(), request.getOvertimes()));
    }

    @PostMapping("/script/lines")
    public ResponseEntity<?> editFuckingRDOS() {
        scriptsVersion.fixLinesAddingPPUID();
        scriptsVersion.fixEquipments();
        return ResponseEntity.ok("TOP MEU PATRÃO");
    }
    @PostMapping("/script/steelCables")
    public ResponseEntity<?> steelCablesRDOS() {
        scriptsVersion.fixAllPpus();
        return ResponseEntity.ok("TOP MEU PATRÃO");
    }

    @PostMapping("/script/valueMeasured")
    public ResponseEntity<?> valueMeasured() {
        scriptsVersion.backfillValueMeasuredByLineStrategies();
        return ResponseEntity.ok("TOP MEU PATRÃO");
    }

    @PostMapping("/sync-employee-status/{rdoId}")
    public ResponseEntity<RDOSyncLogResponse> syncEmployeeStatus(
            @PathVariable String rdoId,
            JwtAuthenticationToken jwt
    ) {
        var syncLog = updateEmployeeStatusService.updateEmployeeStatus(rdoId, jwt);
        return ResponseEntity.ok(syncLogMapper.toResponse(syncLog));
    }

    public record CreateRDOResponse(
            String message,
            int status,
            String id,
            Long codigoSequencial
    ) {}

}
