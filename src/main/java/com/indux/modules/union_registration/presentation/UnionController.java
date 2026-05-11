package com.indux.modules.union_registration.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.indux.core.application.dto.generic.GenericMessage;
import com.indux.modules.union_registration.application.dto.*;
import com.indux.modules.union_registration.application.dto.labor_rights.LaborRightsDTO;
import com.indux.modules.union_registration.application.dto.labor_rights.SalaryDTO;
import com.indux.modules.union_registration.application.service.*;
import com.indux.modules.union_registration.application.usecase.GetLaborContractAddendumDetailUseCase;
import com.indux.modules.union_registration.application.usecase.GetLaborContractDetailUseCase;
import com.indux.modules.union_registration.application.usecase.ListLaborContractAddendumsUseCase;
import com.indux.modules.union_registration.domain.enums.TipoInstrumento;
import com.indux.modules.union_registration.domain.exception.UnionNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unions")
@RequiredArgsConstructor
@Slf4j
public class UnionController {
    
    private final CreateUnionUseCase createUnionUseCase;
    
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        // Permite o binding de propriedades aninhadas usando notação de array
        binder.setAutoGrowNestedPaths(true);
        
        // Configuração específica para evitar erro de binding com notação de array
        binder.setDisallowedFields();
        
        // Permite campos aninhados profundos
        binder.setIgnoreInvalidFields(false);
        binder.setIgnoreUnknownFields(true);
    }
    private final DeleteUnionUseCase deleteUnionUseCase;
    private final GetUnionByIdUseCase getUnionByIdUseCase;
    private final GetUnionDetailedUseCase getUnionDetailedUseCase;
    private final GetUnionDetailedWithSummaryUseCase getUnionDetailedWithSummaryUseCase;
    private final GetLaborContractDetailUseCase getLaborContractDetailUseCase;
    private final ListUnionsUseCase listUnionsUseCase;
    private final UpdateUnionUseCase updateUnionUseCase;
    private final CreateLaborContractUseCase createLaborContractUseCase;
    private final CreateLaborContractAddendumUseCase createLaborContractAddendumUseCase;
    private final UpdateLaborContractUseCase updateLaborContractUseCase;
    private final DeleteLaborContractUseCase deleteLaborContractUseCase;
    private final ListLaborContractsUseCase listLaborContractsUseCase;
    private final GetLaborContractAddendumDetailUseCase getLaborContractAddendumDetailUseCase;
    private final UpdateLaborContractAddendumUseCase updateLaborContractAddendumUseCase;
    private final DeleteLaborContractAddendumUseCase deleteLaborContractAddendumUseCase;
    private final ListLaborContractAddendumsUseCase listLaborContractAddendumsUseCase;
    private final SalaryExportService salaryExportService;
    
    @PostMapping
   
    public ResponseEntity<UnionResponseDTO> createUnion(@Valid @ModelAttribute CreateUnionRequestDTO request, Authentication authentication) {
        try {
            String usuarioCriacao = authentication != null ? authentication.getName() : null;
            UnionResponseDTO response = createUnionUseCase.execute(request, usuarioCriacao);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            log.error("Erro ao criar sindicato: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Erro interno ao criar sindicato", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @DeleteMapping("/{id}")
  
    public ResponseEntity<GenericMessage> deleteUnion(@PathVariable String id) {
        try {
            GenericMessage response = deleteUnionUseCase.execute(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GenericMessage(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericMessage("Erro interno do servidor", 500));
        }
    }
    
    @PatchMapping("/{id}/deactivate")
    
    public ResponseEntity<GenericMessage> deactivateUnion(@PathVariable String id) {
        try {
            GenericMessage response = deleteUnionUseCase.softDelete(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GenericMessage(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericMessage("Erro interno do servidor", 500));
        }
    }
    
    @GetMapping
   
    public ResponseEntity<Page<UnionSimpleResponseDTO>> listUnions(
            @RequestParam(required = false) String nomeCompletoSindicato,
            @RequestParam(required = false) String cnpj,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String categoriaRepresentada,
            @RequestParam(required = false) String ufSede,
            @RequestParam(required = false) String municipioSede,
            @RequestParam(required = false) String situacaoMte,
            @RequestParam(required = false) String presidenteAtual,
            @RequestParam(required = false) String statusRegistro,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) String uf,
            @RequestParam(required = false) String ufAtendida,
            @PageableDefault(size = 20) Pageable pageable) {
        
        try {
            ListUnionsFilterDTO filters = new ListUnionsFilterDTO(
                    nomeCompletoSindicato, cnpj, tipo, categoriaRepresentada,
                    ufSede, municipioSede, situacaoMte, presidenteAtual, 
                    statusRegistro, cidade, uf, ufAtendida
            );
            
            Page<UnionSimpleResponseDTO> unions;
            if (hasAnyFilter(filters)) {
                unions = listUnionsUseCase.executeSimple(filters, pageable);
            } else {
                unions = listUnionsUseCase.listAllSimple(pageable);
            }
            
            return ResponseEntity.ok(unions);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private boolean hasAnyFilter(ListUnionsFilterDTO filters) {
        return filters.getNomeCompletoSindicato() != null ||
               filters.getCnpj() != null ||
               filters.getTipo() != null ||
               filters.getCategoriaRepresentada() != null ||
               filters.getUfSede() != null ||
               filters.getMunicipioSede() != null ||
               filters.getSituacaoMte() != null ||
               filters.getPresidenteAtual() != null ||
               filters.getStatusRegistro() != null ||
               filters.getCidade() != null ||
               filters.getUf() != null ||
               filters.getUfAtendida() != null;
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getUnionById(@PathVariable String id) {
        try {
            UnionResponseDTO union = getUnionByIdUseCase.execute(id);
            return ResponseEntity.ok(union);
        } catch (UnionNotFoundException e) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                e.getMessage(),
                404,
                java.time.LocalDateTime.now().toString(),
                "/api/unions/" + id
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            ErrorResponseDTO error = new ErrorResponseDTO(
                "Erro interno do servidor ao buscar sindicato",
                500,
                java.time.LocalDateTime.now().toString(),
                "/api/unions/" + id
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{id}/detailed")
    public ResponseEntity<?> getUnionDetailed(@PathVariable String id) {
        try {
            UnionDetailedResponseDTO union = getUnionDetailedUseCase.execute(id);
            return ResponseEntity.ok(union);
        } catch (UnionNotFoundException e) {
            log.warn("Sindicato não encontrado com ID: {}", id);
            ErrorResponseDTO error = new ErrorResponseDTO(
                e.getMessage(),
                404,
                java.time.LocalDateTime.now().toString(),
                "/api/unions/" + id + "/detailed"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            log.error("Erro interno ao buscar sindicato detalhado com ID: {}", id, e);
            ErrorResponseDTO error = new ErrorResponseDTO(
                "Erro interno do servidor ao buscar sindicato: " + e.getMessage(),
                500,
                java.time.LocalDateTime.now().toString(),
                "/api/unions/" + id + "/detailed"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/{id}/detailed-summary")
    public ResponseEntity<?> getUnionDetailedWithSummary(@PathVariable String id) {
        try {
            UnionDetailedWithSummaryDTO union = getUnionDetailedWithSummaryUseCase.execute(id);
            return ResponseEntity.ok(union);
        } catch (UnionNotFoundException e) {
            log.warn("Sindicato não encontrado com ID: {}", id);
            ErrorResponseDTO error = new ErrorResponseDTO(
                e.getMessage(),
                404,
                java.time.LocalDateTime.now().toString(),
                "/api/unions/" + id + "/detailed-summary"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            log.error("Erro interno ao buscar sindicato detalhado com resumo para ID: {}", id, e);
            ErrorResponseDTO error = new ErrorResponseDTO(
                "Erro interno do servidor ao buscar sindicato: " + e.getMessage(),
                500,
                java.time.LocalDateTime.now().toString(),
                "/api/unions/" + id + "/detailed-summary"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<UnionResponseDTO> updateUnion(
            @PathVariable String id, 
            @Valid @ModelAttribute UpdateUnionRequestDTO request,
            Authentication authentication) {
        try {
            String usuarioAtualizacao = authentication.getName();
            UnionResponseDTO response = updateUnionUseCase.execute(id, request, usuarioAtualizacao);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping(value = "/{unionId}/labor-contracts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createLaborContract(
            @PathVariable String unionId,
            @Valid @ModelAttribute CreateLaborContractRequestDTO request,
            @RequestParam(value = "arquivosInstrumento", required = false) List<org.springframework.web.multipart.MultipartFile> arquivosInstrumento,
            @RequestParam(value = "laborRightsJson", required = false) String laborRightsJson,
            Authentication authentication) {
        try {
            String usuarioCriacao = authentication.getName();
            request.setSindicatoTrabalhadoresId(unionId);
            
            // Processar laborRights se enviado como JSON
            if (laborRightsJson != null && !laborRightsJson.trim().isEmpty()) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());
                    LaborRightsDTO laborRights = objectMapper.readValue(laborRightsJson, LaborRightsDTO.class);
                    request.setLaborRights(laborRights);
                } catch (Exception e) {
                    log.error("Erro ao processar laborRights JSON: {}", e.getMessage());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new GenericMessage("Erro ao processar dados dos direitos trabalhistas: " + e.getMessage(), 400));
                }
            }
            
            LaborContractResponseDTO response = createLaborContractUseCase.execute(request, usuarioCriacao);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GenericMessage(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericMessage("Erro interno do servidor: " + e.getMessage(), 500));
        }
    }
    
    @DeleteMapping("/{unionId}/labor-contracts/{contractId}")
    public ResponseEntity<GenericMessage> deleteLaborContract(
            @PathVariable String unionId,
            @PathVariable String contractId) {
        try {
            GenericMessage response = deleteLaborContractUseCase.execute(contractId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GenericMessage(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericMessage("Erro interno do servidor: " + e.getMessage(), 500));
        }
    }
    
    @PatchMapping("/{unionId}/labor-contracts/{contractId}/deactivate")
    public ResponseEntity<GenericMessage> deactivateLaborContract(
            @PathVariable String unionId,
            @PathVariable String contractId) {
        try {
            GenericMessage response = deleteLaborContractUseCase.softDelete(contractId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GenericMessage(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericMessage("Erro interno do servidor: " + e.getMessage(), 500));
        }
    }
    
    @GetMapping("/{unionId}/labor-contracts")
    public ResponseEntity<?> listLaborContracts(
            @PathVariable String unionId,
            @RequestParam(required = false) String tipoInstrumento,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            if (tipoInstrumento != null && !tipoInstrumento.trim().isEmpty()) {
                TipoInstrumento tipo = TipoInstrumento.fromString(tipoInstrumento.trim());
                Page<LaborContractResponseDTO> contracts = listLaborContractsUseCase.executeByType(unionId, tipo, pageable);
                return ResponseEntity.ok(contracts);
            } else {
                Page<LaborContractResponseDTO> contracts = listLaborContractsUseCase.execute(unionId, pageable);
                return ResponseEntity.ok(contracts);
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GenericMessage(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericMessage("Erro interno do servidor: " + e.getMessage(), 500));
        }
    }
    
    @GetMapping("/{unionId}/labor-contracts/all")
    public ResponseEntity<?> listAllLaborContracts(@PathVariable String unionId) {
        try {
            List<LaborContractResponseDTO> contracts = listLaborContractsUseCase.executeAll(unionId);
            return ResponseEntity.ok(contracts);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GenericMessage(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericMessage("Erro interno do servidor: " + e.getMessage(), 500));
        }
    }
    
    @GetMapping("/{unionId}/labor-contracts/{contractId}")
    public ResponseEntity<?> getLaborContractDetail(
            @PathVariable String unionId,
            @PathVariable String contractId) {
        try {
            LaborContractResponseDTO contract = getLaborContractDetailUseCase.execute(contractId);
            return ResponseEntity.ok(contract);
        } catch (RuntimeException e) {
            log.warn("ACT/CCT não encontrada com ID: {}", contractId);
            ErrorResponseDTO error = new ErrorResponseDTO(
                e.getMessage(),
                404,
                java.time.LocalDateTime.now().toString(),
                "/api/unions/" + unionId + "/labor-contracts/" + contractId
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            log.error("Erro interno ao buscar ACT/CCT com ID: {}", contractId, e);
            ErrorResponseDTO error = new ErrorResponseDTO(
                "Erro interno do servidor ao buscar ACT/CCT: " + e.getMessage(),
                500,
                java.time.LocalDateTime.now().toString(),
                "/api/unions/" + unionId + "/labor-contracts/" + contractId
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PatchMapping(value = "/{unionId}/labor-contracts/{contractId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateLaborContract(
            @PathVariable String unionId,
            @PathVariable String contractId,
            @Valid @ModelAttribute UpdateLaborContractRequestDTO request,
            @RequestParam(value = "arquivosInstrumento", required = false) List<org.springframework.web.multipart.MultipartFile> arquivosInstrumento,
            @RequestParam(value = "laborRightsJson", required = false) String laborRightsJson,
            Authentication authentication) {
        try {
            String usuarioAtualizacao = authentication.getName();
            
            // Processar arquivos se fornecidos
            if (arquivosInstrumento != null && !arquivosInstrumento.isEmpty()) {
                request.setArquivosInstrumento(arquivosInstrumento);
            }
            
            // Processar laborRights se enviado como JSON
            if (laborRightsJson != null && !laborRightsJson.trim().isEmpty()) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());
                    LaborRightsDTO laborRights = objectMapper.readValue(laborRightsJson, LaborRightsDTO.class);
                    request.setLaborRights(laborRights);
                } catch (Exception e) {
                    log.error("Erro ao processar laborRightsJson: {}", e.getMessage());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new GenericMessage("Erro ao processar dados de direitos trabalhistas: " + e.getMessage(), 400));
                }
            }
            
            LaborContractResponseDTO response = updateLaborContractUseCase.execute(contractId, request, usuarioAtualizacao);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erro ao atualizar ACT/CCT com ID: {}", contractId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GenericMessage(e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Erro interno ao atualizar ACT/CCT com ID: {}", contractId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericMessage("Erro interno do servidor: " + e.getMessage(), 500));
        }
    }
    
    @PostMapping("/{unionId}/labor-contracts/{contractId}/addendums")
   
    public ResponseEntity<?> createLaborContractAddendum(
            @PathVariable String unionId,
            @PathVariable String contractId,
            @Valid @ModelAttribute CreateLaborContractAddendumRequestDTO request,
            @RequestParam(value = "laborRightsJson", required = false) String laborRightsJson,
            Authentication authentication) {
        try {
            String usuarioCriacao = authentication.getName();
            
            // Processar laborRights se enviado como JSON
            if (laborRightsJson != null && !laborRightsJson.trim().isEmpty()) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());
                    LaborRightsDTO laborRights = objectMapper.readValue(laborRightsJson, LaborRightsDTO.class);
                    request.setLaborRights(laborRights);
                } catch (Exception e) {
                    log.error("Erro ao processar laborRights JSON: {}", e.getMessage());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new GenericMessage("Erro ao processar dados dos direitos trabalhistas: " + e.getMessage(), 400));
                }
            }
            
            LaborContractAddendumResponseDTO response = createLaborContractAddendumUseCase.execute(contractId, request, usuarioCriacao);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GenericMessage(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericMessage("Erro interno do servidor: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/{unionId}/labor-contracts/{contractId}/addendums/{addendumId}")
    public ResponseEntity<?> getLaborContractAddendumDetail(
            @PathVariable String unionId,
            @PathVariable String contractId,
            @PathVariable String addendumId) {
        try {
            LaborContractAddendumResponseDTO addendum = getLaborContractAddendumDetailUseCase.execute(addendumId);
            return ResponseEntity.ok(addendum);
        } catch (RuntimeException e) {
            log.warn("Aditivo não encontrado com ID: {}", addendumId);
            ErrorResponseDTO error = new ErrorResponseDTO(
                e.getMessage(),
                404,
                java.time.LocalDateTime.now().toString(),
                "/api/unions/" + unionId + "/labor-contracts/" + contractId + "/addendums/" + addendumId
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        } catch (Exception e) {
            log.error("Erro interno ao buscar aditivo com ID: {}", addendumId, e);
            ErrorResponseDTO error = new ErrorResponseDTO(
                "Erro interno do servidor ao buscar aditivo: " + e.getMessage(),
                500,
                java.time.LocalDateTime.now().toString(),
                "/api/unions/" + unionId + "/labor-contracts/" + contractId + "/addendums/" + addendumId
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PatchMapping(value = "/{unionId}/labor-contracts/{contractId}/addendums/{addendumId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateLaborContractAddendum(
            @PathVariable String unionId,
            @PathVariable String contractId,
            @PathVariable String addendumId,
            @Valid @ModelAttribute UpdateLaborContractAddendumRequestDTO request,
            @RequestParam(value = "arquivosAnexos", required = false) List<org.springframework.web.multipart.MultipartFile> arquivosAnexos,
            @RequestParam(value = "laborRightsJson", required = false) String laborRightsJson,
            Authentication authentication) {
        try {
            // Processar arquivos se fornecidos
            if (arquivosAnexos != null && !arquivosAnexos.isEmpty()) {
                request.setArquivosAnexos(arquivosAnexos);
            }
            
            // Processar laborRights se enviado como JSON
            if (laborRightsJson != null && !laborRightsJson.trim().isEmpty()) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());
                    LaborRightsDTO laborRights = objectMapper.readValue(laborRightsJson, LaborRightsDTO.class);
                    request.setLaborRights(laborRights);
                } catch (Exception e) {
                    log.error("Erro ao processar laborRightsJson: {}", e.getMessage());
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new GenericMessage("Erro ao processar dados de direitos trabalhistas: " + e.getMessage(), 400));
                }
            }
            
            String usuarioAtualizacao = authentication.getName();
            LaborContractAddendumResponseDTO response = updateLaborContractAddendumUseCase.execute(addendumId, request, usuarioAtualizacao);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Erro ao atualizar aditivo com ID: {}", addendumId, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GenericMessage(e.getMessage(), 400));
        } catch (Exception e) {
            log.error("Erro interno ao atualizar aditivo com ID: {}", addendumId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericMessage("Erro interno do servidor: " + e.getMessage(), 500));
        }
    }

    @DeleteMapping("/{unionId}/labor-contracts/{contractId}/addendums/{addendumId}")
    public ResponseEntity<GenericMessage> deleteLaborContractAddendum(
            @PathVariable String unionId,
            @PathVariable String contractId,
            @PathVariable String addendumId) {
        try {
            GenericMessage response = deleteLaborContractAddendumUseCase.execute(addendumId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GenericMessage(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericMessage("Erro interno do servidor: " + e.getMessage(), 500));
        }
    }

    @GetMapping("/{unionId}/labor-contracts/{contractId}/addendums")
    public ResponseEntity<?> listLaborContractAddendums(
            @PathVariable String unionId,
            @PathVariable String contractId) {
        try {
            List<LaborContractAddendumResponseDTO> response = listLaborContractAddendumsUseCase.execute(contractId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new GenericMessage(e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericMessage("Erro interno do servidor: " + e.getMessage(), 500));
        }
    }
    
    @PostMapping("/salary/export")
    public ResponseEntity<?> exportSalary(
            @Valid @RequestBody SalaryDTO salaryDTO,
            @RequestParam(value = "format", defaultValue = "xlsx") String format) {
        try {
            byte[] data;
            String filename;
            String contentType;
            
            switch (format.toLowerCase()) {
                case "pdf":
                    data = salaryExportService.exportToPdf(salaryDTO);
                    filename = "salarios_beneficios.pdf";
                    contentType = "application/pdf";
                    break;
                case "csv":
                    data = salaryExportService.exportToCsv(salaryDTO);
                    filename = "salarios_beneficios.csv";
                    contentType = "text/csv; charset=UTF-8";
                    break;
                case "xlsx":
                case "excel":
                default:
                    data = salaryExportService.exportToExcel(salaryDTO);
                    filename = "salarios_beneficios.xlsx";
                    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    break;
            }
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(data);
                    
        } catch (Exception e) {
            log.error("Erro ao exportar salários no formato {}: {}", format, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenericMessage("Erro ao exportar salários: " + e.getMessage(), 500));
        }
    }
}
