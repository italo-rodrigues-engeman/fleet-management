package com.indux.modules.modulo_mega.presentation.controller;

import com.indux.modules.modulo_mega.application.dto.groups.GroupFilter;
import com.indux.modules.modulo_mega.application.dto.items.MegaItemFilter;
import com.indux.modules.modulo_mega.application.services.MegaGroupService;
import com.indux.modules.modulo_mega.application.services.MegaItemTableService;
import com.indux.modules.modulo_mega.service.PdfGeneratorService;
import com.indux.modules.modulo_mega.service.ExcelGeneratorService; 
import com.indux.modules.modulo_mega.service.ItemSolicitationService;
import com.indux.modules.modulo_mega.application.dto.*;
import com.indux.modules.modulo_mega.domain.entities.mongo.ItemSolicitationEntity;
import com.indux.modules.modulo_mega.domain.enums.ItemSolicitationStatus;
import com.indux.modules.modulo_mega.domain.repository.specs.MegaItemQueries;
import com.indux.modules.modulo_mega.domain.enums.AutocompleteStrategy;
import com.indux.modules.modulo_mega.service.MegaItemService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import jakarta.validation.Valid;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import com.indux.core.domain.service.user.UserService;
import com.indux.core.domain.repository.generic.RegionalRepository;
import com.indux.core.domain.model.employee.Regional;
import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.modules.modulo_mega.domain.entities.jpa.CodigoAplicacaoMegaEntity;
import com.indux.modules.modulo_mega.domain.repository.jpa.CodigoAplicacaoMegaRepository;
import com.indux.modules.modulo_mega.domain.entities.jpa.CodigoServicoMegaEntity;
import com.indux.modules.modulo_mega.domain.repository.jpa.CodigoServicoMegaRepository;
import com.indux.modules.modulo_mega.domain.entities.jpa.CodigoIcmsMegaEntity;
import com.indux.modules.modulo_mega.domain.repository.jpa.CodigoIcmsMegaRepository;
import com.indux.modules.modulo_mega.domain.entities.jpa.CodigoPisCofinsMegaEntity;
import com.indux.modules.modulo_mega.domain.repository.jpa.CodigoPisCofinsMegaRepository;
import com.indux.modules.modulo_mega.domain.entities.jpa.CodigoSituacaoTributariaMegaEntity;
import com.indux.modules.modulo_mega.domain.repository.jpa.CodigoSituacaoTributariaMegaRepository;
import com.indux.modules.modulo_mega.domain.entities.jpa.CodigoSpedFiscalMegaEntity;
import com.indux.modules.modulo_mega.domain.repository.jpa.CodigoSpedFiscalMegaRepository;
import com.indux.modules.modulo_mega.domain.entities.jpa.UnidadeMedidaMegaEntity;
import com.indux.modules.modulo_mega.domain.repository.jpa.UnidadeMedidaMegaRepository;
import java.util.Arrays;

@RestController
@RequestMapping("/api/mega-itens")
@RequiredArgsConstructor
@Slf4j
public class MegaItemController {
    
    private final MegaItemService service;
    private final MegaItemQueries queries;
    private final PdfGeneratorService pdfGeneratorService;
    private final ExcelGeneratorService excelGeneratorService;
    private final ItemSolicitationService itemSolicitationService;
    private final UserService userService;
    private final RegionalRepository regionalRepository;
    private final MegaGroupService groupService;
    private final MegaItemTableService megaItemService;
    private final CodigoAplicacaoMegaRepository codigoAplicacaoMegaRepository;
    private final CodigoServicoMegaRepository codigoServicoMegaRepository;
    private final CodigoIcmsMegaRepository codigoIcmsMegaRepository;
    private final CodigoPisCofinsMegaRepository codigoPisCofinsMegaRepository;
    private final CodigoSituacaoTributariaMegaRepository codigoSituacaoTributariaMegaRepository;
    private final CodigoSpedFiscalMegaRepository codigoSpedFiscalMegaRepository;
    private final UnidadeMedidaMegaRepository unidadeMedidaMegaRepository;

    // ... (SEUS MÉTODOS EXISTENTES MANTIDOS AQUI) ...

    // Endpoints de solicitação de item - devem vir antes do endpoint genérico /{type}

