package com.indux.modules.ppu.application.dtos;

import com.indux.core.application.dto.generic.EmployeeDTO;

/**
 * <b>AuthorContext</b> representa a identidade de quem disparou uma ação de
 * negócio.
 * <p>
 * É um value object de domínio puro — não carrega dependências de
 * infraestrutura (JWT, HTTP, etc.).
 * Deve ser construído na camada de apresentação (controller) e passado às
 * services como parâmetro.
 */
public record AuthorContext(String registration, String name) {

    public static AuthorContext from(EmployeeDTO employee) {
        return new AuthorContext(employee.getMatricula(), employee.getName());
    }
}
