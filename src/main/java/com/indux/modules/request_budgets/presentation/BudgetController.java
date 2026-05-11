package com.indux.modules.request_budgets.presentation;

import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.request_budgets.application.dto.BudgetCircularesResponseDTO;
import com.indux.modules.request_budgets.application.dto.BudgetEsclarecimentoResponseDTO;
import com.indux.modules.request_budgets.application.dto.BudgetOSResponseDTO;
import com.indux.modules.request_budgets.application.dto.BudgetPremissaResponseDTO;
import com.indux.modules.request_budgets.application.dto.BudgetPropostaResponseDTO;
import com.indux.modules.request_budgets.application.dto.BudgetResponseDTO;
import com.indux.modules.request_budgets.application.dto.BudgetVersionResponseDTO;
import com.indux.modules.request_budgets.application.dto.CreateKeywordsRequest;
import com.indux.modules.request_budgets.application.dto.KeywordResponseDTO;
import com.indux.modules.request_budgets.application.dto.UpsertScoringRequest;
import com.indux.modules.request_budgets.application.dto.BudgetScoringConfigResponseDTO;
import com.indux.modules.request_budgets.application.dto.CreateBudgetCircularesRequest;
import com.indux.modules.request_budgets.application.dto.CreateBudgetEsclarecimentoRequest;
import com.indux.modules.request_budgets.application.dto.CreateBudgetOSRequest;
import com.indux.modules.request_budgets.application.dto.CreateBudgetPremissaRequest;
import com.indux.modules.request_budgets.application.dto.CreateBudgetPropostaRequest;
import com.indux.modules.request_budgets.application.dto.CreateBudgetRequest;
import com.indux.modules.request_budgets.application.dto.CreateBudgetResponseDTO;
import com.indux.modules.request_budgets.application.dto.CreateBudgetVersionRequest;
import com.indux.modules.request_budgets.application.dto.Filtro1RequestDTO;
import com.indux.modules.request_budgets.application.dto.Filtro2RequestDTO;
import com.indux.modules.request_budgets.application.dto.CreateBudgetComercialRequest;
import com.indux.modules.request_budgets.application.dto.CreateBudgetEngenhariaRequest;
import com.indux.modules.request_budgets.application.dto.CreateBudgetSmsRequest;
import com.indux.modules.request_budgets.application.dto.CreateBudgetOperacaoRequest;
import com.indux.modules.request_budgets.application.service.BudgetCaracteristicaService;
import com.indux.modules.request_budgets.application.service.BudgetCircularesService;
import com.indux.modules.request_budgets.application.service.BudgetEsclarecimentoService;
import com.indux.modules.request_budgets.application.service.BudgetOSService;
import com.indux.modules.request_budgets.application.service.BudgetPremissaService;
import com.indux.modules.request_budgets.application.service.BudgetPropostaService;
import com.indux.modules.request_budgets.application.service.BudgetService;
import com.indux.modules.request_budgets.application.service.BudgetTipoService;
import com.indux.modules.request_budgets.application.service.BudgetVersionService;
import com.indux.modules.request_budgets.application.service.BudgetKeywordService;
import com.indux.modules.request_budgets.application.service.BudgetScoringService;
import com.indux.modules.request_budgets.domain.model.BudgetCaracteristica;
import com.indux.modules.request_budgets.domain.model.BudgetTipo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;
    private final BudgetTipoService budgetTipoService;
    private final BudgetCaracteristicaService budgetCaracteristicaService;
    private final BudgetVersionService budgetVersionService;
    private final BudgetOSService budgetOSService;
    private final BudgetEsclarecimentoService budgetEsclarecimentoService;
    private final BudgetCircularesService budgetCircularesService;
    private final BudgetPremissaService budgetPremissaService;
    private final BudgetPropostaService budgetPropostaService;
    private final BudgetKeywordService budgetKeywordService;
    private final BudgetScoringService budgetScoringService;

    @PostMapping
    public ResponseEntity<CreateBudgetResponseDTO> createBudget(
            @Valid @RequestBody CreateBudgetRequest request,
            JwtAuthenticationToken jwt) {
        
        CreateBudgetResponseDTO response = budgetService.createBudget(request, jwt.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponseDTO> getBudgetById(@PathVariable String id) {
        BudgetResponseDTO response = budgetService.getBudgetById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponseDTO>> getAllBudgets() {
        List<BudgetResponseDTO> budgets = budgetService.getAllBudgets();
        return ResponseEntity.ok(budgets);
    }

    @PostMapping("/tipos")
    public ResponseEntity<BudgetTipo> createTipo(@RequestBody BudgetTipo tipo) {
        BudgetTipo created = budgetTipoService.createTipo(tipo);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/tipos")
    public ResponseEntity<List<BudgetTipo>> getAllTipos() {
        List<BudgetTipo> tipos = budgetTipoService.getAllTipos();
        return ResponseEntity.ok(tipos);
    }

    @GetMapping("/tipos/{id}")
    public ResponseEntity<BudgetTipo> getTipoById(@PathVariable String id) {
        BudgetTipo tipo = budgetTipoService.getTipoById(id);
        return ResponseEntity.ok(tipo);
    }

    @DeleteMapping("/tipos/{id}")
    public ResponseEntity<GenericMessage> deleteTipo(@PathVariable String id) {
        budgetTipoService.deleteTipo(id);
        return ResponseEntity.ok(new GenericMessage("Tipo removido com sucesso", 200));
    }

    @PostMapping("/caracteristicas")
    public ResponseEntity<BudgetCaracteristica> createCaracteristica(@RequestBody BudgetCaracteristica caracteristica) {
        BudgetCaracteristica created = budgetCaracteristicaService.createCaracteristica(caracteristica);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/caracteristicas")
    public ResponseEntity<List<BudgetCaracteristica>> getAllCaracteristicas() {
        List<BudgetCaracteristica> caracteristicas = budgetCaracteristicaService.getAllCaracteristicas();
        return ResponseEntity.ok(caracteristicas);
    }

    @GetMapping("/caracteristicas/{id}")
    public ResponseEntity<BudgetCaracteristica> getCaracteristicaById(@PathVariable String id) {
        BudgetCaracteristica caracteristica = budgetCaracteristicaService.getCaracteristicaById(id);
        return ResponseEntity.ok(caracteristica);
    }

    @DeleteMapping("/caracteristicas/{id}")
    public ResponseEntity<GenericMessage> deleteCaracteristica(@PathVariable String id) {
        budgetCaracteristicaService.deleteCaracteristica(id);
        return ResponseEntity.ok(new GenericMessage("Característica removida com sucesso", 200));
    }

    @PostMapping(value = "/{budgetId}/versions", consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<BudgetVersionResponseDTO> createBudgetVersion(
            @PathVariable String budgetId,
            @Valid @ModelAttribute CreateBudgetVersionRequest request,
            JwtAuthenticationToken jwt) {
        
        BudgetVersionResponseDTO response = budgetVersionService.createBudgetVersion(budgetId, request, jwt.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{budgetId}/versions")
    public ResponseEntity<List<BudgetVersionResponseDTO>> getBudgetVersions(@PathVariable String budgetId) {
        List<BudgetVersionResponseDTO> versions = budgetVersionService.getBudgetVersionsByBudgetId(budgetId);
        return ResponseEntity.ok(versions);
    }

    @GetMapping("/{budgetId}/versions/{versionId}")
    public ResponseEntity<BudgetVersionResponseDTO> getBudgetVersionById(
            @PathVariable String budgetId,
            @PathVariable String versionId) {
        BudgetVersionResponseDTO version = budgetVersionService.getBudgetVersionById(versionId);
        return ResponseEntity.ok(version);
    }

    @PostMapping(value = "/{budgetId}/versions/{versionId}/comercial", consumes = {"multipart/form-data"})
    public ResponseEntity<BudgetVersionResponseDTO> addComercial(
            @PathVariable String budgetId,
            @PathVariable String versionId,
            @Valid @ModelAttribute CreateBudgetComercialRequest request,
            JwtAuthenticationToken jwt) {
        
        BudgetVersionResponseDTO response = budgetVersionService.addComercial(versionId, request, jwt.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/{budgetId}/versions/{versionId}/engenharia", consumes = {"multipart/form-data"})
    public ResponseEntity<BudgetVersionResponseDTO> addEngenharia(
            @PathVariable String budgetId,
            @PathVariable String versionId,
            @Valid @ModelAttribute CreateBudgetEngenhariaRequest request,
            JwtAuthenticationToken jwt) {
        
        BudgetVersionResponseDTO response = budgetVersionService.addEngenharia(versionId, request, jwt.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/{budgetId}/versions/{versionId}/sms", consumes = {"multipart/form-data"})
    public ResponseEntity<BudgetVersionResponseDTO> addSms(
            @PathVariable String budgetId,
            @PathVariable String versionId,
            @Valid @ModelAttribute CreateBudgetSmsRequest request,
            JwtAuthenticationToken jwt) {
        
        BudgetVersionResponseDTO response = budgetVersionService.addSms(versionId, request, jwt.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/{budgetId}/versions/{versionId}/operacoes", consumes = {"multipart/form-data"})
    public ResponseEntity<BudgetVersionResponseDTO> addOperacao(
            @PathVariable String budgetId,
            @PathVariable String versionId,
            @Valid @ModelAttribute CreateBudgetOperacaoRequest request,
            JwtAuthenticationToken jwt) {
        
        BudgetVersionResponseDTO response = budgetVersionService.addOperacao(versionId, request, jwt.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/scoring")
    public ResponseEntity<BudgetScoringConfigResponseDTO> upsertScoring(
            @Valid @RequestBody UpsertScoringRequest request,
            JwtAuthenticationToken jwt) {
        BudgetScoringConfigResponseDTO response = budgetScoringService.upsertScoring(request, jwt.getName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/scoring")
    public ResponseEntity<BudgetScoringConfigResponseDTO> getScoring() {
        BudgetScoringConfigResponseDTO response = budgetScoringService.getScoring();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/scoring/fields")
    public ResponseEntity<Map<String, List<String>>> getAvailableScoringFields() {
        Map<String, List<String>> fields = budgetScoringService.getAvailableFields();
        return ResponseEntity.ok(fields);
    }

    @PostMapping("/keywords")
    public ResponseEntity<List<KeywordResponseDTO>> addKeywords(
            @Valid @RequestBody CreateKeywordsRequest request,
            JwtAuthenticationToken jwt) {
        List<KeywordResponseDTO> response = budgetKeywordService.addKeywords(request, jwt.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/keywords")
    public ResponseEntity<List<KeywordResponseDTO>> listKeywords() {
        List<KeywordResponseDTO> response = budgetKeywordService.listKeywords();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{budgetId}/versions/{versionId}/filtro1", consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<BudgetVersionResponseDTO> createFiltro1(
            @PathVariable String budgetId,
            @PathVariable String versionId,
            @Valid @ModelAttribute Filtro1RequestDTO request,
            JwtAuthenticationToken jwt) {

        BudgetVersionResponseDTO response = budgetVersionService.updateFiltro1(versionId, request, jwt.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/{budgetId}/versions/{versionId}/filtro2", consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<BudgetVersionResponseDTO> createFiltro2(
            @PathVariable String budgetId,
            @PathVariable String versionId,
            @Valid @ModelAttribute Filtro2RequestDTO request,
            JwtAuthenticationToken jwt) {

        BudgetVersionResponseDTO response = budgetVersionService.updateFiltro2(versionId, request, jwt.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{budgetId}/versions/{versionId}/approve")
    public ResponseEntity<BudgetVersionResponseDTO> approveBudgetVersion(
            @PathVariable String budgetId,
            @PathVariable String versionId,
            JwtAuthenticationToken jwt) {

        BudgetVersionResponseDTO response = budgetVersionService.approveVersion(budgetId, versionId, jwt.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{budgetId}/versions/{versionId}/filtro1", consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<BudgetVersionResponseDTO> updateFiltro1(
            @PathVariable String budgetId,
            @PathVariable String versionId,
            @Valid @ModelAttribute Filtro1RequestDTO request,
            JwtAuthenticationToken jwt) {
        
        BudgetVersionResponseDTO response = budgetVersionService.updateFiltro1(versionId, request, jwt.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{budgetId}/versions/{versionId}/filtro2", consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<BudgetVersionResponseDTO> updateFiltro2(
            @PathVariable String budgetId,
            @PathVariable String versionId,
            @Valid @ModelAttribute Filtro2RequestDTO request,
            JwtAuthenticationToken jwt) {
        
        BudgetVersionResponseDTO response = budgetVersionService.updateFiltro2(versionId, request, jwt.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/{budgetId}/os", consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<BudgetOSResponseDTO> createBudgetOS(
            @PathVariable String budgetId,
            @Valid @ModelAttribute CreateBudgetOSRequest request,
            JwtAuthenticationToken jwt) {
        
        BudgetOSResponseDTO response = budgetOSService.createBudgetOS(budgetId, request, jwt.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{budgetId}/os")
    public ResponseEntity<List<BudgetOSResponseDTO>> getBudgetOS(@PathVariable String budgetId) {
        List<BudgetOSResponseDTO> osList = budgetOSService.getBudgetOSByBudgetId(budgetId);
        return ResponseEntity.ok(osList);
    }

    @GetMapping("/{budgetId}/os/{osId}")
    public ResponseEntity<BudgetOSResponseDTO> getBudgetOSById(
            @PathVariable String budgetId,
            @PathVariable String osId) {
        BudgetOSResponseDTO os = budgetOSService.getBudgetOSById(osId);
        return ResponseEntity.ok(os);
    }

    @PostMapping(value = "/{budgetId}/versions/{versionId}/esclarecimentos", consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<BudgetEsclarecimentoResponseDTO> createBudgetEsclarecimento(
            @PathVariable String budgetId,
            @PathVariable String versionId,
            @Valid @ModelAttribute CreateBudgetEsclarecimentoRequest request,
            JwtAuthenticationToken jwt) {
        
        BudgetEsclarecimentoResponseDTO response = budgetEsclarecimentoService.createBudgetEsclarecimento(versionId, request, jwt.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{budgetId}/versions/{versionId}/esclarecimentos")
    public ResponseEntity<List<BudgetEsclarecimentoResponseDTO>> getBudgetEsclarecimentos(
            @PathVariable String budgetId,
            @PathVariable String versionId) {
        List<BudgetEsclarecimentoResponseDTO> esclarecimentos = budgetEsclarecimentoService.getBudgetEsclarecimentosByVersionId(versionId);
        return ResponseEntity.ok(esclarecimentos);
    }

    @GetMapping("/{budgetId}/versions/{versionId}/esclarecimentos/{esclarecimentoId}")
    public ResponseEntity<BudgetEsclarecimentoResponseDTO> getBudgetEsclarecimentoById(
            @PathVariable String budgetId,
            @PathVariable String versionId,
            @PathVariable String esclarecimentoId) {
        BudgetEsclarecimentoResponseDTO esclarecimento = budgetEsclarecimentoService.getBudgetEsclarecimentoById(esclarecimentoId);
        return ResponseEntity.ok(esclarecimento);
    }

    @PostMapping(value = "/{budgetId}/versions/{versionId}/circulares", consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<BudgetCircularesResponseDTO> createBudgetCirculares(
            @PathVariable String budgetId,
            @PathVariable String versionId,
            @Valid @ModelAttribute CreateBudgetCircularesRequest request,
            JwtAuthenticationToken jwt) {
        
        BudgetCircularesResponseDTO response = budgetCircularesService.createBudgetCirculares(versionId, request, jwt.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{budgetId}/versions/{versionId}/circulares")
    public ResponseEntity<List<BudgetCircularesResponseDTO>> getBudgetCirculares(
            @PathVariable String budgetId,
            @PathVariable String versionId) {
        List<BudgetCircularesResponseDTO> circulares = budgetCircularesService.getBudgetCircularesByVersionId(versionId);
        return ResponseEntity.ok(circulares);
    }

    @GetMapping("/{budgetId}/versions/{versionId}/circulares/{circularesId}")
    public ResponseEntity<BudgetCircularesResponseDTO> getBudgetCircularesById(
            @PathVariable String budgetId,
            @PathVariable String versionId,
            @PathVariable String circularesId) {
        BudgetCircularesResponseDTO circulares = budgetCircularesService.getBudgetCircularesById(circularesId);
        return ResponseEntity.ok(circulares);
    }

    @PostMapping(value = "/{budgetId}/versions/{versionId}/premissas", consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<BudgetPremissaResponseDTO> createBudgetPremissa(
            @PathVariable String budgetId,
            @PathVariable String versionId,
            @Valid @ModelAttribute CreateBudgetPremissaRequest request,
            JwtAuthenticationToken jwt) {
        
        BudgetPremissaResponseDTO response = budgetPremissaService.createBudgetPremissa(versionId, request, jwt.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{budgetId}/versions/{versionId}/premissas")
    public ResponseEntity<List<BudgetPremissaResponseDTO>> getBudgetPremissas(
            @PathVariable String budgetId,
            @PathVariable String versionId) {
        List<BudgetPremissaResponseDTO> premissas = budgetPremissaService.getBudgetPremissasByVersionId(versionId);
        return ResponseEntity.ok(premissas);
    }

    @GetMapping("/{budgetId}/versions/{versionId}/premissas/{premissaId}")
    public ResponseEntity<BudgetPremissaResponseDTO> getBudgetPremissaById(
            @PathVariable String budgetId,
            @PathVariable String versionId,
            @PathVariable String premissaId) {
        BudgetPremissaResponseDTO premissa = budgetPremissaService.getBudgetPremissaById(premissaId);
        return ResponseEntity.ok(premissa);
    }

    @PostMapping(value = "/{budgetId}/versions/{versionId}/propostas", consumes = {"multipart/form-data", "application/json"})
    public ResponseEntity<BudgetPropostaResponseDTO> createBudgetProposta(
            @PathVariable String budgetId,
            @PathVariable String versionId,
            @Valid @ModelAttribute CreateBudgetPropostaRequest request,
            JwtAuthenticationToken jwt) {
        
        BudgetPropostaResponseDTO response = budgetPropostaService.createBudgetProposta(versionId, request, jwt.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{budgetId}/versions/{versionId}/propostas")
    public ResponseEntity<BudgetPropostaResponseDTO> getBudgetProposta(
            @PathVariable String budgetId,
            @PathVariable String versionId) {
        BudgetPropostaResponseDTO proposta = budgetPropostaService.getBudgetPropostaByVersionId(versionId);
        return ResponseEntity.ok(proposta);
    }

    @GetMapping("/{budgetId}/versions/{versionId}/propostas/{propostaId}")
    public ResponseEntity<BudgetPropostaResponseDTO> getBudgetPropostaById(
            @PathVariable String budgetId,
            @PathVariable String versionId,
            @PathVariable String propostaId) {
        BudgetPropostaResponseDTO proposta = budgetPropostaService.getBudgetPropostaById(propostaId);
        return ResponseEntity.ok(proposta);
    }
}

