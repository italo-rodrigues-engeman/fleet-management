package com.indux.modules.modulo_mega.service;

import com.indux.modules.modulo_mega.application.dto.ItemHistoryDetailedDTO;
import com.indux.modules.modulo_mega.application.dto.AbcCurveGroupDTO;
import com.indux.modules.modulo_mega.application.dto.AbcItemDetailDTO;

// --- IMPORTS DO OPENPDF 3.0.0 ---
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import java.awt.Color;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Comparator;
import java.math.BigDecimal;
import java.math.RoundingMode;


@Service
public class PdfGeneratorService {

    // FORMATADORES
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    // DEFINIÇÃO DE FONTES MODERNAS
    private static final Font FONT_TITLE = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(50, 50, 50)); 
    private static final Font FONT_SUBTITLE = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.GRAY); 
    private static final Font FONT_SECTION = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(30, 30, 30)); 
    
    // ESTILO DOS CAMPOS (MODERNO)
    private static final Font FONT_FIELD_LABEL = new Font(Font.HELVETICA, 7, Font.BOLD, new Color(120, 120, 120)); 
    private static final Font FONT_FIELD_VALUE = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(0, 0, 0)); 
    
    // Fontes da Tabela ABC (Diminuídas levemente para caber mais colunas)
    private static final Font FONT_TABLE_HEADER = new Font(Font.HELVETICA, 7, Font.BOLD, Color.WHITE);
    private static final Font FONT_TABLE_BODY = new Font(Font.HELVETICA, 7, Font.NORMAL, Color.BLACK);
    
    private static final Font FONT_FOOTER = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.GRAY);

    // CORES
    private static final Color HEADER_BAR_COLOR = new Color(244, 164, 96); // Laranja
    private static final Color LINE_COLOR = new Color(200, 200, 200); // Cor da linha do campo
    private static final Color COLOR_A = new Color(102, 204, 102); 
    private static final Color COLOR_B = new Color(255, 204, 102); 
    private static final Color COLOR_C = new Color(255, 102, 102); 

    // =========================================================================
    // GERAÇÃO DO PDF DA CURVA ABC
    // =========================================================================

    public byte[] generateAbcCurvePdf(List<AbcCurveGroupDTO> abcGroups) {
        Document document = new Document(PageSize.A4.rotate()); // MUDANÇA: Usei ROTATE (Paisagem) para caber melhor as 8 colunas
        // Se preferir Retrato, mude para PageSize.A4, mas as colunas ficarão apertadas.
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            addPageHeader(document, "RELATÓRIO DE CURVA ABC (ANÁLISE DE PARETO)", null);

            abcGroups.sort(Comparator.comparing(AbcCurveGroupDTO::getCurveClass));

            for (AbcCurveGroupDTO group : abcGroups) {
                addAbcGroupSection(document, group);
            }

            addDocumentFooter(document);
            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF da Curva ABC: " + e.getMessage(), e);
        }
    }

    private void addPageHeader(Document document, String title, String subtitle) throws Exception {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.addElement(new Paragraph("ENGEMAN - " + title, FONT_TITLE));
        
        if (subtitle != null) {
            cell.addElement(new Paragraph(subtitle, FONT_SUBTITLE));
        }
        
        cell.addElement(new Paragraph("Gerado em: " + LocalDateTime.now().format(DATETIME_FORMATTER), FONT_FOOTER));
        cell.setPaddingBottom(10f);
        
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell lineCell = new PdfPCell(new Phrase(" "));
        lineCell.setBackgroundColor(HEADER_BAR_COLOR);
        lineCell.setBorder(Rectangle.NO_BORDER);
        lineCell.setFixedHeight(3f);
        line.addCell(lineCell);

        headerTable.addCell(cell);
        
        document.add(headerTable);
        document.add(line);
        document.add(new Paragraph(" ")); 
    }

    private void addAbcGroupSection(Document document, AbcCurveGroupDTO group) throws Exception {
        Color color;
        switch (group.getCurveClass()) {
            case "A": color = COLOR_A; break;
            case "B": color = COLOR_B; break;
            case "C": color = COLOR_C; break;
            default: color = Color.BLACK;
        }

        PdfPTable titleTable = new PdfPTable(1);
        titleTable.setWidthPercentage(100);
        titleTable.setSpacingBefore(15f);
        
        PdfPCell titleCell = new PdfPCell(new Phrase("CURVA " + group.getCurveClass(), new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE)));
        titleCell.setBackgroundColor(color);
        titleCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        titleCell.setPadding(5f);
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleTable.addCell(titleCell);

        document.add(titleTable);

        addAbcTotals(document, group);

        // AQUI ESTÁ A CORREÇÃO DA TABELA
        addAbcItemsTable(document, group.getItems());
    }

   
   private void addAbcTotals(Document document, AbcCurveGroupDTO group) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingBefore(5f);
        table.setSpacingAfter(10f);

        addModernField(table, "VALOR TOTAL DO GRUPO", formatCurrency(group.getTotalGroupValue()));
        addModernField(table, "VOLUME TOTAL (QTD)", group.getTotalGroupVolume().longValue());

        document.add(table);
    }


    // --- CORREÇÃO AQUI: ADICIONADOS STATUS E PEDIDOS ---
    private void addAbcItemsTable(Document document, List<AbcItemDetailDTO> items) throws Exception {
        // Agora são 9 Colunas
        
        float[] columnWidths = {0.10f, 0.55f, 0.10f, 0.50f, 0.10f,0.10f,0.15f, 0.2f, 0.19f, 0.19f};
        PdfPTable table = new PdfPTable(columnWidths);
        table.setWidthPercentage(100);
        table.setSpacingAfter(20f);

        // Cabeçalho Clean
        String[] headers = {"CÓD-ITEM", "ITEM", "CÓD-GRUPO","GRUPO", "STATUS","TIPO", "PEDIDOS", "QTD", "PREÇO MÉDIO", "TOTAL"};
        for (String h : headers) {
            PdfPCell c = new PdfPCell(new Phrase(h, FONT_TABLE_HEADER));
            c.setBackgroundColor(new Color(80, 80, 80));
            c.setPadding(2f);
            c.setBorder(Rectangle.NO_BORDER);
            table.addCell(c);
        }

        if (items != null && !items.isEmpty()) {
            boolean gray = false;
            for (AbcItemDetailDTO item : items) {
                Color bg = gray ? new Color(245, 245, 245) : Color.WHITE;
                
                addCleanCell(table, item.getIdItem(), Element.ALIGN_LEFT, bg);
                addCleanCell(table, item.getItemName(), Element.ALIGN_LEFT, bg);
				addCleanCell(table, item.getGroupCode(), Element.ALIGN_LEFT, bg);
                addCleanCell(table, item.getGroupName(), Element.ALIGN_LEFT, bg);
                addCleanCell(table, item.getStatus(), Element.ALIGN_CENTER, bg);
                addCleanCell(table, item.getItemType(), Element.ALIGN_CENTER, bg);
                addCleanCell(table, item.getTotalOrders(), Element.ALIGN_CENTER, bg); 
                addCleanCell(table, item.getQtdItensTotal(), Element.ALIGN_RIGHT, bg);
                addCleanCell(table, formatCurrency(item.getAveragePrice()), Element.ALIGN_RIGHT, bg);
                addCleanCell(table, formatCurrency(item.getTotalValue()), Element.ALIGN_RIGHT, bg);
                
                gray = !gray; 
            }
        } else {
            PdfPCell c = new PdfPCell(new Phrase("Nenhum item nesta curva.", FONT_TABLE_BODY));
            c.setColspan(9); 
            c.setHorizontalAlignment(Element.ALIGN_CENTER);
            c.setPadding(10f);
            table.addCell(c);
        }

        document.add(table);
    }
    
    private void addCleanCell(PdfPTable table, Object val, int align, Color bg) {
        PdfPCell c = new PdfPCell(new Phrase(val != null ? val.toString() : "-", FONT_TABLE_BODY));
        c.setHorizontalAlignment(align);
        c.setBackgroundColor(bg);
        c.setPadding(3f);
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(new Color(230, 230, 230)); 
        table.addCell(c);
    }

    // =========================================================================
    // GERAÇÃO DO HISTÓRICO DE COMPRAS (ESTILO FORMULÁRIO MODERNO)
    // =========================================================================

    public byte[] generateItemHistoryPdf(List<ItemHistoryDetailedDTO> historyData, Integer idItem) {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            addPageHeader(document, "HISTÓRICO DETALHADO", "Código do Item: " + idItem + " | Registros: " + historyData.size());

            for (int i = 0; i < historyData.size(); i++) {
                ItemHistoryDetailedDTO item = historyData.get(i);

                if (i > 0) {
                    Paragraph spacer = new Paragraph(" ");
                    spacer.setSpacingAfter(20f);
                    document.add(spacer);
                    
                    PdfPTable div = new PdfPTable(1);
                    div.setWidthPercentage(100);
                    PdfPCell dc = new PdfPCell(new Phrase(" "));
                    dc.setBackgroundColor(HEADER_BAR_COLOR);
                    dc.setFixedHeight(2f);
                    dc.setBorder(Rectangle.NO_BORDER);
                    div.addCell(dc);
                    document.add(div);
                    document.add(new Paragraph(" "));
                }

                addItemBasicInfo(document, item);
                addSupplierInfo(document, item);
                addLocationInfo(document, item);
                addFinancialInfo(document, item);
                addDatesInfo(document, item);
                addPeopleInfo(document, item);
                addOrderInfo(document, item);
            }

            addDocumentFooter(document);
            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF de Histórico: " + e.getMessage(), e);
        }
    }

    // --- SEÇÕES DO HISTÓRICO ---

    private void addItemBasicInfo(Document document, ItemHistoryDetailedDTO item) throws Exception {
        addSectionTitle(document, "DADOS BÁSICOS");
        PdfPTable table = createFormTable(2); 
        
        addModernField(table, "CÓDIGO", item.getIdItem());
        addModernField(table, "DESCRIÇÃO", item.getItemName());
        
        addModernField(table, "STATUS PEDIDO", item.getOrderStatus());
        addModernField(table, "STATUS ITEM", item.getItemStatus());
        addModernField(table, "CÓD. GRUPO", item.getGroupCode());
        addModernField(table, "GRUPO", item.getGroupName());
        addModernField(table, "PEDIDO", item.getOrderNumber());
        addModernField(table, "NOTA", item.getNoteNumber());
        addModernField(table, "NÚMERO DA SOLICITACAO", item.getSolicitation());
        addModernField(table, "TIPO", item.getItemType());
        addModernField(table, "AP", item.getApprove());
        addModernField(table, "UNIDADE DE MEDIDA", item.getUnitOfMeasure());
        
		addModernField(table, "", "");
        
        document.add(table);
    }

    private void addSupplierInfo(Document document, ItemHistoryDetailedDTO item) throws Exception {
        addSectionTitle(document, "FORNECEDOR E VALORES");
        PdfPTable table = createFormTable(3); 
        
        addModernField(table, "CÓD. FORNECEDOR", item.getSupplierCode());
         addModernField(table, "CNPJ", item.getCnpj());
        addModernField(table, "NOME FORNECEDOR", item.getSupplierName());
        addModernField(table, "QTD COMPRADA", item.getQtdItensTotal());
        addModernField(table, "PREÇO MÉDIO", formatCurrency(item.getAveragePriceFromSupplier()));
        addModernField(table, "TOTAL DA COMPRA", formatCurrency(item.getTotalItemValue()));
        addModernField(table, "", ""); // Espaço
        
        document.add(table);
    }

    private void addLocationInfo(Document document, ItemHistoryDetailedDTO item) throws Exception {
        addSectionTitle(document, "LOCALIZAÇÃO");
        PdfPTable table = createFormTable(3);
        
        addModernField(table, "REGIONAL", item.getRegionalCode() + " - " + item.getRegionalName());
        addModernField(table, "FILIAL", item.getBranchId() + " - " + item.getBranchName());
        addModernField(table, "DIRETORIA", item.getDirectoryName());
        addModernField(table, "SUPERINTENDÊNCIA", item.getSuperName());
        
        addModernField(table, "", "");
        addModernField(table, "", "");
        
        document.add(table);
    }

    private void addFinancialInfo(Document document, ItemHistoryDetailedDTO item) throws Exception {
        addSectionTitle(document, "DOCUMENTAÇÃO");
        PdfPTable table = createFormTable(2); 
        
        addModernField(table, "CONTRATO", item.getContractName());
        
        
        document.add(table);
    }

    private void addDatesInfo(Document document, ItemHistoryDetailedDTO item) throws Exception {
        addSectionTitle(document, "DATAS DO PROCESSO");
        PdfPTable table = createFormTable(4); 
        
        addModernField(table, "CRIAÇÃO", formatDate(item.getCreationDate()));
        
        addModernField(table, "SOLICITAÇÃO", formatDate(item.getSolDate()));
        addModernField(table, "COMPRA", formatDate(item.getOrderDate()));
        addModernField(table, "ENTREGA", formatDate(item.getDeliveryDate()));
        
        
        addModernField(table, "", ""); 
        addModernField(table, "", "");
        
        document.add(table);
    }

    private void addPeopleInfo(Document document, ItemHistoryDetailedDTO item) throws Exception {
        addSectionTitle(document, "RESPONSÁVEIS E PEDIDO");
        PdfPTable table = createFormTable(3);
        
        addModernField(table, "SOLICITANTE", item.getRequesterName());
        addModernField(table, "USUÁRIO INCLUSÃO", item.getRegisterUser());
        addModernField(table, "USUÁRIO EDIÇÃO", item.getRegisterUserEdition());
        addModernField(table, "PROJETO", item.getProjectName());
        addModernField(table, "CÓDIGO PROJETO", item.getProjectCode());
        addModernField(table, "COMPRADOR", item.getBuyerName());
        
        addModernField(table, "TIPO PEDIDO", item.getOrderType());
        
        addModernField(table, "", "");
        addModernField(table, "", "");
        
        document.add(table);
    }

    private void addOrderInfo(Document document, ItemHistoryDetailedDTO item) throws Exception {
        // Vazio intencionalmente (dados movidos para PeopleInfo)
    }

    // --- MÉTODOS DE ESTILIZAÇÃO VISUAL ---

    private void addSectionTitle(Document document, String title) throws Exception {
        Paragraph p = new Paragraph(title.toUpperCase(), FONT_SECTION);
        p.setSpacingBefore(12f);
        p.setSpacingAfter(2f);
        document.add(p);
        
        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell c = new PdfPCell(new Phrase(" "));
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(Color.LIGHT_GRAY);
        c.setBorderWidth(0.5f);
        c.setFixedHeight(1f);
        line.addCell(c);
        document.add(line);
    }

    private PdfPTable createFormTable(int columns) {
        PdfPTable table = new PdfPTable(columns);
        table.setWidthPercentage(100);
        table.setSpacingBefore(5f);
        table.setSpacingAfter(5f);
        return table;
    }

    private void addModernField(PdfPTable table, String label, Object value) {
        PdfPCell cell = new PdfPCell();
        
        Paragraph pLabel = new Paragraph(label, FONT_FIELD_LABEL);
        pLabel.setSpacingAfter(1f);
        cell.addElement(pLabel);
        
        String valStr = (value != null && !value.toString().trim().isEmpty()) ? value.toString() : "-";
        Paragraph pValue = new Paragraph(valStr, FONT_FIELD_VALUE);
        cell.addElement(pValue);
        
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderColor(LINE_COLOR);
        cell.setBorderWidth(0.5f);
        
        cell.setPaddingTop(2f);
        cell.setPaddingBottom(5f);
        cell.setPaddingLeft(2f);
        cell.setPaddingRight(10f);
        
        table.addCell(cell);
    }

    private void addDocumentFooter(Document document) throws Exception {
        Paragraph footer = new Paragraph("KOGNI - Sistema de Controladoria Inteligente", FONT_FOOTER);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(30f);
        document.add(footer);
    }

    // --- FORMATADORES ---

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "-";
    }

    private String formatCurrency(BigDecimal value) {
        return value != null ? CURRENCY_FORMATTER.format(value) : "-";
    }

    private String formatDecimal(BigDecimal value) {
       return value != null ? value.setScale(2, RoundingMode.HALF_UP).toPlainString() : "-";
    }
}