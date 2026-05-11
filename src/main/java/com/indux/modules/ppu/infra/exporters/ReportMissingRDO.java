package com.indux.modules.ppu.infra.exporters;

import com.indux.modules.ppu.presentation.dtos.MissingRDORequest;
import com.indux.modules.ppu.presentation.dtos.MissingRDOResponse;
import org.jetbrains.annotations.NotNull;
import org.openpdf.text.*;
import org.openpdf.text.Image;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.*;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Component
public class ReportMissingRDO {
    public byte[] generate(List<MissingRDOResponse> responses, MissingRDORequest request, String userName) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Document document = new Document(PageSize.A4, 36, 36, 54, 54);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            addFooter(writer);

            document.open();

            PdfPTable headerTable = createHeader();

            createHeaderInformation(request, userName, document, headerTable);
            document.add(new Paragraph("Total de itens encontrados: " + responses.size() ));
            document.add(new Paragraph(" "));
            PdfPTable table = new PdfPTable(new float[]{2, 2, 2, 6})    ;
            table.setWidthPercentage(100);

            createTable(responses, table);

            document.add(table);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF de RDOs faltantes", e);
        }
    }

    private void createTable(List<MissingRDOResponse> responses, PdfPTable table) {
        String[] headers = {"Data", "Plataforma", "Regional", "Contrato"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            table.addCell(cell);
        }

        for (MissingRDOResponse r : responses) {
            table.addCell(r.getData() != null ? r.getData().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-");
            table.addCell(r.getPlataforma() != null ? r.getPlataforma() : "-");
            table.addCell(r.getRegional() != null ? r.getRegional() : "-");
            table.addCell(truncate(r.getNomeContrato(), 50));
        }
    }

    private static void createHeaderInformation(MissingRDORequest request, String userName, Document document, PdfPTable headerTable) {
        document.add(headerTable);
        document.add(new Paragraph(" "));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        document.add(new Paragraph("Gerado em: " + LocalDate.now().format(formatter)));
        document.add(new Paragraph("Gerado por: " + userName));
        document.add(new Paragraph("Período: " + request.startDate().format(formatter) + " até " + request.endDate().format(formatter)));
        if (request.contract() != null) document.add(new Paragraph("Contrato: " + request.contract()));
        if (request.platforms() != null) document.add(new Paragraph("Plataforma: " + request.platforms()));
    }

    @NotNull
    private static PdfPTable createHeader() throws IOException {
        String base64 = "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";
        byte[] imageBytes = Base64.getDecoder().decode(base64);

        Image logo = Image.getInstance(imageBytes);
        logo.scaleToFit(80, 80);
        logo.setAlignment(Image.ALIGN_LEFT);

        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1, 3});

        PdfPCell logoCell = new PdfPCell(logo, false);
        logoCell.setBorder(Rectangle.NO_BORDER);

        PdfPCell titleCell = new PdfPCell(new Phrase("Relatório: RDO EM FALTA", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        titleCell.setBorder(Rectangle.NO_BORDER);

        headerTable.addCell(logoCell);
        headerTable.addCell(titleCell);
        return headerTable;
    }

    private static void addFooter(PdfWriter writer) {
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                Phrase footer = new Phrase("Página " + writer.getPageNumber(), FontFactory.getFont(FontFactory.HELVETICA, 8));
                ColumnText.showTextAligned(writer.getDirectContent(), Element.ALIGN_CENTER, footer, (document.right() - document.left()) / 2 + document.leftMargin(), document.bottom() - 10, 0);
            }
        });
    }


    private String truncate(String text, int maxLength) {
        if (text == null) return "-";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
