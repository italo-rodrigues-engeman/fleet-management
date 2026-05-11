package com.indux.modules.ppu.presentation.controller;

import com.indux.modules.ppu.domain.entities.bm.RMLog;
import com.indux.modules.ppu.domain.services.bm.audit.mio.AuditMio;
import com.indux.modules.ppu.application.dtos.request.CreateBMRequest;
import com.indux.modules.ppu.application.dtos.request.MioDivergenceJustificationRequest;
import com.indux.modules.ppu.application.dtos.response.audit.AuditMIOResponse;
import com.indux.modules.ppu.application.dtos.response.bm.BMGrid;
import com.indux.modules.ppu.application.dtos.response.bm.BMModel;
import com.indux.modules.ppu.application.services.bm.BMService;
import com.indux.modules.ppu.application.services.bm.audit.rm.AuditRM;
import com.indux.modules.ppu.application.services.rdo.operation.audit.GenerateAuditExcelService;
import com.indux.modules.ppu.application.services.rdo.operation.bm.*;
import com.indux.modules.ppu.domain.services.bm.audit.samc.AuditSAMC;
import com.indux.modules.ppu.infra.mapper.MioDivergenceMapper;
import com.indux.modules.ppu.application.dtos.response.RMLogResponse;
import com.indux.modules.ppu.presentation.dtos.audit.AuditResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("api/solicitacoes/ppu/rdo/op/bm")
@Validated
public class BMController {
        private final BMDetailsHandler bmHandler;
        private final BMTimelineHandler bmTimelineHandler;
        private final BMMonthlyHandler bmMonthlyHandler;
        private final BMPlatformHandler bmPlatformHandler;
        private final BMEquipments bmEquipments;
        private final AuditRM auditRM;
        private final BMService bmService;
        private final GenerateAuditExcelService generateExcelService;
        private final AuditMio auditMio;
        private final MioDivergenceMapper mioMapper;
        private final AuditSAMC<AuditResponse> auditSAMCService;

        public BMController(BMDetailsHandler bmHandler, BMTimelineHandler bmTimelineHandler,
                        BMMonthlyHandler bmMonthlyHandler, BMPlatformHandler bmPlatformHandler,
                        BMEquipments bmEquipments, AuditRM auditRM, BMService bmService,
                        GenerateAuditExcelService generateExcelService, AuditMio auditMio,
                        MioDivergenceMapper mioMapper, AuditSAMC<AuditResponse> auditSAMCService) {
                this.bmHandler = bmHandler;
                this.bmTimelineHandler = bmTimelineHandler;
                this.bmMonthlyHandler = bmMonthlyHandler;
                this.bmPlatformHandler = bmPlatformHandler;
                this.bmEquipments = bmEquipments;
                this.auditRM = auditRM;
                this.bmService = bmService;
                this.generateExcelService = generateExcelService;
                this.auditMio = auditMio;
                this.mioMapper = mioMapper;
                this.auditSAMCService = auditSAMCService;
        }

