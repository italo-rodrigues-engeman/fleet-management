package com.indux.modules.ppu.application.services.bm.audit.rm;

import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.parser.PdfTextExtractor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RMPDFReader {
    private static final Pattern P_VALOR_BRUTO = Pattern.compile("(?iu)\\bVALOR\\s*BRUTO\\b\\s*:?\\s*([\\d.]+,\\d{2})");
    private static final Pattern P_RETENCAO    = Pattern.compile("(?iu)\\bRETEN[ÇC][AÃ]O\\s*CONTRATUAL\\b\\s*:?\\s*([\\d.]+,\\d{2})");
    private static final Pattern P_LIQ_PARCIAL = Pattern.compile("(?iu)\\bVALOR\\s*L[ÍI]QUIDO\\s*PARCIAL\\b\\s*:?\\s*([\\d.]+,\\d{2})");
    private static final Pattern P_IMPOSTOS    = Pattern.compile("(?iu)\\bIMPOSTOS\\s*RETIDOS\\b\\s*:?\\s*([\\d.]+,\\d{2})");
    private static final Pattern P_LIQUIDO     = Pattern.compile("(?iu)\\bVALOR\\s*L[ÍI]QUIDO\\b(?!\\s*PARCIAL)\\s*:?\\s*([\\d.]+,\\d{2})");
    private static final Pattern P_CONTRATO    = Pattern.compile("(?iu)\\bCONTRATO\\s*R/3\\s*:?\\s*(\\d+)");


    public record Resumo(
            BigDecimal valorBruto,
            BigDecimal retencaoContratual,
            BigDecimal valorLiquidoParcial,
            BigDecimal impostosRetidos,
            BigDecimal valorLiquido,
            String contract
    ) {}

    public static Resumo parse(MultipartFile pdf) throws Exception {
        String text;
        try(var is = pdf.getInputStream(); var reader = new PdfReader(is)){
            var extractor = new PdfTextExtractor(reader);
            var sb = new StringBuilder();
            for (int p = 1; p <= reader.getNumberOfPages(); p++) sb.append(extractor.getTextFromPage(p)).append('\n');
            text = normalize(sb.toString());
        }
            return new Resumo(
                    find(text, P_VALOR_BRUTO),
                    find(text, P_RETENCAO),
                    find(text, P_LIQ_PARCIAL),
                    find(text, P_IMPOSTOS),
                    find(text, P_LIQUIDO),
                    findContract(text, P_CONTRATO)
            );
    }

    private static BigDecimal find(String text, Pattern p) {
        Matcher m = p.matcher(text);
        if (!m.find()) return null;
        return new BigDecimal(m.group(1).replace(".", "").replace(",", "."));
    }

    private static String findContract(String text, Pattern p) {
        Matcher m = p.matcher(text);
        if (!m.find()) return null;
        return m.group(1);
    }

    private static String normalize(String s) {
        String t = s.replace('\u00A0',' ').replace('\u2007',' ').replace('\u202F',' ');
        t = Normalizer.normalize(t, Normalizer.Form.NFC);
        return t.replaceAll("[ \\t]+", " ");
    }

}