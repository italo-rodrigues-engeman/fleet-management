package com.indux.modules.ppu.application.dtos.response;

import com.indux.modules.ppu.infra.mio.dto.BoardedEmployee;

import java.util.List;

public record AvailableServiceDTO(
        String id,
        String nome,
        String numero,
        String numeroPPU,
        List<BoardedEmployee> colaboradores
) {
}
