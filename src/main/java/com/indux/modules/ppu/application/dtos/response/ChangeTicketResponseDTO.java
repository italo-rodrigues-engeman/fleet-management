package com.indux.modules.ppu.application.dtos.response;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record ChangeTicketResponseDTO(
        String id,
        String ppuId,
        Instant criadoEm,
        String criadoPor,
        String motivo,
        Long versaoOrigem,
        Long versaoDestino,
        AtorDTO ator,
        OrigemDTO origem,
        List<ItemMudancaDTO> itens,
        List<LogHistoricoDTO> diferencas,
        Map<String, Object> contrato,
        Long regionalId,
        String regionalNome,
        String apelido,
        String status,
        Instant aprovadoEm,
        String aprovadoPor,
        String nomeAprovador,
        String motivoRejeicao
) {
    public record AtorDTO(
            String usuarioId,
            String nome
    ) {}
    
    public record OrigemDTO(
            String servico,
            String ip
    ) {}
    
    public record ItemMudancaDTO(
            String itemId,
            String tipoLinha,
            String plataforma,
            Integer quantidade,
            String campo
    ) {}
    
    public record LogHistoricoDTO(
            String operacao,
            String caminho,
            Object de,
            Object para
    ) {}
}