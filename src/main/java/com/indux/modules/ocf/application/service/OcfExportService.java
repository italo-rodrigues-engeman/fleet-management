package com.indux.modules.ocf.application.service;

import com.indux.modules.ocf.application.dto.OccurrenceExportDTO;
import com.indux.modules.ocf.application.dto.OccurrenceFilter;
import com.indux.modules.ocf.domain.model.OcorrenciaFF;
import com.indux.modules.ocf.domain.repository.OcorrenciaFFRepository;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openpdf.text.*;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OcfExportService {
    private final OcorrenciaFFRepository repository;
    private final OccurrenceFFService occurrenceService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    /**
     * Substitui valores "selecione" por "-" para melhor apresentação na exportação
     */
    private String formatFieldValue(String value) {
        if (value == null || value.trim().isEmpty() || 
            value.toLowerCase().contains("selecione") || 
            value.equalsIgnoreCase("selecione")) {
            return "-";
        }
        return value;
    }

    public OcfExportService(OcorrenciaFFRepository repository, OccurrenceFFService occurrenceService) {
        this.repository = repository;
        this.occurrenceService = occurrenceService;
    }

    public List<OccurrenceExportDTO> getAllOccurrencesForExport(OccurrenceFilter filter, boolean hasPermission, String userId) {
        List<OcorrenciaFF> allOccurrences = new ArrayList<>();
        int pageNumber = 0;
        int pageSize = 1000; // Buscar em lotes de 1000
        Page<OcorrenciaFF> page;
        
        // Buscar todas as páginas até não haver mais
        do {
            Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "criado_em"));
            page = occurrenceService.searchFilter(filter, hasPermission, userId, pageable);
            allOccurrences.addAll(page.getContent());
            pageNumber++;
        } while (page.hasNext());
        
        return allOccurrences.stream()
                .map(this::convertToExportDTO)
                .collect(Collectors.toList());
    }

    private OccurrenceExportDTO convertToExportDTO(OcorrenciaFF occurrence) {
        // Extrair data fim do etapa_log (etapa 0 - finalização)
        LocalDateTime dataFimEtapaLog = null;
        if (occurrence.getStepLog() != null && !occurrence.getStepLog().isEmpty()) {
            // Buscar especificamente a etapa 0 (finalização)
            dataFimEtapaLog = occurrence.getStepLog().stream()
                    .filter(step -> step.getStep() == 0 && step.getFinal_at() != null)
                    .map(step -> step.getFinal_at().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                    .findFirst()
                    .orElse(null);
        }
        
        return new OccurrenceExportDTO(
                occurrence.getId(),
                occurrence.getCodeID() != null ? occurrence.getCodeID().toString() : "",
                occurrence.getOrigem() != null ? occurrence.getOrigem().toString() : "",
                occurrence.getData_Ocorrencia(),
                occurrence.getColaborador() != null ? occurrence.getColaborador().getMatricula() : "",
                occurrence.getColaborador() != null ? occurrence.getColaborador().getName() : "",
                occurrence.getColaborador() != null ? occurrence.getColaborador().getCargo() : "",
                occurrence.getSolicitante() != null ? occurrence.getSolicitante().getNome() : "",
                occurrence.getSolicitante() != null ? occurrence.getSolicitante().getEmail() : "",
                occurrence.getDescricao(),
                formatFieldValue(occurrence.getCausas()),
                occurrence.getStatus() != null ? occurrence.getStatus().toString() : "",
                occurrence.getSituacao() != null ? occurrence.getSituacao().toString() : "",
                formatFieldValue(occurrence.getPrioridade()),
                formatFieldValue(occurrence.getTipoAtendimento()),
                formatFieldValue(occurrence.getTipoFluxo()),
                occurrence.getValorContestado(),
                occurrence.getValorPagarDescontar(),
                occurrence.getTelefone(),
                formatFieldValue(occurrence.getTipoDeBeneficio()),
                formatFieldValue(occurrence.getMotivo()),
                occurrence.getDataAtendimento(),
                dataFimEtapaLog != null ? dataFimEtapaLog : occurrence.getDataFim(),
                occurrence.getAtendenteRHlocal(),
                occurrence.getAtendenteRHmatriz(),
                formatFieldValue(occurrence.getCompetencia()),
                formatFieldValue(occurrence.getDescricaoOcorrencia()),
                formatFieldValue(occurrence.getJustificativaOcorrencia()),
                occurrence.getAprovacaoGestor(),
                occurrence.getAprovacaoAnalista(),
                occurrence.getAprovacaoAnalista1(),
                occurrence.getObservacaoGestor(),
                occurrence.getObservacaoAnalista(),
                occurrence.getObservacaoAnalista1(),
                occurrence.getMotivoAnalista(),
                occurrence.getPertinente()
        );
    }

    public byte[] exportToPdf(OccurrenceFilter filter, boolean hasPermission, String userId) throws IOException {
        List<OccurrenceExportDTO> data = getAllOccurrencesForExport(filter, hasPermission, userId);
        
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 36, 36, 54, 54);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            
            addFooter(writer);
            document.open();
            
            PdfPTable headerTable = createHeader();
            document.add(headerTable);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Gerado em: " + LocalDate.now().format(DATE_FORMATTER), FontFactory.getFont(FontFactory.HELVETICA, 10)));
            document.add(new Paragraph("Total de ocorrências: " + data.size(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            document.add(new Paragraph(" "));
            
            PdfPTable table = new PdfPTable(15);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);
            
            String[] headers = {"Protocolo", "Data Ocorrência", "Data Fim", "Colaborador", "Função", "Matrícula", 
                               "Assunto", "Tipo Atend.", "Motivo", "Causa", "Competência", "Status", "Situação", 
                               "Descrição Ocorrência", "Justificativa Ocorrência"};
            
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }
            
            org.openpdf.text.Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
            for (OccurrenceExportDTO occurrence : data) {
                table.addCell(new Phrase(truncate(occurrence.numeroProtocolo(), 20), cellFont));
                table.addCell(new Phrase(occurrence.dataOcorrencia() != null ? occurrence.dataOcorrencia().format(DATE_FORMATTER) : "", cellFont));
                table.addCell(new Phrase(occurrence.dataFim() != null ? occurrence.dataFim().format(DATETIME_FORMATTER) : "", cellFont));
                table.addCell(new Phrase(truncate(formatFieldValue(occurrence.colaboradorNome()), 25), cellFont));
                table.addCell(new Phrase(truncate(formatFieldValue(occurrence.colaboradorSetor()), 25), cellFont));
                table.addCell(new Phrase(truncate(formatFieldValue(occurrence.colaboradorMatricula()), 15), cellFont));
                table.addCell(new Phrase(truncate(formatFieldValue(occurrence.tipoFluxo()), 15), cellFont));
                table.addCell(new Phrase(truncate(formatFieldValue(occurrence.tipoAtendimento()), 15), cellFont));
                table.addCell(new Phrase(truncate(formatFieldValue(occurrence.motivo()), 30), cellFont));
                table.addCell(new Phrase(truncate(formatFieldValue(occurrence.causa()), 30), cellFont));
                table.addCell(new Phrase(truncate(formatFieldValue(occurrence.competencia()), 15), cellFont));
                table.addCell(new Phrase(truncate(formatFieldValue(occurrence.status()), 15), cellFont));
                table.addCell(new Phrase(truncate(formatFieldValue(occurrence.situacao()), 15), cellFont));
                table.addCell(new Phrase(truncate(formatFieldValue(occurrence.descricaoOcorrencia()), 30), cellFont));
                table.addCell(new Phrase(truncate(formatFieldValue(occurrence.justificativaOcorrencia()), 30), cellFont));
            }
            
            document.add(table);
            document.close();
            
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            throw new IOException("Erro ao gerar PDF de ocorrências", e);
        }
    }

    private PdfPTable createHeader() {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        
        PdfPCell titleCell = new PdfPCell(new Phrase("Relatório de Ocorrências - OCF", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPadding(10);
        titleCell.setBackgroundColor(new Color(240, 240, 240));
        
        headerTable.addCell(titleCell);
        return headerTable;
    }

    private void addFooter(PdfWriter writer) {
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                Phrase footer = new Phrase("Página " + writer.getPageNumber(), FontFactory.getFont(FontFactory.HELVETICA, 8));
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, footer, 
                    (document.right() - document.left()) / 2 + document.leftMargin(), document.bottom() - 10, 0);
            }
        });
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "-";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    public byte[] exportToExcel(OccurrenceFilter filter, boolean hasPermission, String userId) throws IOException {
        List<OccurrenceExportDTO> data = getAllOccurrencesForExport(filter, hasPermission, userId);
        
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Ocorrências OCF");
            
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));
            
            CellStyle dateTimeStyle = workbook.createCellStyle();
            dateTimeStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy HH:mm"));
            
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            String[] headers = {"Protocolo", "Data Ocorrência", "Data Fim", "Colaborador", "Função", "Matrícula", 
                               "Assunto", "Tipo Atend.", "Motivo", "Causa", "Competência", "Status", "Situação", 
                               "Descrição Ocorrência", "Justificativa Ocorrência"};
            
            for (int i = 0; i < headers.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            int rowNum = 1;
            for (OccurrenceExportDTO occurrence : data) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
                
                int colNum = 0;
                // 1. Protocolo
                row.createCell(colNum++).setCellValue(occurrence.numeroProtocolo() != null ? occurrence.numeroProtocolo() : "");
                
                // 2. Data Ocorrência
                if (occurrence.dataOcorrencia() != null) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(colNum++);
                    cell.setCellValue(occurrence.dataOcorrencia().format(DATE_FORMATTER));
                } else {
                    row.createCell(colNum++).setCellValue("");
                }
                
                // 3. Data Fim
                if (occurrence.dataFim() != null) {
                    org.apache.poi.ss.usermodel.Cell cell = row.createCell(colNum++);
                    cell.setCellValue(occurrence.dataFim().format(DATETIME_FORMATTER));
                } else {
                    row.createCell(colNum++).setCellValue("");
                }
                
                // 4. Colaborador
                row.createCell(colNum++).setCellValue(formatFieldValue(occurrence.colaboradorNome()));
                
                // 5. Função
                row.createCell(colNum++).setCellValue(formatFieldValue(occurrence.colaboradorSetor()));
                
                // 6. Matrícula
                row.createCell(colNum++).setCellValue(formatFieldValue(occurrence.colaboradorMatricula()));
                
                // 7. Assunto
                row.createCell(colNum++).setCellValue(formatFieldValue(occurrence.tipoFluxo()));
                
                // 8. Tipo Atend.
                row.createCell(colNum++).setCellValue(formatFieldValue(occurrence.tipoAtendimento()));
                
                // 9. Motivo
                row.createCell(colNum++).setCellValue(formatFieldValue(occurrence.motivo()));
                
                // 10. Causa
                row.createCell(colNum++).setCellValue(formatFieldValue(occurrence.causa()));
                
                // 11. Competência
                row.createCell(colNum++).setCellValue(formatFieldValue(occurrence.competencia()));
                
                // 12. Status
                row.createCell(colNum++).setCellValue(formatFieldValue(occurrence.status()));
                
                // 13. Situação
                row.createCell(colNum++).setCellValue(formatFieldValue(occurrence.situacao()));
                
                // 14. Descrição Ocorrência
                row.createCell(colNum++).setCellValue(formatFieldValue(occurrence.descricaoOcorrencia()));
                
                // 15. Justificativa Ocorrência
                row.createCell(colNum++).setCellValue(formatFieldValue(occurrence.justificativaOcorrencia()));
            }
            
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public byte[] exportToCsv(OccurrenceFilter filter, boolean hasPermission, String userId) throws IOException {
        List<OccurrenceExportDTO> data = getAllOccurrencesForExport(filter, hasPermission, userId);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            
            String[] headers = {"Protocolo", "Data Ocorrência", "Data Fim", "Colaborador", "Função", "Matrícula", 
                               "Assunto", "Tipo Atend.", "Motivo", "Causa", "Competência", "Status", "Situação", 
                               "Descrição Ocorrência", "Justificativa Ocorrência"};
            writer.writeNext(headers);
            
            for (OccurrenceExportDTO occurrence : data) {
                String[] line = {
                    // 1. Protocolo
                    occurrence.numeroProtocolo() != null ? occurrence.numeroProtocolo() : "",
                    // 2. Data Ocorrência
                    occurrence.dataOcorrencia() != null ? occurrence.dataOcorrencia().format(DATE_FORMATTER) : "",
                    // 3. Data Fim
                    occurrence.dataFim() != null ? occurrence.dataFim().format(DATETIME_FORMATTER) : "",
                    // 4. Colaborador
                    formatFieldValue(occurrence.colaboradorNome()),
                    // 5. Função
                    formatFieldValue(occurrence.colaboradorSetor()),
                    // 6. Matrícula
                    formatFieldValue(occurrence.colaboradorMatricula()),
                    // 7. Assunto
                    formatFieldValue(occurrence.tipoFluxo()),
                    // 8. Tipo Atend.
                    formatFieldValue(occurrence.tipoAtendimento()),
                    // 9. Motivo
                    formatFieldValue(occurrence.motivo()),
                    // 10. Causa
                    formatFieldValue(occurrence.causa()),
                    // 11. Competência
                    formatFieldValue(occurrence.competencia()),
                    // 12. Status
                    formatFieldValue(occurrence.status()),
                    // 13. Situação
                    formatFieldValue(occurrence.situacao()),
                    // 14. Descrição Ocorrência
                    formatFieldValue(occurrence.descricaoOcorrencia()),
                    // 15. Justificativa Ocorrência
                    formatFieldValue(occurrence.justificativaOcorrencia())
                };
                writer.writeNext(line);
            }
            
            writer.flush();
        }
        
        return outputStream.toByteArray();
    }
}