    @PostMapping("/solicitacao-item/admin/adjust-sequence")
    public ResponseEntity<?> adjustSequence() {
        try {
            itemSolicitationService.adjustSequentialIds();
            return ResponseEntity.ok("Sequenciais ajustados com sucesso.");
        } catch (Exception e) {
            log.error("Erro ao ajustar sequenciais", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao ajustar sequenciais: " + e.getMessage());
        }
    }

    @PostMapping("/solicitacao-item")
    public ResponseEntity<ItemSolicitationEntity> solicitationItemCreate(
            @Valid @RequestBody ItemSolicitationDTO dto,
            JwtAuthenticationToken jwt
    ) {
        String regional = getRegionalFromUser(jwt);
        UUID userId = UUID.fromString(jwt.getName());
        ItemSolicitationEntity saved = itemSolicitationService.create(dto, regional, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/solicitacao-item")
    public ResponseEntity<ItemSolicitationPagedResponseDTO> listSolicitations(
            @PageableDefault(size = 20, sort = "creationDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicial,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFinal,
            @RequestParam(required = false) ItemSolicitationStatus status,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) String mainName,     // Novo parâmetro
            @RequestParam(required = false) String description   // Novo parâmetro
    ) {
        return ResponseEntity.ok(itemSolicitationService.findAll(
                pageable, dataInicial, dataFinal, status, createdBy, mainName, description
        ));
    }

    @GetMapping("/solicitacao-item/{id}")
    public ResponseEntity<ItemSolicitationEntity> getSolicitationById(
            @PathVariable String id
    ) {
        return ResponseEntity.ok(itemSolicitationService.findById(id));
    }

    @GetMapping("/solicitacao-item/{id}/download-excel")
    public ResponseEntity<byte[]> downloadSolicitationItemExcel(
            @PathVariable String id
    ) {
        ItemSolicitationEntity item = itemSolicitationService.findById(id);
        
        List<ItemSolicitationEntity> items = List.of(item);
        byte[] excel = excelGeneratorService.generateRegisteredItemsExcel(items);
        
        String filename = "item_solicitacao_" + id + ".xls";
        return createDownloadResponse(excel, filename, 
                MediaType.parseMediaType("application/vnd.ms-excel"));
    }

    @PutMapping("/solicitacao-item/{id}/cadastrar")
    public ResponseEntity<ItemSolicitationEntity> registerItem(
            @PathVariable String id,
            @Valid @RequestBody RegisterItemSolicitationDTO dto,
            JwtAuthenticationToken jwt
    ) {
        UUID userId = UUID.fromString(jwt.getName());
        ItemSolicitationEntity saved = itemSolicitationService.registerItem(id, dto, userId);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/solicitacao-item/{id}/rejeitar")
    public ResponseEntity<ItemSolicitationEntity> rejectSolicitation(
            @PathVariable String id,
            @Valid @RequestBody RejectionSolicitationDTO dto,
            JwtAuthenticationToken jwt
    ) {
        UUID userId = UUID.fromString(jwt.getName());
        ItemSolicitationEntity saved = itemSolicitationService.reject(id, dto, userId);
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/solicitacao-item/{id}/validacao-tecnica")
    public ResponseEntity<ItemSolicitationEntity> updateTechnicalValidation(
            @PathVariable String id,
            @Valid @RequestBody TechnicalValidationSolicitationDTO dto,
            JwtAuthenticationToken jwt
    ) {
        UUID userId = UUID.fromString(jwt.getName());
        ItemSolicitationEntity saved = itemSolicitationService.updateTechnicalValidation(id, dto, userId);
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/solicitacao-item/{id}/aprovacao-tributaria")
    public ResponseEntity<ItemSolicitationEntity> updateTaxApproval(
            @PathVariable String id,
            @Valid @RequestBody TaxApprovalSolicitationDTO dto,
            JwtAuthenticationToken jwt
    ) {
        UUID userId = UUID.fromString(jwt.getName());
        ItemSolicitationEntity saved = itemSolicitationService.updateTaxApproval(id, dto, userId);
        return ResponseEntity.ok(saved);
    }

    @PatchMapping("/solicitacao-item/{id}/grupo-item")
    public ResponseEntity<ItemSolicitationEntity> updateGrupoItem(
            @PathVariable String id,
            @Valid @RequestBody UpdateGrupoItemSolicitationDTO dto,
            JwtAuthenticationToken jwt
    ) {
        UUID userId = UUID.fromString(jwt.getName());
        ItemSolicitationEntity saved = itemSolicitationService.updateGrupoItem(id, dto, userId);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/codigo-aplicacao")
    public ResponseEntity<Page<CodigoAplicacaoMegaEntity>> getAllCodigoAplicacao(
            @RequestParam(required = false) String descAplicacao,
            @RequestParam(required = false) Integer codAplicacao,
            @PageableDefault(size = 20, sort = "codAplicacao", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CodigoAplicacaoMegaEntity> codigos;
        String codigoStr = codAplicacao != null ? codAplicacao.toString() : null;
        boolean hasDesc = descAplicacao != null && !descAplicacao.isBlank();
        boolean hasCod = codigoStr != null && !codigoStr.isBlank();
        
        if (hasDesc && hasCod) {
            // Ambos os filtros
            codigos = codigoAplicacaoMegaRepository.findByDescAplicacaoAndCodAplicacaoContaining(descAplicacao, codigoStr, pageable);
        } else if (hasDesc) {
            // Apenas descrição
            codigos = codigoAplicacaoMegaRepository.findByDescAplicacaoContainingIgnoreCase(descAplicacao, pageable);
        } else if (hasCod) {
            // Apenas código
            codigos = codigoAplicacaoMegaRepository.findByCodAplicacaoContaining(codigoStr, pageable);
        } else {
            // Sem filtros
            codigos = codigoAplicacaoMegaRepository.findAll(pageable);
        }
        return ResponseEntity.ok(codigos);
    }

    @GetMapping("/codigo-servico")
    public ResponseEntity<Page<CodigoServicoMegaEntity>> getAllCodigoServico(
            @RequestParam(required = false) String descServico,
            @RequestParam(required = false) Integer codServico,
            @PageableDefault(size = 20, sort = "codServico", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CodigoServicoMegaEntity> codigos;
        String codigoStr = codServico != null ? codServico.toString() : null;
        boolean hasDesc = descServico != null && !descServico.isBlank();
        boolean hasCod = codigoStr != null && !codigoStr.isBlank();
        
        if (hasDesc && hasCod) {
            // Ambos os filtros
            codigos = codigoServicoMegaRepository.findByDescServicoAndCodServicoContaining(descServico, codigoStr, pageable);
        } else if (hasDesc) {
            // Apenas descrição
            codigos = codigoServicoMegaRepository.findByDescServicoContainingIgnoreCase(descServico, pageable);
        } else if (hasCod) {
            // Apenas código
            codigos = codigoServicoMegaRepository.findByCodServicoContaining(codigoStr, pageable);
        } else {
            // Sem filtros
            codigos = codigoServicoMegaRepository.findAll(pageable);
        }
        return ResponseEntity.ok(codigos);
    }

    @GetMapping("/codigo-icms")
    public ResponseEntity<List<CodigoIcmsMegaEntity>> getAllCodigoIcms() {
        List<CodigoIcmsMegaEntity> codigos = codigoIcmsMegaRepository.findAll();
        return ResponseEntity.ok(codigos);
    }

    @GetMapping("/codigo-pis-cofins")
    public ResponseEntity<Page<CodigoPisCofinsMegaEntity>> getAllCodigoPisCofins(
            @PageableDefault(size = 20, sort = "codPisCofins", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CodigoPisCofinsMegaEntity> codigos = codigoPisCofinsMegaRepository.findAll(pageable);
        return ResponseEntity.ok(codigos);
    }

    @GetMapping("/codigo-situacao-tributaria")
    public ResponseEntity<Page<CodigoSituacaoTributariaMegaEntity>> getAllCodigoSituacaoTributaria(
            @PageableDefault(size = 20, sort = "codSitTributaria", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CodigoSituacaoTributariaMegaEntity> codigos = codigoSituacaoTributariaMegaRepository.findAll(pageable);
        return ResponseEntity.ok(codigos);
    }

    @GetMapping("/codigo-sped-fiscal")
    public ResponseEntity<Page<CodigoSpedFiscalMegaEntity>> getAllCodigoSpedFiscal(
            @PageableDefault(size = 20, sort = "codSpedFiscal", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        Page<CodigoSpedFiscalMegaEntity> codigos = codigoSpedFiscalMegaRepository.findAll(pageable);
        return ResponseEntity.ok(codigos);
    }

    @GetMapping("/unidade-medida")
    public ResponseEntity<List<UnidadeMedidaMegaEntity>> getAllUnidadeMedida() {
        List<UnidadeMedidaMegaEntity> unidades = unidadeMedidaMegaRepository.findAll();
        return ResponseEntity.ok(unidades);
    }

    @GetMapping("/{type}")
    public Page<?> getAutocomplete(
            @PathVariable String type,
            @RequestParam(required = false) String term,
            Pageable pageable) {
        try {
            String searchTerm = (term == null || term.isBlank()) ? "" : term;
            AutocompleteStrategy strategy = AutocompleteStrategy.fromString(type);
            return strategy.search(queries, searchTerm, pageable);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/grupo")
    public Page<?> getGrupo(
            Pageable pageable,
            GroupFilter filter
    ) {
        try {
            return groupService.getAll(pageable, filter);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/item")
    public Page<?> getItemsNames(
            Pageable pageable,
            MegaItemFilter filter
    ) {
        try {
            return megaItemService.getAll(pageable, filter);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/type/disponiveis")
    public List<AutocompleteTypeDTO> getAvailableTypes() {
        return Arrays.stream(AutocompleteStrategy.values())
                .map(type -> new AutocompleteTypeDTO(type.getValue(), type.getLabel()))
                .toList();
    }

    @GetMapping("/itens-por-grupo")
    public ResponseEntity<Page<ItemGroupResponseDTO>> getItensPorGrupo(
            @RequestParam(required = false) List<Integer> groupCodes,
            @RequestParam(required = false) List<String> groupNames,
            @RequestParam(required = false) String term,
            @PageableDefault(size = 20) Pageable pageable) {
        
        
        
        return ResponseEntity.ok(service.findItemsByGroup(groupCodes, groupNames, term, pageable));
    }

    @GetMapping("/pesquisar-itens")
    public Page<ItemGroupedDTO> searchItems(ItemFilter filter, Pageable pageable) {
        return service.searchItems(filter, pageable);
    }

    @GetMapping("/detalhes-item/{idItem}")
    public ResponseEntity<ItemDetailedDTO> getDetails(
            @PathVariable Integer idItem,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(service.getItemDetailedInfo(idItem, startDate, endDate));
    }

    @GetMapping("/{idItem}/historico-compras")
    public ResponseEntity<Page<ItemHistoryDetailedDTO>> getItemHistory(
            @PathVariable Integer idItem,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(service.getItemHistoryDetails(idItem, startDate, endDate, pageable));
    }

    @GetMapping("/{idItem}/historico-compras/download-pdf")
    public ResponseEntity<byte[]> downloadHistoryPdf(
            @PathVariable Integer idItem,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<ItemHistoryDetailedDTO> data = service.getItemHistoryDetails(idItem, startDate, endDate, Pageable.unpaged()).getContent();
        if (data.isEmpty()) return ResponseEntity.noContent().build();

        byte[] pdf = pdfGeneratorService.generateItemHistoryPdf(data, idItem);
        // CORREÇÃO: Método createDownloadResponse agora existe no fim da classe
        return createDownloadResponse(pdf, "historico_item_" + idItem + ".pdf", MediaType.APPLICATION_PDF);
    }

    @GetMapping("/{idItem}/historico-compras/download-excel")
    public ResponseEntity<byte[]> downloadHistoryExcel(
            @PathVariable Integer idItem,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<ItemHistoryDetailedDTO> data = service.getItemHistoryDetails(idItem, startDate, endDate, Pageable.unpaged()).getContent();
        if (data.isEmpty()) return ResponseEntity.noContent().build();

        byte[] excel = excelGeneratorService.generateItemHistoryExcel(data);
        // CORREÇÃO: Método createDownloadResponse agora existe no fim da classe
        return createDownloadResponse(excel, "historico_item_" + idItem + ".xlsx", 
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

     @GetMapping("/itens-complemento")
    public ResponseEntity<Page<ItemComplementDTO>> getItemsWithComplement(
            @RequestParam(value = "nomeItem", required = false) String nomeItem,
            @PageableDefault(size = 50, sort = "idItem", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        String searchTerm = (nomeItem == null || nomeItem.isBlank()) ? "" : nomeItem;
        Page<ItemComplementDTO> result = queries.findItemsWithComplement(searchTerm, pageable);
        return ResponseEntity.ok(result);
    }

    private void sendEmailToApplicant(ItemSolicitationEntity entity, String subject, String messageBody) {
        // ... (Mantido igual) ...
    }

    // =============================================================================================
    // MÉTODO ADICIONADO (CORREÇÃO)
    // =============================================================================================
    private ResponseEntity<byte[]> createDownloadResponse(byte[] content, String filename, MediaType mediaType) {
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(content);
    }

    /**
     * Busca a regional do usuário de forma segura, tratando todos os possíveis erros.
     * Retorna null se não for possível obter a regional, sem interromper o fluxo da aplicação.
     * 
     * @param jwt Token JWT do usuário autenticado
     * @return String com o nome da regional ou null se não for possível obter
     */
    private String getRegionalFromUser(JwtAuthenticationToken jwt) {
        UUID userId = UUID.fromString(jwt.getName());

        try {
            log.debug("Buscando regional para usuário: {}", userId);

            EmployeeDTO employeeDTO = userService.getEmployeeFromUser(userId);
            if (employeeDTO == null) {
                log.warn("EmployeeDTO não encontrado para o usuário: {}", userId);
                return null;
            }

            if (employeeDTO.getFilial_id() == null) {
                log.debug("Usuário {} não possui filial_id associado", userId);
                return null;
            }

            Optional<Regional> regionalOpt = regionalRepository.findByFilialId(employeeDTO.getFilial_id());
            if (regionalOpt.isPresent()) {
                String regional = regionalOpt.get().getRegional();
                log.debug("Regional encontrada para usuário {}: {}", userId, regional);
                return regional;
            } else {
                log.debug("Regional não encontrada para filial_id: {}", employeeDTO.getFilial_id());
                return null;
            }

        } catch (Exception e) {
            log.error("Erro ao buscar regional do usuário {}. Erro: {}", userId, e.getMessage(), e);
            return null;
        }
    }
}