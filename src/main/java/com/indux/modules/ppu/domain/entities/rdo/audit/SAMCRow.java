package com.indux.modules.ppu.domain.entities.rdo.audit;

import lombok.Getter;

import java.text.Normalizer;
import java.util.*;

@Getter
public class SAMCRow {
    private final Map<String, String> dataByHeader;
    private final List<String> dataByIndex;

    private final String contrato;
    private final String local;
    private final String dataInicio;
    private final String data;
    private final String numeroDetalhamento;
    private final String descricaoServico;
    private final String quantidadeExecutada;
    private final String status;
    private final String auditableValue;

    public SAMCRow(Map<String, String> dataByHeader, List<String> dataByIndex) {
        this.dataByHeader = canonicalizeHeaderMap(dataByHeader);
        this.dataByIndex = dataByIndex != null ? dataByIndex : List.of();

        this.contrato = get("contrato");
        this.local = get("local");
        this.dataInicio = firstNonBlank(
                get("dataInicio"),
                get("periodoInicio"),
                get("periodo inicio"),
                get("período início"));
        this.data = get("data");
        this.numeroDetalhamento = get("numerodetalhamento");
        this.descricaoServico = get("descricaoservico");
        this.quantidadeExecutada = get("quantidadeexecutada");
        this.status = get("status");
        this.auditableValue = get("auditablevalue");
    }

    private static Map<String, String> canonicalizeHeaderMap(Map<String, String> in) {
        if (in == null || in.isEmpty()) return new HashMap<>();
        Map<String, String> out = new HashMap<>(Math.max(16, in.size() * 2));
        for (Map.Entry<String, String> e : in.entrySet()) {
            if (e.getKey() == null) continue;
            String key = canonKey(e.getKey());
            if (key.isBlank()) continue;
            out.put(key, e.getValue());
        }
        return out;
    }

    private static String canonKey(String s) {
        if (s == null) return "";
        String normalized = Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    private String get(String key) {
        if (key == null) return null;
        return dataByHeader.get(canonKey(key));
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return null;
    }

    public String column(String columnName) {
        if (columnName == null) return null;
        return dataByHeader.get(canonKey(columnName));
    }

    public String column(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= dataByIndex.size()) return null;
        return dataByIndex.get(columnIndex);
    }

    public String contrato() { return contrato; }
    public String local() { return local; }
    public String dataInicio() { return dataInicio; }
    public String data() { return data; }
    public String numeroDetalhamento() { return numeroDetalhamento; }
    public String descricaoServico() { return descricaoServico; }
    public String quantidadeExecutada() { return quantidadeExecutada; }
    public String status() { return status; }
    public String auditableValue() { return auditableValue; }

    public static SAMCRowBuilder builder() { return new SAMCRowBuilder(); }

    public static class SAMCRowBuilder {
        private String contrato;
        private String local;
        private String dataInicio;
        private String data;
        private String numeroDetalhamento;
        private String descricaoServico;
        private String quantidadeExecutada;
        private String status;
        private String auditableValue;

        public SAMCRowBuilder contrato(String v) { this.contrato = v; return this; }
        public SAMCRowBuilder local(String v) { this.local = v; return this; }
        public SAMCRowBuilder dataInicio(String v) { this.dataInicio = v; return this; }
        public SAMCRowBuilder data(String v) { this.data = v; return this; }
        public SAMCRowBuilder numeroDetalhamento(String v) { this.numeroDetalhamento = v; return this; }
        public SAMCRowBuilder descricaoServico(String v) { this.descricaoServico = v; return this; }
        public SAMCRowBuilder quantidadeExecutada(String v) { this.quantidadeExecutada = v; return this; }
        public SAMCRowBuilder status(String v) { this.status = v; return this; }
        public SAMCRowBuilder auditableValue(String v) { this.auditableValue = v; return this; }

        public SAMCRow build() {
            Map<String, String> map = new HashMap<>();
            if (contrato != null) map.put("contrato", contrato);
            if (local != null) map.put("local", local);
            if (dataInicio != null) map.put("datainicio", dataInicio);
            if (data != null) map.put("data", data);
            if (numeroDetalhamento != null) map.put("numerodetalhamento", numeroDetalhamento);
            if (descricaoServico != null) map.put("descricaoservico", descricaoServico);
            if (quantidadeExecutada != null) map.put("quantidadeexecutada", quantidadeExecutada);
            if (status != null) map.put("status", status);
            if (auditableValue != null) map.put("auditablevalue", auditableValue);
            return new SAMCRow(map, new ArrayList<>());
        }
    }
}
