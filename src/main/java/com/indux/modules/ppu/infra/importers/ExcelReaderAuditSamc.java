package com.indux.modules.ppu.infra.importers;

import com.indux.core.domain.service.importers.ExcelReader;
import com.indux.modules.ppu.domain.entities.ppu.AuditableConfig;
import com.indux.modules.ppu.domain.entities.rdo.audit.SAMCRow;
import com.indux.modules.ppu.infra.exceptions.InvalidExcelFileException;
import com.indux.modules.ppu.infra.exceptions.MissingHeaderException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Component
public class ExcelReaderAuditSamc implements ExcelReader<SAMCRow> {

    @Override
    public List<SAMCRow> readerAllRows(MultipartFile file) {
        return readerSpecificHeaders(file, Map.of());
    }

    @Override
    public List<SAMCRow> readerSpecificHeaders(MultipartFile file, Map<String, String> headerMapping) {
        return readRows(file, headerMapping);
    }

    public List<SAMCRow> readerSpecificHeaders(
            MultipartFile file,
            Map<String, String> headerMapping,
            AuditableConfig auditableConfig
    ) {
        return readRows(file, headerMapping);
    }

    private List<SAMCRow> readRows(MultipartFile file, Map<String, String> internalMapping) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new MissingHeaderException("A planilha está vazia ou não contém cabeçalho.");
            }

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("pt-BR"));

            Map<String, Integer> headerIndexMap = new HashMap<>();
            Map<Integer, String> indexHeaderMap = new HashMap<>();

            int lastCellNum = headerRow.getLastCellNum();
            for (int i = 0; i < lastCellNum; i++) {
                Cell cell = headerRow.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                if (cell != null) {
                    String headerText = formatCell(cell, formatter, evaluator).trim().toLowerCase(Locale.ROOT);
                    if (!headerText.isBlank()) {
                        headerIndexMap.put(headerText, i);
                        indexHeaderMap.put(i, headerText);
                    }
                }
            }

            for (Map.Entry<String, String> entry : internalMapping.entrySet()) {
                String internalKey = entry.getKey();
                String excelHeader = entry.getValue() == null ? "" : entry.getValue().toLowerCase(Locale.ROOT);

                if ("auditableValue".equals(internalKey) && (entry.getValue() == null || entry.getValue().isBlank())) {
                    continue;
                }

                if (!headerIndexMap.containsKey(excelHeader)) {
                    throw new MissingHeaderException("Cabeçalho obrigatório '" + entry.getValue() + "' não encontrado.");
                }
            }

            List<SAMCRow> rows = new ArrayList<>();
            int lastRowNum = sheet.getLastRowNum();

            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                List<String> dataByIndex = new ArrayList<>();
                Map<String, String> dataByHeader = new HashMap<>();

                int maxCol = Math.max(lastCellNum, row.getLastCellNum());
                for (int col = 0; col < maxCol; col++) {
                    Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String val = cell == null ? "" : formatCell(cell, formatter, evaluator).trim();
                    dataByIndex.add(val);

                    String rawHeader = indexHeaderMap.get(col);
                    if (rawHeader != null) {
                        dataByHeader.put(rawHeader, val);
                    }
                }

                for (Map.Entry<String, String> entry : internalMapping.entrySet()) {
                    String internalKey = entry.getKey();
                    String excelHeader = entry.getValue() == null ? "" : entry.getValue().toLowerCase(Locale.ROOT);

                    Integer colIdx = headerIndexMap.get(excelHeader);
                    if (colIdx != null && colIdx < dataByIndex.size()) {
                        dataByHeader.put(internalKey, dataByIndex.get(colIdx));
                    }
                }

                rows.add(new SAMCRow(dataByHeader, dataByIndex));
            }

            return rows;

        } catch (IOException e) {
            throw new InvalidExcelFileException("Erro ao processar o arquivo Excel: " + e.getMessage());
        }
    }

    @Override
    public List<SAMCRow> readerSpecificColumns(MultipartFile file, List<Integer> columnIndexes) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int lastRow = sheet.getLastRowNum();

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("pt-BR"));

            List<SAMCRow> rows = new ArrayList<>();

            for (int i = 1; i <= lastRow; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                List<String> dataByIndex = new ArrayList<>();
                int maxIdx = columnIndexes.stream().max(Integer::compareTo).orElse(0);
                for (int k = 0; k <= maxIdx; k++) dataByIndex.add("");

                boolean anyNonEmpty = false;
                for (int colIdx : columnIndexes) {
                    Cell cell = row.getCell(colIdx, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String text = cell == null ? "" : formatCell(cell, formatter, evaluator).trim();
                    if (!text.isEmpty()) anyNonEmpty = true;
                    if (colIdx < dataByIndex.size()) dataByIndex.set(colIdx, text);
                }

                if (!anyNonEmpty) continue;

                rows.add(new SAMCRow(new HashMap<>(), dataByIndex));
            }

            return rows;
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler o arquivo Excel", e);
        }
    }

    @Override
    public List<String> parseUniqueColumn(MultipartFile file, String columnName) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(0);
            int columnIndex = findColumnIndex(header, columnName);

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("pt-BR"));

            return extractColumn(sheet, columnIndex, formatter, evaluator);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler o arquivo Excel", e);
        }
    }

    @Override
    public List<String> parseUniqueColumnByIndex(MultipartFile file, int columnIndex) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            DataFormatter formatter = new DataFormatter(Locale.forLanguageTag("pt-BR"));

            return extractColumn(sheet, columnIndex, formatter, evaluator);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler o arquivo Excel", e);
        }
    }

    private int findColumnIndex(Row header, String columnName) {
        if (header == null) throw new IllegalArgumentException("A planilha não contém cabeçalho.");
        for (Cell cell : header) {
            if (cell == null) continue;
            String headerText = cell.getCellType() == CellType.STRING ? cell.getStringCellValue() : cell.toString();
            if (columnName.equalsIgnoreCase(headerText.trim())) {
                return cell.getColumnIndex();
            }
        }
        throw new IllegalArgumentException("Cabeçalho '" + columnName + "' não encontrado.");
    }

    private List<String> extractColumn(Sheet sheet, int columnIndex, DataFormatter formatter, FormulaEvaluator evaluator) {
        List<String> values = new ArrayList<>();
        int lastRow = sheet.getLastRowNum();

        for (int i = 1; i <= lastRow; i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            String text = cell == null ? "" : formatCell(cell, formatter, evaluator).trim();
            if (text.isEmpty()) continue;

            values.add(text);
        }

        return values;
    }

    private String formatCell(Cell cell, DataFormatter formatter, FormulaEvaluator evaluator) {
        if (cell == null) return "";

        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().toLocalDate().toString();
        }

        if (cell.getCellType() == CellType.FORMULA && DateUtil.isCellDateFormatted(cell)) {
            double v = cell.getNumericCellValue();
            return DateUtil.getJavaDate(v).toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
                    .toString();
        }

        return formatter.formatCellValue(cell, evaluator);
    }
}
