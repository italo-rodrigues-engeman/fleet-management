package com.indux.modules.modulo_mega.presentation.controller;

import com.indux.modules.modulo_mega.application.dto.AbcCurveGroupDTO;
import com.indux.modules.modulo_mega.application.dto.AbcFilterDTO;
import com.indux.modules.modulo_mega.application.dto.CriteriaDto;
import com.indux.modules.modulo_mega.domain.enums.AbcClassificationCriteria;
import com.indux.modules.modulo_mega.service.AbcCurveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/curva-abc")
@RequiredArgsConstructor
public class AbcReportController {

    private final AbcCurveService service;

    /**
     * Retorna os dados da curva ABC para exibição em tela (JSON).
     * Aplica o limite de itens por grupo (Geralmente 10) para otimização de performance no front.
     */
    @GetMapping
    public ResponseEntity<List<AbcCurveGroupDTO>> getAbcData(@ModelAttribute AbcFilterDTO filter) {
        // O Service utiliza o AbcQueries internamente para buscar os dados via Criteria API
        return ResponseEntity.ok(service.calculateAbcCurve(filter, true));
    }

    /**
     * Gera e baixa o relatório da Curva ABC em formato PDF.
     */
    @GetMapping("/download-pdf")
    public ResponseEntity<byte[]> downloadAbcPdf(@ModelAttribute AbcFilterDTO filter) {
        byte[] pdfBytes = service.downloadAbcReport(filter);
        
        if (pdfBytes == null || pdfBytes.length == 0) {
            return ResponseEntity.noContent().build();
        }

        return createDownloadResponse(pdfBytes, "relatorio_curva_abc.pdf", MediaType.APPLICATION_PDF);
    }

    /**
     * Gera e baixa o relatório completo da Curva ABC em formato Excel (.xlsx).
     */
    @GetMapping("/download-excel")
    public ResponseEntity<byte[]> downloadAbcExcel(@ModelAttribute AbcFilterDTO filter) {
        byte[] excelBytes = service.downloadAbcReportExcel(filter);
        
        if (excelBytes == null || excelBytes.length == 0) {
            return ResponseEntity.noContent().build();
        }

        return createDownloadResponse(excelBytes, "curva_abc.xlsx", 
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    /**
     * Retorna os dados da curva ABC para exibição em tela (JSON) utilizando a nova Arquitetura (MongoDB).
     */
    @GetMapping("/v2")
    public ResponseEntity<List<AbcCurveGroupDTO>> getAbcDataNew(@ModelAttribute AbcFilterDTO filter) {
        return ResponseEntity.ok(service.calculateAbcCurveNew(filter, true));
    }

    /**
     * Gera e baixa o PDF da nova Curva ABC.
     */
    @GetMapping("/v2/download-pdf")
    public ResponseEntity<byte[]> downloadAbcPdfNew(@ModelAttribute AbcFilterDTO filter) {
        byte[] pdfBytes = service.downloadAbcReportNew(filter);
        
        if (pdfBytes == null || pdfBytes.length == 0) {
            return ResponseEntity.noContent().build();
        }

        return createDownloadResponse(pdfBytes, "relatorio_curva_abc_v2.pdf", MediaType.APPLICATION_PDF);
    }

    /**
     * Gera e baixa o Excel da nova Curva ABC.
     */
    @GetMapping("/v2/download-excel")
    public ResponseEntity<byte[]> downloadAbcExcelNew(@ModelAttribute AbcFilterDTO filter) {
        byte[] excelBytes = service.downloadAbcReportExcelNew(filter);
        
        if (excelBytes == null || excelBytes.length == 0) {
            return ResponseEntity.noContent().build();
        }

        return createDownloadResponse(excelBytes, "curva_abc_v2.xlsx", 
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    }

    /**
     * Lista os critérios de classificação disponíveis (Valor, Quantidade, Preço Médio).
     */
    @GetMapping("/abc-criterios")
    public ResponseEntity<List<CriteriaDto>> listCriteriaDetails() {
        List<CriteriaDto> details = Arrays.stream(AbcClassificationCriteria.values())
                .map(criteria -> new CriteriaDto(criteria.name(), criteria.getDescription()))
                .toList();

        return ResponseEntity.ok(details);
    }

    // =============================================================================================
    // MÉTODO AUXILIAR DE DOWNLOAD
    // =============================================================================================

    private ResponseEntity<byte[]> createDownloadResponse(byte[] content, String filename, MediaType mediaType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}