        @PostMapping(path = "/second-audit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
        public ResponseEntity<List<AuditResponse>> secondSAMCAudit(
                        @RequestPart("file") MultipartFile file,
                        @RequestParam("bmID") String bmID,
                        JwtAuthenticationToken jwt) throws ParseException {
                var result = auditSAMCService.auditBatch(bmID, file, jwt);
                return ResponseEntity.ok(result);
        }

        @GetMapping("/details")
        public ResponseEntity<?> fetchBMDetails(
                        @RequestParam String plataforma,
                        @RequestParam String bmID) {
                return ResponseEntity.ok(bmHandler.fetchItem(bmID, plataforma));
        }

        @GetMapping("/timeline")
        public ResponseEntity<?> fetchBMTimeline(
                        @RequestParam String plataforma,
                        @RequestParam String bmID) {
                return ResponseEntity.ok(bmTimelineHandler.fetchBMTimeline(plataforma, bmID));
        }

        @GetMapping("/monthly")
        public ResponseEntity<?> fetchBMMonthly(
                        @RequestParam String bmID) {
                return ResponseEntity.ok(bmMonthlyHandler.fetchMonthlyBM(bmID));
        }

        @GetMapping("/by-platform")
        public ResponseEntity<?> fetchBMByPlatform(
                        @RequestParam String bmID) {
                return ResponseEntity.ok(bmPlatformHandler.fetchAll(bmID));
        }

        @GetMapping("/equipments")
        public ResponseEntity<?> fetchEquipments(
                        @RequestParam String plataforma,
                        @RequestParam String bmID) {
                return ResponseEntity.ok(bmEquipments.calculate(bmID, plataforma));
        }

        @PostMapping(path = "/audit-rm", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<RMLogResponse> auditRM(@RequestParam String bmID,
                        @RequestParam("files") List<MultipartFile> files,
                        JwtAuthenticationToken token) throws Exception {
                var result = auditRM.execute(bmID, files, token);

                return ResponseEntity.ok(result);
        }

        @PostMapping("/")
        public ResponseEntity<Void> create(@Validated @RequestBody CreateBMRequest request) {
                bmService.create(request);
                return ResponseEntity.status(HttpStatus.CREATED).build();
        }

        @GetMapping("/{id}")
        public ResponseEntity<BMModel> getById(@PathVariable String id) {
                var response = bmService.getByID(id);
                return ResponseEntity.ok(response);
        }

        @GetMapping("/grid")
        public ResponseEntity<Page<BMGrid>> getAllGrid(Pageable pageable) {
                var response = bmService.getAllGrid(pageable);
                return ResponseEntity.ok(response);
        }

        @PostMapping("/{id}/approve")
        public ResponseEntity<Void> approve(
                        @PathVariable String id,
                        JwtAuthenticationToken token) {
                bmService.approve(id, token.getName());
                return ResponseEntity.ok().build();
        }

        @PostMapping("/{id}/consolidate")
        public ResponseEntity<Void> consolidate(
                        @PathVariable String id,
                        @RequestBody BMModel request,
                        JwtAuthenticationToken token) {
                bmService.consolidate(id, request, token);
                return ResponseEntity.ok().build();
        }

        @GetMapping(path = "/export-audit-excel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        public ResponseEntity<Resource> exportAuditExcelForBM(
                        @RequestParam String bmID,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
                        JwtAuthenticationToken jwt) {
                var bm = bmService.getByID(bmID);

                // Se não fornecer datas, usa o período do BM
                LocalDate startDate = inicio != null ? inicio : bm.period().getStart();
                LocalDate endDate = fim != null ? fim : bm.period().getEnd();

                byte[] excelBytes = generateExcelService.generateForDateRange(
                                bm.ppuId(),
                                startDate,
                                endDate,
                                List.of() // As plataformas serão buscadas do PPU
                );

                ByteArrayResource resource = new ByteArrayResource(excelBytes);

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=auditoria_bm_" + bmID + ".xlsx");
                headers.add(HttpHeaders.CONTENT_TYPE,
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

                return ResponseEntity.ok()
                                .headers(headers)
                                .contentLength(excelBytes.length)
                                .body(resource);
        }

        @PostMapping(path = "/export-audit-excel-by-ids", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        public ResponseEntity<Resource> exportAuditExcelByIds(
                        @RequestBody List<String> rdoIds,
                        JwtAuthenticationToken jwt) {
                byte[] excelBytes = generateExcelService.generateForMultipleRDOs(rdoIds);
                ByteArrayResource resource = new ByteArrayResource(excelBytes);

                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=auditoria_rdos_multiplos.xlsx");
                headers.add(HttpHeaders.CONTENT_TYPE,
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

                return ResponseEntity.ok()
                                .headers(headers)
                                .contentLength(excelBytes.length)
                                .body(resource);
        }

        @PostMapping("/second-audit/mio")
        public ResponseEntity<List<AuditMIOResponse>> secondAuditMIO(
                        @RequestParam String bmID,
                        JwtAuthenticationToken jwt) throws IOException, ExecutionException, InterruptedException {
                var result = auditMio.call(bmID, jwt);
                return ResponseEntity.ok(mioMapper.toResponses(result));
        }

        @PatchMapping("/{id}/mio-divergences/justifications")
        public ResponseEntity<Void> justifyMioDivergences(
                        @PathVariable String id,
                        @Validated @RequestBody MioDivergenceJustificationRequest request) {
                bmService.justifyMioDivergences(id, request.items());
                return ResponseEntity.ok().build();
        }
}
