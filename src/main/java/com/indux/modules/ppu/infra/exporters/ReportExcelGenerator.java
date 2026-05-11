package com.indux.modules.ppu.infra.exporters;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.ppu.application.dtos.response.competence.ConsolidationRecord;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class ReportExcelGenerator {
    public ByteArrayResource generate(List<ConsolidationRecord> consolidations) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("Consolidação de Competência");
            int rowNum = 0;
            for (ConsolidationRecord entry : consolidations) {
                var row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.matricula());
                row.createCell(1).setCellValue(entry.evento());
                row.createCell(2).setCellValue(entry.referencia());
                row.createCell(3).setCellValue(entry.valor());
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                workbook.write(baos);
                return new ByteArrayResource(baos.toByteArray());
            }
        } catch (IOException e) {
            throw new ModuleFailure("Erro ao gerar Excel" + e.toString());
        }
    }
}
