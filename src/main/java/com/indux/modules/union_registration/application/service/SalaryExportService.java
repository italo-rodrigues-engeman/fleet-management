package com.indux.modules.union_registration.application.service;

import com.indux.modules.union_registration.application.dto.labor_rights.BeneficioEstruturadoDTO;
import com.indux.modules.union_registration.application.dto.labor_rights.SalaryDTO;
import com.indux.modules.union_registration.application.dto.labor_rights.SalaryFunctionDTO;
import com.opencsv.CSVWriter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openpdf.text.*;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class SalaryExportService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private String formatFieldValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "-";
        }
        return value;
    }
    
    private String formatBeneficio(BeneficioEstruturadoDTO beneficio) {
        if (beneficio == null) {
            return "-";
        }
        
        StringBuilder sb = new StringBuilder();
        if (beneficio.getPeriodicidade() != null) {
            sb.append("Per: ").append(beneficio.getPeriodicidade());
        }
        if (beneficio.getValorPercentual() != null) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("Valor %: ").append(beneficio.getValorPercentual()).append("%");
        }
        if (beneficio.getValorReais() != null) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("Valor R$: ").append(beneficio.getValorReais());
        }
        
        return sb.length() > 0 ? sb.toString() : "-";
    }
    
    private String formatBeneficioCompacto(BeneficioEstruturadoDTO beneficio) {
        if (beneficio == null) {
            return "-";
        }
        
        StringBuilder sb = new StringBuilder();
        if (beneficio.getPeriodicidade() != null) {
            // Periodicidade com abreviação mais legível
            String per = beneficio.getPeriodicidade();
            if (per.equalsIgnoreCase("Mensal")) {
                sb.append("Mensal");
            } else if (per.equalsIgnoreCase("Anual")) {
                sb.append("Anual");
            } else if (per.equalsIgnoreCase("Diário")) {
                sb.append("Diário");
            } else if (per.contains("necessário")) {
                sb.append("Qdo Necessário");
            } else {
                sb.append(per);
            }
        }
        if (beneficio.getValorPercentual() != null) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append(beneficio.getValorPercentual()).append("%");
        }
        if (beneficio.getValorReais() != null) {
            if (sb.length() > 0) sb.append(" | ");
            // Formatar valor em reais
            String valor = beneficio.getValorReais().toString();
            if (valor.contains(".")) {
                // Se tiver casas decimais, mostrar apenas 2
                int dotIndex = valor.indexOf(".");
                if (dotIndex + 3 < valor.length()) {
                    valor = valor.substring(0, dotIndex + 3);
                }
            }
            sb.append("R$ ").append(valor);
        }
        
        return sb.length() > 0 ? sb.toString() : "-";
    }
    
    public byte[] exportToPdf(SalaryDTO salaryDTO) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // A4 landscape - criar Rectangle com dimensões explicitamente invertidas
            // No openpdf, Rectangle(width, height) onde width > height = landscape
            // A4 portrait padrão: 595 x 842 (width x height)
            // A4 landscape: 842 x 595 (width x height) - largura > altura
            Rectangle a4Landscape = new Rectangle(842f, 595f);
            Document document = new Document(a4Landscape, 20, 20, 40, 40);
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            
            addFooter(writer);
            document.open();
            
            PdfPTable headerTable = createHeader();
            document.add(headerTable);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Gerado em: " + LocalDate.now().format(DATE_FORMATTER), 
                FontFactory.getFont(FontFactory.HELVETICA, 10)));
            
            // Informações gerais
            if (salaryDTO.getPisoSalarial() != null || 
                salaryDTO.getDataBase() != null || salaryDTO.getPorcentagemReajuste() != null ||
                salaryDTO.getDissidio() != null) {
                PdfPTable infoTable = new PdfPTable(2);
                infoTable.setWidthPercentage(100);
                infoTable.setSpacingBefore(10f);
                infoTable.setSpacingAfter(10f);
                
                addInfoRow(infoTable, "Piso Salarial", formatFieldValue(salaryDTO.getPisoSalarial()));
                addInfoRow(infoTable, "Data Base", salaryDTO.getDataBase() != null ? 
                    salaryDTO.getDataBase().format(DATE_FORMATTER) : "-");
                addInfoRow(infoTable, "Porcentagem Reajuste", formatFieldValue(salaryDTO.getPorcentagemReajuste()));
                addInfoRow(infoTable, "Dissídio", formatFieldValue(salaryDTO.getDissidio()));
                
                document.add(infoTable);
                document.add(new Paragraph(" "));
            }
            
            // Tabela de funções
            if (salaryDTO.getFuncoes() != null && !salaryDTO.getFuncoes().isEmpty()) {
                document.add(new Paragraph("Funções e Benefícios", 
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
                document.add(new Paragraph(" "));
                
                PdfPTable table = createFunctionsTable(salaryDTO.getFuncoes());
                document.add(table);
            }
            
            document.close();
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            throw new IOException("Erro ao gerar PDF de salários", e);
        }
    }
    
    private PdfPTable createHeader() {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        
        PdfPCell titleCell = new PdfPCell(new Phrase("Relatório de Salários e Benefícios", 
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setPadding(10);
        titleCell.setBackgroundColor(new Color(240, 240, 240));
        
        headerTable.addCell(titleCell);
        return headerTable;
    }
    
    private void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label + ":", 
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
        labelCell.setPadding(5);
        labelCell.setBackgroundColor(new Color(245, 245, 245));
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, 
            FontFactory.getFont(FontFactory.HELVETICA, 10)));
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }
    
    private PdfPTable createFunctionsTable(List<SalaryFunctionDTO> funcoes) {
        // Criar tabela com todas as colunas (4 campos básicos + 36 benefícios = 40 colunas)
        PdfPTable table = new PdfPTable(40);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);
        
        // Definir larguras das colunas (proporcional) - aumentadas para melhor legibilidade
        try {
            float[] columnWidths = new float[40];
            // Colunas básicas (mais largas para melhor visualização)
            columnWidths[0] = 4f; // Função ACT
            columnWidths[1] = 2.5f; // Código CBO
            columnWidths[2] = 5f; // Função CBO
            columnWidths[3] = 3f; // Salário Base
            // Benefícios (larguras aumentadas para melhor legibilidade)
            for (int i = 4; i < 40; i++) {
                columnWidths[i] = 3.5f;
            }
            table.setWidths(columnWidths);
        } catch (DocumentException e) {
            // Se falhar, usar largura padrão
        }
        
        String[] headers = {
            "Função ACT", "Código CBO", "Função CBO", "Salário Base",
            "Hora Extra 1", "Hora Extra 2", "Hora Extra 3", "Adic. Noturno",
            "Adic. Insalub.", "Adic. Pericul.", "Adic. Sobreaviso", "Adic. Prontidão",
            "Café Manhã", "Almoço", "Lanche", "Lanche Parada",
            "Vale Aliment.", "Vale Alim. Parada", "Vale Refeição", "Vale Refeição Parada",
            "Cesta Básica", "Cesta Natalina", "Plano Odonto", "Plano Saúde",
            "Seguro Vida", "Gratificação", "Gratif./Abono Parada", "PLR",
            "PLR Parada", "Flash Virtual", "Prêmio Desempenho", "Aux. Moradia",
            "Ajuda Custo", "Reemb. Viagem", "Vale Transporte", "Aux. Transporte",
            "Fretado", "Anuênio", "Contrib. Patronal", "Contrib. Patronal Educ."
        };
        
        // Cabeçalho da tabela - fonte maior para melhor legibilidade
        org.openpdf.text.Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            cell.setPadding(3);
            cell.setFixedHeight(25f);
            table.addCell(cell);
        }
        
        // Dados das funções - fonte maior para melhor legibilidade
        org.openpdf.text.Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 6);
        for (SalaryFunctionDTO funcao : funcoes) {
            // Campos básicos - menos truncamento devido às colunas mais largas
            addCell(table, truncate(formatFieldValue(funcao.getNomeFuncaoAct()), 25), cellFont);
            addCell(table, truncate(formatFieldValue(funcao.getCodigoCbo()), 15), cellFont);
            addCell(table, truncate(formatFieldValue(funcao.getNomeFuncaoCbo()), 35), cellFont);
            addCell(table, truncate(formatFieldValue(funcao.getSalarioBase()), 18), cellFont);
            
            // Horas Extras - menos truncamento
            addCell(table, truncate(formatBeneficioCompacto(funcao.getHoraExtra1()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getHoraExtra2()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getHoraExtra3()), 18), cellFont);
            
            // Adicionais
            addCell(table, truncate(formatBeneficioCompacto(funcao.getAdicionalNoturno()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getAdicionalInsalubridade()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getAdicionalPericulosidade()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getAdicionalSobreaviso()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getAdicionalProntidao()), 18), cellFont);
            
            // Alimentação e Refeição
            addCell(table, truncate(formatBeneficioCompacto(funcao.getCafeDaManha()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getAlmoco()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getLanche()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getLancheParada()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getValeAlimentacao()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getValeAlimentacaoParada()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getValeRefeicao()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getValeRefeicaoParada()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getCestaBasica()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getCestaNatalina()), 18), cellFont);
            
            // Benefícios de Saúde
            addCell(table, truncate(formatBeneficioCompacto(funcao.getPlanoOdontologico()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getPlanoSaude()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getSeguroVida()), 18), cellFont);
            
            // Premiações
            addCell(table, truncate(formatBeneficioCompacto(funcao.getGratificacao()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getGratificacaoAbonoParada()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getPlr()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getPlrParada()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getFlashVirtual()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getPremioDesempenho()), 18), cellFont);
            
            // Outros Benefícios
            addCell(table, truncate(formatBeneficioCompacto(funcao.getAuxilioMoradia()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getAjudaDeCusto()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getReembolsoDespesaViagem()), 18), cellFont);
            
            // Transporte
            addCell(table, truncate(formatBeneficioCompacto(funcao.getValeTransporte()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getAuxilioTransporte()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getFretado()), 18), cellFont);
            
            // Anuênio e Contribuições
            addCell(table, truncate(formatBeneficioCompacto(funcao.getAnuenio()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getContribuicaoPatronal()), 18), cellFont);
            addCell(table, truncate(formatBeneficioCompacto(funcao.getContribuicaoPatronalEducativa()), 18), cellFont);
        }
        
        return table;
    }
    
    private void addCell(PdfPTable table, String text, org.openpdf.text.Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(3);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setNoWrap(false); // Permitir quebra de linha
        cell.setMinimumHeight(20f); // Altura mínima aumentada para melhor visualização
        table.addCell(cell);
    }
    
    private void addFooter(PdfWriter writer) {
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                Phrase footer = new Phrase("Página " + writer.getPageNumber(), 
                    FontFactory.getFont(FontFactory.HELVETICA, 8));
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, footer, 
                    (document.right() - document.left()) / 2 + document.leftMargin(), 
                    document.bottom() - 10, 0);
            }
        });
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null) return "-";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
    
    public byte[] exportToExcel(SalaryDTO salaryDTO) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Salários e Benefícios");
            
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
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setWrapText(true);
            
            CellStyle contentCenterStyle = workbook.createCellStyle();
            contentCenterStyle.setAlignment(HorizontalAlignment.CENTER);
            contentCenterStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            contentCenterStyle.setWrapText(true);
            contentCenterStyle.setBorderBottom(BorderStyle.THIN);
            contentCenterStyle.setBorderTop(BorderStyle.THIN);
            contentCenterStyle.setBorderLeft(BorderStyle.THIN);
            contentCenterStyle.setBorderRight(BorderStyle.THIN);
            
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy"));
            
            int rowNum = 0;
            
            // Informações gerais
            if (salaryDTO.getPisoSalarial() != null || 
                salaryDTO.getDataBase() != null || salaryDTO.getPorcentagemReajuste() != null ||
                salaryDTO.getDissidio() != null) {
                Row infoHeaderRow = sheet.createRow(rowNum++);
                infoHeaderRow.createCell(0).setCellValue("Informações Gerais");
                infoHeaderRow.getCell(0).setCellStyle(headerStyle);
                
                addInfoRowToExcel(sheet, rowNum++, "Piso Salarial", formatFieldValue(salaryDTO.getPisoSalarial()));
                addInfoRowToExcel(sheet, rowNum++, "Data Base", 
                    salaryDTO.getDataBase() != null ? salaryDTO.getDataBase().format(DATE_FORMATTER) : "-");
                addInfoRowToExcel(sheet, rowNum++, "Porcentagem Reajuste", 
                    formatFieldValue(salaryDTO.getPorcentagemReajuste()));
                addInfoRowToExcel(sheet, rowNum++, "Dissídio", formatFieldValue(salaryDTO.getDissidio()));
                
                rowNum++; // Linha em branco
            }
            
            // Tabela de funções
            if (salaryDTO.getFuncoes() != null && !salaryDTO.getFuncoes().isEmpty()) {
                Row headerRow = sheet.createRow(rowNum++);
                String[] headers = {"Função ACT", "Código CBO", "Função CBO", "Salário Base", 
                                   "Hora Extra 1", "Hora Extra 2", "Hora Extra 3", "Adicional Noturno",
                                   "Adicional Insalubridade", "Adicional Periculosidade", "Adicional Sobreaviso",
                                   "Adicional Prontidão", "Café da Manhã", "Almoço", "Lanche", "Lanche Parada",
                                   "Vale Alimentação", "Vale Alimentação Parada", "Vale Refeição", 
                                   "Vale Refeição Parada", "Cesta Básica", "Cesta Natalina", "Plano Odontológico",
                                   "Plano de Saúde", "Seguro de Vida", "Gratificação", "Gratificação/Abono Parada",
                                   "PLR", "PLR Parada", "Flash Virtual", "Prêmio Desempenho", "Auxílio Moradia",
                                   "Ajuda de Custo", "Reembolso Despesa Viagem", "Vale Transporte", 
                                   "Auxílio Transporte", "Fretado", "Anuênio", "Contribuição Patronal",
                                   "Contribuição Patronal Educativa"};
                
                // largura padrão maior para facilitar leitura (em caracteres)
                sheet.setDefaultColumnWidth(35);
                
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }
                headerRow.setHeightInPoints(24);
                
                for (SalaryFunctionDTO funcao : salaryDTO.getFuncoes()) {
                    Row row = sheet.createRow(rowNum++);
                    int colNum = 0;
                    
                    Cell c0 = row.createCell(colNum++); c0.setCellValue(formatFieldValue(funcao.getNomeFuncaoAct())); c0.setCellStyle(contentCenterStyle);
                    Cell c1 = row.createCell(colNum++); c1.setCellValue(formatFieldValue(funcao.getCodigoCbo())); c1.setCellStyle(contentCenterStyle);
                    Cell c2 = row.createCell(colNum++); c2.setCellValue(formatFieldValue(funcao.getNomeFuncaoCbo())); c2.setCellStyle(contentCenterStyle);
                    Cell c3 = row.createCell(colNum++); c3.setCellValue(formatFieldValue(funcao.getSalarioBase())); c3.setCellStyle(contentCenterStyle);
                    Cell c4 = row.createCell(colNum++); c4.setCellValue(formatBeneficio(funcao.getHoraExtra1())); c4.setCellStyle(contentCenterStyle);
                    Cell c5 = row.createCell(colNum++); c5.setCellValue(formatBeneficio(funcao.getHoraExtra2())); c5.setCellStyle(contentCenterStyle);
                    Cell c6 = row.createCell(colNum++); c6.setCellValue(formatBeneficio(funcao.getHoraExtra3())); c6.setCellStyle(contentCenterStyle);
                    Cell c7 = row.createCell(colNum++); c7.setCellValue(formatBeneficio(funcao.getAdicionalNoturno())); c7.setCellStyle(contentCenterStyle);
                    Cell c8 = row.createCell(colNum++); c8.setCellValue(formatBeneficio(funcao.getAdicionalInsalubridade())); c8.setCellStyle(contentCenterStyle);
                    Cell c9 = row.createCell(colNum++); c9.setCellValue(formatBeneficio(funcao.getAdicionalPericulosidade())); c9.setCellStyle(contentCenterStyle);
                    Cell c10 = row.createCell(colNum++); c10.setCellValue(formatBeneficio(funcao.getAdicionalSobreaviso())); c10.setCellStyle(contentCenterStyle);
                    Cell c11 = row.createCell(colNum++); c11.setCellValue(formatBeneficio(funcao.getAdicionalProntidao())); c11.setCellStyle(contentCenterStyle);
                    Cell c12 = row.createCell(colNum++); c12.setCellValue(formatBeneficio(funcao.getCafeDaManha())); c12.setCellStyle(contentCenterStyle);
                    Cell c13 = row.createCell(colNum++); c13.setCellValue(formatBeneficio(funcao.getAlmoco())); c13.setCellStyle(contentCenterStyle);
                    Cell c14 = row.createCell(colNum++); c14.setCellValue(formatBeneficio(funcao.getLanche())); c14.setCellStyle(contentCenterStyle);
                    Cell c15 = row.createCell(colNum++); c15.setCellValue(formatBeneficio(funcao.getLancheParada())); c15.setCellStyle(contentCenterStyle);
                    Cell c16 = row.createCell(colNum++); c16.setCellValue(formatBeneficio(funcao.getValeAlimentacao())); c16.setCellStyle(contentCenterStyle);
                    Cell c17 = row.createCell(colNum++); c17.setCellValue(formatBeneficio(funcao.getValeAlimentacaoParada())); c17.setCellStyle(contentCenterStyle);
                    Cell c18 = row.createCell(colNum++); c18.setCellValue(formatBeneficio(funcao.getValeRefeicao())); c18.setCellStyle(contentCenterStyle);
                    Cell c19 = row.createCell(colNum++); c19.setCellValue(formatBeneficio(funcao.getValeRefeicaoParada())); c19.setCellStyle(contentCenterStyle);
                    Cell c20 = row.createCell(colNum++); c20.setCellValue(formatBeneficio(funcao.getCestaBasica())); c20.setCellStyle(contentCenterStyle);
                    Cell c21 = row.createCell(colNum++); c21.setCellValue(formatBeneficio(funcao.getCestaNatalina())); c21.setCellStyle(contentCenterStyle);
                    Cell c22 = row.createCell(colNum++); c22.setCellValue(formatBeneficio(funcao.getPlanoOdontologico())); c22.setCellStyle(contentCenterStyle);
                    Cell c23 = row.createCell(colNum++); c23.setCellValue(formatBeneficio(funcao.getPlanoSaude())); c23.setCellStyle(contentCenterStyle);
                    Cell c24 = row.createCell(colNum++); c24.setCellValue(formatBeneficio(funcao.getSeguroVida())); c24.setCellStyle(contentCenterStyle);
                    Cell c25 = row.createCell(colNum++); c25.setCellValue(formatBeneficio(funcao.getGratificacao())); c25.setCellStyle(contentCenterStyle);
                    Cell c26 = row.createCell(colNum++); c26.setCellValue(formatBeneficio(funcao.getGratificacaoAbonoParada())); c26.setCellStyle(contentCenterStyle);
                    Cell c27 = row.createCell(colNum++); c27.setCellValue(formatBeneficio(funcao.getPlr())); c27.setCellStyle(contentCenterStyle);
                    Cell c28 = row.createCell(colNum++); c28.setCellValue(formatBeneficio(funcao.getPlrParada())); c28.setCellStyle(contentCenterStyle);
                    Cell c29 = row.createCell(colNum++); c29.setCellValue(formatBeneficio(funcao.getFlashVirtual())); c29.setCellStyle(contentCenterStyle);
                    Cell c30 = row.createCell(colNum++); c30.setCellValue(formatBeneficio(funcao.getPremioDesempenho())); c30.setCellStyle(contentCenterStyle);
                    Cell c31 = row.createCell(colNum++); c31.setCellValue(formatBeneficio(funcao.getAuxilioMoradia())); c31.setCellStyle(contentCenterStyle);
                    Cell c32 = row.createCell(colNum++); c32.setCellValue(formatBeneficio(funcao.getAjudaDeCusto())); c32.setCellStyle(contentCenterStyle);
                    Cell c33 = row.createCell(colNum++); c33.setCellValue(formatBeneficio(funcao.getReembolsoDespesaViagem())); c33.setCellStyle(contentCenterStyle);
                    Cell c34 = row.createCell(colNum++); c34.setCellValue(formatBeneficio(funcao.getValeTransporte())); c34.setCellStyle(contentCenterStyle);
                    Cell c35 = row.createCell(colNum++); c35.setCellValue(formatBeneficio(funcao.getAuxilioTransporte())); c35.setCellStyle(contentCenterStyle);
                    Cell c36 = row.createCell(colNum++); c36.setCellValue(formatBeneficio(funcao.getFretado())); c36.setCellStyle(contentCenterStyle);
                    Cell c37 = row.createCell(colNum++); c37.setCellValue(formatBeneficio(funcao.getAnuenio())); c37.setCellStyle(contentCenterStyle);
                    Cell c38 = row.createCell(colNum++); c38.setCellValue(formatBeneficio(funcao.getContribuicaoPatronal())); c38.setCellStyle(contentCenterStyle);
                    Cell c39 = row.createCell(colNum++); c39.setCellValue(formatBeneficio(funcao.getContribuicaoPatronalEducativa())); c39.setCellStyle(contentCenterStyle);
                }
                
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                    int currentWidth = sheet.getColumnWidth(i);
                    int minWidth = (i <= 3 ? 28 : 45) * 256; // primeiras 4 colunas: 28 chars; demais: 45 chars
                    if (currentWidth < minWidth) {
                        sheet.setColumnWidth(i, minWidth);
                    }
                }
            }
            
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }
    
    private void addInfoRowToExcel(Sheet sheet, int rowNum, String label, String value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label + ":");
        row.createCell(1).setCellValue(value);
    }
    
    public byte[] exportToCsv(SalaryDTO salaryDTO) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (CSVWriter writer = new CSVWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            
            // Informações gerais
            writer.writeNext(new String[]{"Informações Gerais"});
            writer.writeNext(new String[]{"Piso Salarial", formatFieldValue(salaryDTO.getPisoSalarial())});
            writer.writeNext(new String[]{"Data Base",
                salaryDTO.getDataBase() != null ? salaryDTO.getDataBase().format(DATE_FORMATTER) : "-"});
            writer.writeNext(new String[]{"Porcentagem Reajuste", formatFieldValue(salaryDTO.getPorcentagemReajuste())});
            writer.writeNext(new String[]{"Dissídio", formatFieldValue(salaryDTO.getDissidio())});
            writer.writeNext(new String[]{}); // Linha em branco
            
            // Tabela de funções
            if (salaryDTO.getFuncoes() != null && !salaryDTO.getFuncoes().isEmpty()) {
                String[] headers = {"Função ACT", "Código CBO", "Função CBO", "Salário Base", 
                                   "Hora Extra 1", "Hora Extra 2", "Hora Extra 3", "Adicional Noturno",
                                   "Adicional Insalubridade", "Adicional Periculosidade", "Adicional Sobreaviso",
                                   "Adicional Prontidão", "Café da Manhã", "Almoço", "Lanche", "Lanche Parada",
                                   "Vale Alimentação", "Vale Alimentação Parada", "Vale Refeição", 
                                   "Vale Refeição Parada", "Cesta Básica", "Cesta Natalina", "Plano Odontológico",
                                   "Plano de Saúde", "Seguro de Vida", "Gratificação", "Gratificação/Abono Parada",
                                   "PLR", "PLR Parada", "Flash Virtual", "Prêmio Desempenho", "Auxílio Moradia",
                                   "Ajuda de Custo", "Reembolso Despesa Viagem", "Vale Transporte", 
                                   "Auxílio Transporte", "Fretado", "Anuênio", "Contribuição Patronal",
                                   "Contribuição Patronal Educativa"};
                writer.writeNext(headers);
                
                for (SalaryFunctionDTO funcao : salaryDTO.getFuncoes()) {
                    String[] line = {
                        formatFieldValue(funcao.getNomeFuncaoAct()),
                        formatFieldValue(funcao.getCodigoCbo()),
                        formatFieldValue(funcao.getNomeFuncaoCbo()),
                        formatFieldValue(funcao.getSalarioBase()),
                        formatBeneficio(funcao.getHoraExtra1()),
                        formatBeneficio(funcao.getHoraExtra2()),
                        formatBeneficio(funcao.getHoraExtra3()),
                        formatBeneficio(funcao.getAdicionalNoturno()),
                        formatBeneficio(funcao.getAdicionalInsalubridade()),
                        formatBeneficio(funcao.getAdicionalPericulosidade()),
                        formatBeneficio(funcao.getAdicionalSobreaviso()),
                        formatBeneficio(funcao.getAdicionalProntidao()),
                        formatBeneficio(funcao.getCafeDaManha()),
                        formatBeneficio(funcao.getAlmoco()),
                        formatBeneficio(funcao.getLanche()),
                        formatBeneficio(funcao.getLancheParada()),
                        formatBeneficio(funcao.getValeAlimentacao()),
                        formatBeneficio(funcao.getValeAlimentacaoParada()),
                        formatBeneficio(funcao.getValeRefeicao()),
                        formatBeneficio(funcao.getValeRefeicaoParada()),
                        formatBeneficio(funcao.getCestaBasica()),
                        formatBeneficio(funcao.getCestaNatalina()),
                        formatBeneficio(funcao.getPlanoOdontologico()),
                        formatBeneficio(funcao.getPlanoSaude()),
                        formatBeneficio(funcao.getSeguroVida()),
                        formatBeneficio(funcao.getGratificacao()),
                        formatBeneficio(funcao.getGratificacaoAbonoParada()),
                        formatBeneficio(funcao.getPlr()),
                        formatBeneficio(funcao.getPlrParada()),
                        formatBeneficio(funcao.getFlashVirtual()),
                        formatBeneficio(funcao.getPremioDesempenho()),
                        formatBeneficio(funcao.getAuxilioMoradia()),
                        formatBeneficio(funcao.getAjudaDeCusto()),
                        formatBeneficio(funcao.getReembolsoDespesaViagem()),
                        formatBeneficio(funcao.getValeTransporte()),
                        formatBeneficio(funcao.getAuxilioTransporte()),
                        formatBeneficio(funcao.getFretado()),
                        formatBeneficio(funcao.getAnuenio()),
                        formatBeneficio(funcao.getContribuicaoPatronal()),
                        formatBeneficio(funcao.getContribuicaoPatronalEducativa())
                    };
                    writer.writeNext(line);
                }
            }
            
            writer.flush();
        }
        
        return outputStream.toByteArray();
    }
}

