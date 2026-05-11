package com.indux.modules.ppu.presentation.controller;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.ppu.application.dtos.NextRdoDTO;
import com.indux.modules.ppu.application.dtos.rdo.RDORecord;
import com.indux.modules.ppu.application.dtos.requests.CreateRDOCoordinatorRequest;
import com.indux.modules.ppu.application.dtos.requests.RDOFlowRequest;
import com.indux.modules.ppu.application.dtos.response.RDOPageResponse;
import com.indux.modules.ppu.application.dtos.response.RDOUpdaterResponse;
import com.indux.modules.ppu.domain.services.bm.audit.samc.AuditSAMC;
import com.indux.modules.ppu.application.services.rdo.RDOService;
import com.indux.modules.ppu.application.services.rdo.operation.DuplicateRDOUseCase;
import com.indux.modules.ppu.application.services.rdo.operation.audit.GenerateAuditExcelService;
import com.indux.modules.ppu.application.services.rdo.rh.FetchUpdaterRDO;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.presentation.dtos.audit.AuditResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("api/solicitacoes/ppu/rdo/op")
@Validated
public class RDOCoordinatorController {
        private final RDOService service;
        private final FetchUpdaterRDO fetchUpdater;
        private final DuplicateRDOUseCase duplicateUseCase;
        private final AuditSAMC<AuditResponse> audit;
        private final GenerateAuditExcelService generateExcelService;

        public RDOCoordinatorController(RDOService service, FetchUpdaterRDO fetchUpdater,
                        DuplicateRDOUseCase duplicateUseCase, AuditSAMC<AuditResponse> audit,
                        GenerateAuditExcelService generateExcelService) {
                this.service = service;
                this.fetchUpdater = fetchUpdater;
                this.duplicateUseCase = duplicateUseCase;
                this.audit = audit;
                this.generateExcelService = generateExcelService;
        }

        @PutMapping("/update/{id}")
        public ResponseEntity<RDOEntity> update(
                        @PathVariable String id,
                        @Validated(RDORecord.RDOUpdate.class) @RequestBody RDORecord record,
                        JwtAuthenticationToken token) {
                String authToken = token != null ? token.getToken().getTokenValue() : null;
                return ResponseEntity.status(HttpStatus.OK)
                                .body(service.updateComplete(id, record, token.getName(), authToken));
        }

        @PatchMapping("/approve")
        public ResponseEntity<NextRdoDTO> approvalIncludeNextPending(
                        @RequestParam(defaultValue = "false") Boolean next,
                        @ModelAttribute RDOFlowRequest request, JwtAuthenticationToken token) {
                return ResponseEntity.status(HttpStatus.OK)
                                .body(service.operationApproval(request, next, token.getName()));
        }

        @GetMapping("/fetch")
        public ResponseEntity<RDOPageResponse> fetchAllGrid(
                        @PageableDefault(sort = "status", direction = Sort.Direction.ASC) JwtAuthenticationToken jwt,
                        Pageable pageable) {
                return ResponseEntity.ok(service.findAllOp(UUID.fromString(jwt.getName()), pageable));
        }

        @GetMapping("/fetch/update/{id}")
        public ResponseEntity<RDOUpdaterResponse> fetchUpdate(
                        @PathVariable String id) {
                return ResponseEntity.ok(fetchUpdater.execute(id));
        }

        @GetMapping("/fetch/by-ppu-and-date")
        public ResponseEntity<List<RDOEntity>> fetchByPpuAndDate(
                        @RequestParam("ppuId") String ppuId,
                        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
                return ResponseEntity.ok(service.fetchAllByPpuAndDate(ppuId, date));
        }

        @PostMapping("/approve/batch-by-ppu-and-date")
        public ResponseEntity<List<RDOEntity>> approveBatchByPpuAndDate(
                        @RequestParam("ppuId") String ppuId,
                        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        JwtAuthenticationToken jwt) {
                String userId = jwt != null ? jwt.getName() : null;
                return ResponseEntity.ok(service.approveBatchByPpuAndDate(ppuId, date, userId));
        }

        @PostMapping("/duplicate/{id}")
        public ResponseEntity<RDOUpdaterResponse> duplicate(
                        @PathVariable String id,
                        @RequestBody CreateRDOCoordinatorRequest request,

                        JwtAuthenticationToken jwt) throws IOException, ExecutionException, InterruptedException {
                return ResponseEntity.ok(duplicateUseCase.execute(request, id, jwt.getName()));
        }

        @PostMapping(path = "/audit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<List<AuditResponse>> auditCodeSap(
                        @RequestPart("file") MultipartFile file,
                        @RequestParam("rdo") String rdo,
                        JwtAuthenticationToken jwt) throws ParseException {
                var result = audit.audit(rdo, file, jwt);
                return ResponseEntity.ok(result);
        }

        @PostMapping(path = "/audit/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<List<AuditResponse>> auditMultipleRDOsWithSamc(
                        @RequestPart("file") MultipartFile file,
                        @RequestParam("rdos") List<String> rdos,
                        JwtAuthenticationToken jwt) throws ParseException {
                var result = audit.auditMultipleRDOs(rdos, file, jwt);
                return ResponseEntity.ok(result);
        }

        @GetMapping(path = "/export-audit-excel/{rdoId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        public ResponseEntity<Resource> exportAuditExcel(
                        @PathVariable String rdoId,
                        JwtAuthenticationToken jwt) {
                byte[] excelBytes = generateExcelService.generateForSingleRDO(rdoId);
                ByteArrayResource resource = new ByteArrayResource(excelBytes);

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=auditoria_rdo_" + rdoId + ".xlsx");
                headers.add(HttpHeaders.CONTENT_TYPE,
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

                return ResponseEntity.ok()
                                .headers(headers)
                                .contentLength(excelBytes.length)
                                .body(resource);
        }

}
