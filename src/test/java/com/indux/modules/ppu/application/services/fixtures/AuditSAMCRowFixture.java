package com.indux.modules.ppu.application.services.fixtures;

import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.audit.SAMCRow;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

public final class AuditSAMCRowFixture {

    private AuditSAMCRowFixture() {
    }

    public static SAMCRow row(String contrato, String local, String data,
            String numeroDetalhamento, String descricaoServico, String quantidadeExecutada) {
        return SAMCRow.builder()
                .contrato(contrato)
                .local(local)
                .data(data)
                .numeroDetalhamento(numeroDetalhamento)
                .descricaoServico(descricaoServico)
                .quantidadeExecutada(quantidadeExecutada)
                .status("Aprovado")
                .auditableValue(descricaoServico)
                .build();
    }

    public static SAMCRow row(String contrato, String local, String data,
            String numeroDetalhamento, String descricaoServico, String quantidadeExecutada, String status) {
        return SAMCRow.builder()
                .contrato(contrato)
                .local(local)
                .data(data)
                .numeroDetalhamento(numeroDetalhamento)
                .descricaoServico(descricaoServico)
                .quantidadeExecutada(quantidadeExecutada)
                .status(status)
                .auditableValue(descricaoServico)
                .build();
    }

    public static SAMCRow headerMismatchRow() {
        return row(
                "4600677525",
                "P-59",
                "26/07/2025",
                "1.1.101",
                "Serviço de Supervisão de Movimentação de Cargas",
                "1");
    }

    public static SAMCRow missingItemRow(String sapCode, String platform, String date) {
        return row(
                sapCode,
                platform,
                date,
                "9.9.9", // número inexistente
                "Serviço Fantasma",
                "5");
    }

    public static List<SAMCRow> validMatchingRows(String sapCode, String platform, String date) {
        return List.of(
                row(sapCode, platform, date, "1.1.101", "Serviço de Supervisão de Movimentação de Cargas", "1"),
                row(sapCode, platform, date, "1.1.116", "Serviço de Sinaleiro", "1"),
                row(sapCode, platform, date, "3.10", "Cabos de Aço AUXILIAR da LANÇA do Guindaste MEP P-58", "2"),
                row(sapCode, platform, date, "3.12", "Cabos de Aço da LANÇA do Guindaste MEP P-58", "2"));
    }

    public static RDOEntity createRDOToHeaderCheck(
            String platform,
            String contractSAP,
            LocalDate date) {
        var contract = new HashMap<String, Object>();
        contract.put("codeSap", contractSAP);
        return RDOEntity
                .builder()
                .platform(platform)
                .contract(contract)
                .date(date)
                .build();
    };
}
