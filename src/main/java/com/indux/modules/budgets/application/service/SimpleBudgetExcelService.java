package com.indux.modules.budgets.application.service;

import com.indux.modules.budgets.domain.model.SimpleBudget;
import com.indux.modules.budgets.domain.model.SimpleBudgetItem;
import com.indux.modules.budgets.domain.repository.SimpleBudgetRepository;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import lombok.RequiredArgsConstructor;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import com.indux.modules.budgets.domain.model.SimpleBudgetServiceItem;
import com.indux.modules.budgets.domain.model.SimpleBudgetServiceSupplier;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SimpleBudgetExcelService {

    private final SimpleBudgetRepository simpleBudgetRepository;

    public byte[] generateBudgetExcel(String budgetId) {
        SimpleBudget budget = simpleBudgetRepository.findById(budgetId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Orçamento não encontrado com ID: " + budgetId));

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Estilos
            CellStyle phaseHeaderStyle = createPhaseHeaderStyle(workbook);
            CellStyle linkStyle = createLinkStyle(workbook);
            CellStyle phaseStatusHeaderStyle = createPhaseStatusHeaderStyle(workbook);
            CellStyle phaseStatusValueStyle = createPhaseStatusValueStyle(workbook);
            CellStyle columnHeaderStyle = createColumnHeaderStyle(workbook);
            CellStyle groupHeaderStyle = createGroupHeaderStyle(workbook);
            CellStyle itemNameStyle = createItemNameStyle(workbook);
            CellStyle centerCellStyle = createCenterCellStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle highlightStyle = createHighlightStyle(workbook);

            // Merge items and services
            List<SimpleBudgetItem> mergedItems = new ArrayList<>();
            if (budget.getItens() != null) {
                mergedItems.addAll(budget.getItens());
            }

            if (budget.getServicos() != null) {
                for (SimpleBudgetServiceItem service : budget.getServicos()) {
                    SimpleBudgetItem itemFromService = SimpleBudgetItem.builder()
                            .nome(service.getNome())
                            .descricao(service.getDescricao())
                            .fase(service.getFase() != null ? service.getFase() : "1") // Default phase 1 if null
                            .grupoItem(service.getGrupoItem() != null ? service.getGrupoItem() : "SERVIÇOS")
                            .tipoItem(service.getTipo())
                            .build();

                    // Populate from selected supplier if exists
                    if (service.getFornecedores() != null) {
                        service.getFornecedores().stream()
                                .filter(s -> Boolean.TRUE.equals(s.getSelecionado()))
                                .findFirst()
                                .ifPresent(supplier -> {
                                    itemFromService.setUnidadeMedida(supplier.getUnidadeMedida());
                                    itemFromService.setQuantidade(supplier.getQuantidade());
                                    itemFromService.setValorOrcamento(supplier.getValorOrcamento());
                                    itemFromService.setValorConsiderado(supplier.getValorConsiderado());
                                    // Use supplier values for other fields if needed
                                });
                    }
                    mergedItems.add(itemFromService);
                }
            }

            // Agrupar itens por fase
            Map<String, List<SimpleBudgetItem>> itemsByPhase = mergedItems.stream()
                    .collect(Collectors.groupingBy(
                            item -> item.getFase() != null ? item.getFase() : "SEM FASE",
                            java.util.LinkedHashMap::new,
                            Collectors.toList()
                    ));

            // Criar uma aba para cada fase
            for (Map.Entry<String, List<SimpleBudgetItem>> phaseEntry : itemsByPhase.entrySet()) {
                String phaseName = phaseEntry.getKey();
                List<SimpleBudgetItem> phaseItems = phaseEntry.getValue();

                // Nome da aba (limitado a 31 caracteres)
                String sheetName = "FASE-F." + String.format("%02d", getPhaseName(phaseName));
                if (sheetName.length() > 31) {
                    sheetName = sheetName.substring(0, 31);
                }
                
                Sheet sheet = workbook.createSheet(sheetName);

                int rowIdx = 0;

                // Linha 1: Título da fase | Summary Headers | New Summary Headers
                Row row1 = sheet.createRow(rowIdx++); // Index 0
                row1.setHeightInPoints(30);
                
                // Mesclar A1:D1
                sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 3));

                // A-D: Título
                Cell titleCell = row1.createCell(0);
                titleCell.setCellValue(sheetName + ".");
                titleCell.setCellStyle(phaseHeaderStyle);
                
                // Bordas para B, C, D
                for (int i = 1; i < 4; i++) {
                   Cell cell = row1.createCell(i);
                   cell.setCellStyle(phaseHeaderStyle);
                }

                // E: DEPRECIAÇÃO (Gray Header)
                Cell depHeaderCell = row1.createCell(4);
                depHeaderCell.setCellValue("DEPRECIAÇÃO");
                depHeaderCell.setCellStyle(phaseStatusHeaderStyle);

                // F: PRAZO ITEM (Gray Header)
                Cell prazoHeaderCell = row1.createCell(5);
                prazoHeaderCell.setCellValue("PRAZO ITEM");
                prazoHeaderCell.setCellStyle(phaseStatusHeaderStyle);

                // G: CUSTO TOTAL (White Value Style as Header)
                Cell custoTotalHeaderCell = row1.createCell(6);
                custoTotalHeaderCell.setCellValue("CUSTO TOTAL");
                custoTotalHeaderCell.setCellStyle(phaseStatusValueStyle);

                // H: VALOR DIFAL (Gray Header)
                Cell difalHeaderCell = row1.createCell(7);
                difalHeaderCell.setCellValue("VALOR DIFAL");
                difalHeaderCell.setCellStyle(phaseStatusHeaderStyle);

                // Styles needed
                CellStyle boldCenterStyle = createBoldCenterStyle(workbook);
                CellStyle grayValueStyle = createGrayValueStyle(workbook);

                // N: MENSAL =
                Cell mensalHeaderCell = row1.createCell(13);
                mensalHeaderCell.setCellValue("MENSAL =");
                mensalHeaderCell.setCellStyle(boldCenterStyle);

                // O: LOCAÇÃO (Yellow)
                Cell locacaoHeaderCell = row1.createCell(14);
                locacaoHeaderCell.setCellValue("LOCAÇÃO");
                locacaoHeaderCell.setCellStyle(createYellowHeaderStyle(workbook));

                // P: COMPRA =
                Cell compraHeaderCell = row1.createCell(15);
                compraHeaderCell.setCellValue("COMPRA =");
                compraHeaderCell.setCellStyle(boldCenterStyle);

                // R: COMPRA
                Cell compraLabelCell = row1.createCell(17);
                compraLabelCell.setCellValue("COMPRA");
                compraLabelCell.setCellStyle(boldCenterStyle);


                // Linha 2: Link | Phase Status | Summary Values | Lower Summary Labels
                Row row2 = sheet.createRow(rowIdx++); // Index 1

                // B: VOLTAR PARA O DEOR
                Cell linkCell = row2.createCell(1);
                linkCell.setCellValue("VOLTAR PARA O DEOR");
                linkCell.setCellStyle(linkStyle);
                Hyperlink link = workbook.getCreationHelper().createHyperlink(HyperlinkType.URL);
                link.setAddress("#");
                linkCell.setHyperlink(link);
                
                // C: FASE ATIVA
                Cell phaseActiveHeaderCell = row2.createCell(2);
                phaseActiveHeaderCell.setCellValue("FASE ATIVA");
                phaseActiveHeaderCell.setCellStyle(phaseStatusHeaderStyle);
                
                // D: Sim
                Cell phaseActiveValueCell = row2.createCell(3);
                phaseActiveValueCell.setCellValue("Sim");
                phaseActiveValueCell.setCellStyle(phaseStatusValueStyle);

                // E: 70% (Gray Value)
                Cell depValueCell = row2.createCell(4);
                depValueCell.setCellValue(0.70);
                CellStyle percentStyle = workbook.createCellStyle();
                percentStyle.cloneStyleFrom(grayValueStyle);
                percentStyle.setDataFormat(workbook.createDataFormat().getFormat("0%"));
                depValueCell.setCellStyle(percentStyle);

                // F: 2,0 (Gray Value)
                Cell prazoValueCell = row2.createCell(5);
                prazoValueCell.setCellValue(2.0);
                CellStyle numberStyle = workbook.createCellStyle();
                numberStyle.cloneStyleFrom(grayValueStyle);
                numberStyle.setDataFormat(workbook.createDataFormat().getFormat("0.0"));
                prazoValueCell.setCellStyle(numberStyle);

                // G: 0 (White Value)
                Cell custoTotalCell = row2.createCell(6);
                custoTotalCell.setCellValue(0);
                custoTotalCell.setCellStyle(phaseStatusValueStyle);

                // H: #N/D (Gray Value)
                Cell difalCell = row2.createCell(7);
                difalCell.setCellFormula("NA()");
                difalCell.setCellStyle(grayValueStyle);

                // N: CUSTO FIXO =
                Cell custoFixoHeaderCell = row2.createCell(13);
                custoFixoHeaderCell.setCellValue("CUSTO FIXO =");
                custoFixoHeaderCell.setCellStyle(boldCenterStyle);

                // O: VALOR S_ DEPREC (Orange)
                Cell valorSDeprecCell = row2.createCell(14);
                valorSDeprecCell.setCellValue("VALOR S_ DEPREC");
                valorSDeprecCell.setCellStyle(createOrangeHeaderStyle(workbook));


                // S-U (Merged): Button "GERAR LISTA DE COMPRAS"
                // Merging S1:U1 and S2:U2 is not standard for a single button usually, but let's see. 
                // The image shows it above the table. It seems to span S-U in Row 1 (and maybe merged down?).
                // Let's assume it's in Row 1 merged from S to U.
                // Looking at the image, it's a large button above headers.
                // Row 1 & 2 are summary sections. 
                // Let's merge S1:U2 for a big button? Or just S1:U1?
                // The previous step defined Row 1 and Row 2.
                // Let's merge S1:U2 for the button block to match the height of the summary section.
                sheet.addMergedRegion(new CellRangeAddress(0, 1, 18, 20)); // S=18, U=20 (0-indexed)
                Cell btnCell = row1.createCell(18); // S1
                btnCell.setCellValue("GERAR LISTA DE COMPRAS");
                CellStyle btnStyle = workbook.createCellStyle();
                btnStyle.cloneStyleFrom(boldCenterStyle);
                btnStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); // Looks like a button
                btnStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                Font btnFont = workbook.createFont();
                btnFont.setBold(true);
                btnFont.setFontHeightInPoints((short) 14);
                btnStyle.setFont(btnFont);
                btnStyle.setBorderBottom(BorderStyle.THICK);
                btnStyle.setBorderTop(BorderStyle.THICK);
                btnStyle.setBorderLeft(BorderStyle.THICK);
                btnStyle.setBorderRight(BorderStyle.THICK);
                btnCell.setCellStyle(btnStyle);
                 // Create cells for S-U in row 1 & 2 to complete the merged region styling if needed, but usually strictly not required if merged.

                // Linha 3: Cabeçalhos das colunas (Starts at index 2)
                Row headerRow = sheet.createRow(rowIdx++);
                String[] headers = {
                    "COD.\nITEM", 
                    "EQUIPAMENTO (nome do item)", 
                    "QTDE", 
                    "CUSTO\nCOMPRA /\nLOCAÇÃO",
                    "DEPRECIAÇÃO",
                    "DEPRECIAÇÃO MENSAL",
                    "MANUTENÇÃO MENSAL",
                    "OUTROS CUSTOS",
                    "SEGURO MENSAL",
                    "KM/DIA",
                    "KM/L",
                    "R$/L",
                    "DIAS/MÊS",
                    "COMBUSTÍVEL\nMENSAL",
                    "TOTAL MENSAL",
                    "CUSTO TOTAL\nCONTRATO",
                    "", // Empty column Q
                    "TIPO", // R
                    "MODALIDADE", // S
                    "INVESTIMENTO TOTAL", // T
                    "DEPRECIAÇÃO TOTAL", // U
                    "RESIDUAL TOTAL", // V
                    "DEMAIS CUSTOS:\nMANUTENÇÃO", // W
                    "DEMAIS CUSTOS:\nLICENCIAMENTOS/FRETES", // X
                    "DEMAIS CUSTOS:\nSEGUROS", // Y
                    "COMBUSTÍVEIS", // Z
                    "CÓDIGO GRUPO", // AA (Yellow)
                    "DESCRIÇÃO GRUPO", // AB (Yellow)
                    "Unidade Medida", // AC (Red)
                    "TIPO", // AD (Red)
                    "VALOR VENDA\nUNIT.", // AE
                    "VALOR TOTAL\nVENDA S/ CRÉDITO", // AF
                    "VALOR\nPIS", // AG
                    "VALOR CRÉDITO\nPIS/COFINS ISS", // AH (Red Text)
                    "VALOR TOTAL\nVENDA C/ CRÉDITO", // AI (Green Text)
                    "", // AJ (Separator)
                    "PESO %" // AK (Blue Text)
                };
                
                // Styles for specific headers
                CellStyle blueHeaderStyle = createBlueHeaderStyle(workbook);
                CellStyle redHeaderStyle = createRedHeaderStyle(workbook); // New
                CellStyle yellowHeaderStyle = createYellowHeaderStyle(workbook);
                CellStyle redTextHeaderStyle = createRedTextHeaderStyle(workbook); // New
                CellStyle greenTextHeaderStyle = createGreenTextHeaderStyle(workbook); // New

                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    if (i == 13 || i == 15 || i == 36) { // COMBUSTÍVEL MENSAL, CUSTO TOTAL CONTRATO, PESO % (Blue Text)
                         cell.setCellStyle(blueHeaderStyle);
                    } else if (i == 16 || i == 35) { // Empty columns Q, AJ
                         cell.setCellStyle(columnHeaderStyle); // Default
                    } else if (i == 26 || i == 27) { // CÓDIGO GRUPO, DESCRIÇÃO GRUPO (Yellow BG)
                        cell.setCellStyle(yellowHeaderStyle);
                    } else if (i == 28 || i == 29) { // UM, TIPO (Red BG)
                        cell.setCellStyle(redHeaderStyle);
                    } else if (i == 33) { // VALOR CRÉDITO PIS/COFINS ISS (Red Text)
                        cell.setCellStyle(redTextHeaderStyle);
                    } else if (i == 34) { // VALOR TOTAL VENDA C/ CRÉDITO (Green Text)
                        cell.setCellStyle(greenTextHeaderStyle);
                    } else {
                        cell.setCellStyle(columnHeaderStyle);
                    }
                }

                // Agrupar itens por grupoItem dentro desta fase
                Map<String, List<SimpleBudgetItem>> itemsByGroup = phaseItems.stream()
                        .collect(Collectors.groupingBy(
                                item -> item.getGrupoItem() != null ? item.getGrupoItem() : "SEM GRUPO",
                                java.util.LinkedHashMap::new,
                                Collectors.toList()
                        ));

                // Processar cada grupo
                int groupIdx = 1;
                for (Map.Entry<String, List<SimpleBudgetItem>> entry : itemsByGroup.entrySet()) {
                    String groupName = entry.getKey();
                    if (!groupName.toUpperCase().startsWith("ITEM ")) {
                         groupName = "ITEM " + groupIdx + " - " + groupName;
                    }
                    groupIdx++;

                    List<SimpleBudgetItem> items = entry.getValue();

                    // Linha do grupo (ex: ITEM 1 - VEÍCULOS / EQUIPAMENTOS / MATERIAIS DE IMPLANTAÇÃO)
                    Row groupRow = sheet.createRow(rowIdx++);
                    
                    // Mesclar células para o título do grupo
                    sheet.addMergedRegion(new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, 1));
                    
                    Cell groupCell = groupRow.createCell(0);
                    groupCell.setCellValue(groupName);
                    groupCell.setCellStyle(groupHeaderStyle);
                    
                    // Células vazias com borda para colunas restantes
                    for(int c = 2; c < headers.length; c++) {
                        if (c == 16 || c == 35) continue; // Skip separator columns
                        Cell emptyCell = groupRow.createCell(c);
                        emptyCell.setCellValue("-");
                        emptyCell.setCellStyle(centerCellStyle);
                    }

                    // Itens do grupo
                    for (SimpleBudgetItem item : items) {
                        Row itemRow = sheet.createRow(rowIdx++);
                        int col = 0;

                        // COD. ITEM (codMega)
                        Cell codCell = itemRow.createCell(col++);
                        if (item.getCodMega() != null) {
                            codCell.setCellValue(item.getCodMega());
                        }
                        codCell.setCellStyle(centerCellStyle);

                        // Nome do item (indentado)
                        Cell nameCell = itemRow.createCell(col++);
                        nameCell.setCellValue("    " + (item.getNome() != null ? item.getNome() : ""));
                        nameCell.setCellStyle(itemNameStyle);

                        // QTDE
                        Cell qtdeCell = itemRow.createCell(col++);
                        if (item.getQuantidade() != null && item.getQuantidade() > 0) {
                            qtdeCell.setCellValue(item.getQuantidade());
                            qtdeCell.setCellStyle(highlightStyle);
                        } else {
                            qtdeCell.setCellStyle(centerCellStyle);
                        }

                        // CUSTO
                        Cell custoCell = itemRow.createCell(col++);
                        if (item.getValorConsiderado() != null && item.getValorConsiderado().compareTo(BigDecimal.ZERO) > 0) {
                            custoCell.setCellValue(item.getValorConsiderado().doubleValue());
                            custoCell.setCellStyle(currencyStyle);
                        } else {
                            custoCell.setCellStyle(centerCellStyle);
                        }
                        
                        // DEPRECIAÇÃO
                        Cell depCell = itemRow.createCell(col++);
                        depCell.setCellValue(0);
                        depCell.setCellStyle(centerCellStyle);

                        // DEPRECIAÇÃO MENSAL
                        Cell depMenCell = itemRow.createCell(col++);
                        depMenCell.setCellValue(0);
                        depMenCell.setCellStyle(centerCellStyle);

                        // MANUTENÇÃO MENSAL
                        Cell manCell = itemRow.createCell(col++);
                        manCell.setCellStyle(centerCellStyle);

                        // OUTROS CUSTOS
                        Cell outrosCell = itemRow.createCell(col++);
                        outrosCell.setCellStyle(centerCellStyle);
                        
                        // SEGURO MENSAL
                        Cell seguroCell = itemRow.createCell(col++);
                        seguroCell.setCellStyle(centerCellStyle);

                        // KM/DIA
                        Cell kmDiaCell = itemRow.createCell(col++);
                        kmDiaCell.setCellStyle(centerCellStyle);

                        // KM/L
                        Cell kmLCell = itemRow.createCell(col++);
                        kmLCell.setCellStyle(centerCellStyle);

                        // R$/L
                        Cell rSLCell = itemRow.createCell(col++);
                        rSLCell.setCellStyle(centerCellStyle);

                        // DIAS/MÊS
                        Cell diasMesCell = itemRow.createCell(col++);
                        diasMesCell.setCellStyle(centerCellStyle);

                        // COMBUSTÍVEL MENSAL (0)
                        Cell combMensalCell = itemRow.createCell(col++);
                        combMensalCell.setCellValue(0.0);
                        combMensalCell.setCellStyle(centerCellStyle);

                        // TOTAL MENSAL (0)
                        Cell totalMensalCell = itemRow.createCell(col++);
                        totalMensalCell.setCellValue(0.0);
                        totalMensalCell.setCellStyle(centerCellStyle);

                        // CUSTO TOTAL CONTRATO (0)
                        Cell custoTotalContratoCell = itemRow.createCell(col++);
                        custoTotalContratoCell.setCellValue(0.0);
                        custoTotalContratoCell.setCellStyle(centerCellStyle);

                        // Empty Column (skip)
                         col++; 

                        // TIPO
                        Cell tipoCell = itemRow.createCell(col++);
                        tipoCell.setCellValue("");
                        tipoCell.setCellStyle(centerCellStyle);

                        // MODALIDADE - Yellow for "MENSAL"
                        Cell modalidadeCell = itemRow.createCell(col++);
                        modalidadeCell.setCellValue("");
                        modalidadeCell.setCellStyle(createYellowHeaderStyle(workbook)); // Reusing yellow style for cell body

                        // INVESTIMENTO TOTAL
                        Cell investTotalCell = itemRow.createCell(col++);
                        investTotalCell.setCellValue(0);
                        investTotalCell.setCellStyle(centerCellStyle);

                        // DEPRECIAÇÃO TOTAL
                        Cell deprecTotalCell = itemRow.createCell(col++);
                        deprecTotalCell.setCellValue(0);
                        deprecTotalCell.setCellStyle(centerCellStyle);

                        // RESIDUAL TOTAL
                        Cell residualTotalCell = itemRow.createCell(col++);
                        residualTotalCell.setCellValue(0);
                        residualTotalCell.setCellStyle(centerCellStyle);

                        // DEMAIS CUSTOS: MANUTENÇÃO
                        Cell demaisManuCell = itemRow.createCell(col++);
                        demaisManuCell.setCellValue(0);
                        demaisManuCell.setCellStyle(centerCellStyle);

                        // DEMAIS CUSTOS: LICENCIAMENTOS/FRETES
                        Cell demaisLicencCell = itemRow.createCell(col++);
                        demaisLicencCell.setCellValue(0);
                        demaisLicencCell.setCellStyle(centerCellStyle);

                        // DEMAIS CUSTOS: SEGUROS
                        Cell demaisSegurosCell = itemRow.createCell(col++);
                        demaisSegurosCell.setCellValue(0);
                        demaisSegurosCell.setCellStyle(centerCellStyle);

                        // COMBUSTÍVEIS
                        Cell combustiveisCell = itemRow.createCell(col++);
                        combustiveisCell.setCellValue(0);
                        combustiveisCell.setCellStyle(centerCellStyle);

                        // CÓDIGO GRUPO (Yellow)
                        Cell codGrupoCell = itemRow.createCell(col++);
                        if (item.getCodGrupo() != null) {
                            codGrupoCell.setCellValue(item.getCodGrupo()); // Assuming e.g. "12.12.11"
                        }
                        codGrupoCell.setCellStyle(createYellowHeaderStyle(workbook));

                        // DESCRIÇÃO GRUPO (Yellow)
                        Cell descGrupoCell = itemRow.createCell(col++);
                        if (item.getGrupo() != null) {
                            descGrupoCell.setCellValue(item.getGrupo());
                        }
                        descGrupoCell.setCellStyle(createYellowHeaderStyle(workbook));

                        // UM (Red Header style)
                        Cell umCell = itemRow.createCell(col++);
                        if (item.getUnidadeMedida() != null) {
                            umCell.setCellValue(item.getUnidadeMedida());
                        } else {
                            umCell.setCellValue("-");
                        }
                        umCell.setCellStyle(centerCellStyle);

                        // TIPO (Red Header)
                        Cell tipo2Cell = itemRow.createCell(col++);
                        if (item.getTipoItem() != null) {
                            tipo2Cell.setCellValue(item.getTipoItem());
                        } else {
                            tipo2Cell.setCellValue("-");
                        }
                        tipo2Cell.setCellStyle(centerCellStyle);

                        // VALOR VENDA UNIT
                        Cell vendaUnitCell = itemRow.createCell(col++);
                        vendaUnitCell.setCellValue(0);
                        vendaUnitCell.setCellStyle(centerCellStyle);

                        // VALOR TOTAL VENDA
                        Cell vendaTotalCell = itemRow.createCell(col++);
                        vendaTotalCell.setCellFormula("NA()"); // #DIV/0! in image often means formula error
                        vendaTotalCell.setCellStyle(centerCellStyle);

                        // VALOR PIS
                        Cell pisCell = itemRow.createCell(col++);
                        pisCell.setCellValue(0);
                        pisCell.setCellStyle(centerCellStyle);

                        // VALOR CRÉDITO PIS/COFINS ISS (AH)
                        Cell creditoPisCell = itemRow.createCell(col++);
                        creditoPisCell.setCellFormula("NA()");
                        creditoPisCell.setCellStyle(centerCellStyle);

                        // VALOR TOTAL VENDA C/ CRÉDITO (AI)
                        Cell totalVendaCreditoCell = itemRow.createCell(col++);
                        totalVendaCreditoCell.setCellFormula("NA()");
                        totalVendaCreditoCell.setCellStyle(centerCellStyle);

                        // Empty Column AJ
                        col++;

                        // PESO % (AK)
                        Cell pesoCell = itemRow.createCell(col++);
                        pesoCell.setCellFormula("NA()");
                        pesoCell.setCellStyle(centerCellStyle);
                    }
                }

                // Ajustar largura das colunas
                sheet.setColumnWidth(0, 2500);  // COD. ITEM
                sheet.setColumnWidth(1, 18000); // EQUIPAMENTO
                sheet.setColumnWidth(2, 3000);  // QTDE
                sheet.setColumnWidth(3, 4500);  // CUSTO
                sheet.setColumnWidth(4, 4500);  // DEPRECIAÇÃO
                sheet.setColumnWidth(5, 4500);  // DEPRECIAÇÃO MENSAL
                sheet.setColumnWidth(6, 4500);  // MANUTENÇÃO MENSAL
                sheet.setColumnWidth(7, 4500);  // OUTROS CUSTOS
                sheet.setColumnWidth(8, 4500);  // SEGURO MENSAL
                // Novas colunas
                sheet.setColumnWidth(9, 3000); // KM/DIA
                sheet.setColumnWidth(10, 3000); // KM/L
                sheet.setColumnWidth(11, 3000); // R$/L
                sheet.setColumnWidth(12, 3000); // DIAS/MÊS
                sheet.setColumnWidth(13, 5000); // COMBUSTÍVEL MENSAL
                sheet.setColumnWidth(14, 5000); // TOTAL MENSAL
                sheet.setColumnWidth(15, 5000); // CUSTO TOTAL CONTRATO
                sheet.setColumnWidth(16, 1000); // Empty
                sheet.setColumnWidth(17, 5000); // TIPO
                sheet.setColumnWidth(18, 5000); // MODALIDADE
                sheet.setColumnWidth(19, 5000); // INVESTIMENTO TOTAL
                sheet.setColumnWidth(20, 5000); // DEPRECIAÇÃO TOTAL
                sheet.setColumnWidth(21, 5000); // RESIDUAL TOTAL
                sheet.setColumnWidth(22, 5000); // DEMAIS: MANU
                sheet.setColumnWidth(23, 6000); // DEMAIS: LICENC
                sheet.setColumnWidth(24, 5000); // DEMAIS: SEGUROS
                sheet.setColumnWidth(25, 5000); // COMBUSTÍVEIS
                sheet.setColumnWidth(26, 6000); // CÓDIGO GRUPO
                sheet.setColumnWidth(27, 8000); // DESCRIÇÃO GRUPO
                sheet.setColumnWidth(28, 5000); // Unidade Medida
                sheet.setColumnWidth(29, 3000); // TIPO
                sheet.setColumnWidth(30, 5000); // VALOR VENDA
                sheet.setColumnWidth(31, 5000); // VALOR TOTAL
                sheet.setColumnWidth(32, 5000); // VALOR PIS
                sheet.setColumnWidth(33, 5000); // CRÉDITO PIS
                sheet.setColumnWidth(34, 5000); // TOTAL VENDA CREDITO
                sheet.setColumnWidth(35, 1000); // Empty AJ
                sheet.setColumnWidth(36, 5000); // PESO %

            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar Excel do orçamento: " + e.getMessage(), e);
        }
    }

    private int getPhaseName(String fase) {
        try {
            return Integer.parseInt(fase);
        } catch (NumberFormatException e) {
            return 1; // Default para fase 1 se não conseguir converter
        }
    }

    private CellStyle createPhaseHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createLinkStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setUnderline(Font.U_SINGLE);
        font.setColor(IndexedColors.BLUE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createPhaseStatusHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createPhaseStatusValueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.BLUE.getIndex());
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createColumnHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createGroupHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createItemNameStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createCenterCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.000"));
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHighlightStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createBlueHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLUE.getIndex());
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createYellowHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LEMON_CHIFFON.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createOrangeHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.TAN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createGrayValueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.BLUE.getIndex());
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createBoldCenterStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    private CellStyle createRedHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.RED.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    private CellStyle createRedTextHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.RED.getIndex());
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createGreenTextHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.GREEN.getIndex());
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }
}
