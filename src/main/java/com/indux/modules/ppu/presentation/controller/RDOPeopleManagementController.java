package com.indux.modules.ppu.presentation.controller;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.ppu.application.dtos.NextRdoDTO;
import com.indux.modules.ppu.application.dtos.requests.ClosedCompetenceRequest;
import com.indux.modules.ppu.application.dtos.requests.RDOFilter;
import com.indux.modules.ppu.application.dtos.requests.RDOFlowRequest;
import com.indux.modules.ppu.application.dtos.response.RDOPageResponse;
import com.indux.modules.ppu.application.dtos.response.competence.CompetenceResponse;
import com.indux.modules.ppu.application.dtos.response.competence.RDOConsolidationResponse;
import com.indux.modules.ppu.application.projection.CompetenceProjection;
import com.indux.modules.ppu.application.projection.RDOGridProjection;
import com.indux.modules.ppu.application.services.rdo.RDOService;
import com.indux.modules.ppu.application.services.rdo.rh.RDOReviewService;
import com.indux.modules.ppu.application.services.rdo.rh.competence.CompetenceFacade;
import com.indux.modules.ppu.application.services.rdo.rh.consolidation.FetchConsolidationArchive;
import com.indux.modules.ppu.application.services.rdo.rh.consolidation.FetchConsolidationView;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/solicitacoes/ppu/rdo/dp")
public class RDOPeopleManagementController {
        private final RDOService service;
        private final RDOReviewService rhService;
        private final CompetenceFacade competenceService;
        private final FetchConsolidationView fetchConsolidation;
        private final FetchConsolidationArchive fetchConsolidationArchive;

        public RDOPeopleManagementController(RDOService service, RDOReviewService rhService,
                        CompetenceFacade competenceService, FetchConsolidationView fetchConsolidation,
                        FetchConsolidationArchive fetchConsolidationArchive) {
                this.service = service;
                this.rhService = rhService;
                this.competenceService = competenceService;
                this.fetchConsolidation = fetchConsolidation;
                this.fetchConsolidationArchive = fetchConsolidationArchive;
        }

        // -- Competence Endpoints -- //
        @PostMapping("/close-competence")
        public ResponseEntity<CompetenceResponse> closeCompetence(
                        @RequestBody ClosedCompetenceRequest request,
                        JwtAuthenticationToken user) {
                return ResponseEntity.ok(competenceService.close(request, user.getName()));
        }

        @DeleteMapping("/cancel-competence")
        public ResponseEntity<GenericMessage> cancelCompetence(
                        @RequestParam YearMonth period,
                        @RequestParam Long project,
                        JwtAuthenticationToken user) {
                competenceService.cancel(period, project, user.getName());
                return ResponseEntity.ok(new GenericMessage("Competência cancelada com sucesso.", 200));
        }

        @GetMapping("/competence")
        public ResponseEntity<Page<CompetenceProjection>> fetchAll(
                        Pageable pageable) {
                return ResponseEntity.ok(competenceService.fetchAllProjections(pageable));
        }

        @GetMapping("/competence/missing")
        public ResponseEntity<List<YearMonth>> fetchAllMissingCompetence(
                        @RequestParam Long project) {
                return ResponseEntity.ok(competenceService.fetchMissingCompetenceInYear(project));
        }

        // -- Consolidation Endpoints --//
        @GetMapping("/consolidation")
        public ResponseEntity<Page<RDOConsolidationResponse>> fetchConsolidation(
                        @RequestParam(required = false) YearMonth period,
                        @RequestParam(required = false) Long project) {
                return ResponseEntity
                                .ok(fetchConsolidation.execute(period != null ? period : YearMonth.now(), project));
        }

        @GetMapping("/consolidation/filter")
        public ResponseEntity<Page<RDOConsolidationResponse>> filterConsolidation(
                        RDOFilter filter,
                        @Nullable YearMonth competence,
                        Pageable pageable) {
                return ResponseEntity.ok(fetchConsolidation.filterConsolidation(pageable, filter, competence));
        }

        @GetMapping("/consolidation/competence")
        public ResponseEntity<Page<RDOConsolidationResponse>> fetchConsolidationByCompetence(
                        @RequestParam YearMonth competence,
                        @RequestParam Long project) {
                return ResponseEntity.ok(fetchConsolidation.fetchConsolidationViewByCompetence(competence, project));
        }

        @GetMapping(path = "/file-consolidation/overtime", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        public ResponseEntity<String> consolidateOvertimeArchive(
                        @RequestParam YearMonth period, @RequestParam Long project,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
                var resource = fetchConsolidationArchive.consolidateOvertime(period, project, startDate, endDate);
                return ResponseEntity
                                .ok(resource);
        }

        @GetMapping(path = "/file-consolidation/premium-night", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        public ResponseEntity<String> consolidationPremiumNightArchive(
                        @RequestParam YearMonth period, @RequestParam Long project,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        JwtAuthenticationToken user) {
                var resource = fetchConsolidationArchive.consolidatePremiumNight(period, project, startDate, endDate);
                return ResponseEntity
                                .ok(resource);
        }

        @GetMapping(path = "/file-consolidation/teamLeader", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        public ResponseEntity<String> consolidationPremiumTeamLeader(
                        @RequestParam YearMonth period, @RequestParam Long project,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                        JwtAuthenticationToken user) {
                var resource = fetchConsolidationArchive.consolidateTeamLeader(period, project, startDate, endDate);
                return ResponseEntity
                                .ok(resource);
        }

        // -- RDO Flow Endpoints -- //
        @PatchMapping("/approve")
        public ResponseEntity<NextRdoDTO> approve(
                        @RequestParam(defaultValue = "false") Boolean next,
                        @RequestBody RDOFlowRequest request,
                        JwtAuthenticationToken token) {
                return ResponseEntity.status(HttpStatus.OK)
                                .body(rhService.approve(request, next, UUID.fromString(token.getName())));
        }

        @PatchMapping("/recuse")
        public ResponseEntity<NextRdoDTO> recuse(
                        @RequestParam(defaultValue = "false") Boolean next,
                        @RequestBody RDOFlowRequest request, JwtAuthenticationToken token) {
                return ResponseEntity.status(HttpStatus.OK)
                                .body(rhService.decline(request, next, UUID.fromString(token.getName())));
        }

        @GetMapping("/fetch")
        public ResponseEntity<RDOPageResponse> fetch(
                        JwtAuthenticationToken jwt,
                        Pageable pageable) {
                return ResponseEntity.ok(rhService.fetch(UUID.fromString(jwt.getName()), pageable));
        }

        @GetMapping("/by-competence")
        public Page<RDOGridProjection> getByCompetence(
                        @RequestParam String platform,
                        @RequestParam YearMonth period,
                        Pageable pageable) {
                return service.listByCompetence(platform, period, pageable);
        }

        @GetMapping("/by-data")
        public Page<RDOGridProjection> getByDate(
                        @RequestParam String platform,
                        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        Pageable pageable) {
                return service.listByDateWithinCompetence(platform, date, pageable);
        }

}
