package com.indux.core.domain.service.auth;

import com.indux.core.domain.model.generic.TokenEvent;

import java.util.UUID;

public interface TokenEventValidator {
    /**
     * Cria um novo evento de token para um usuário.
     * @param userId identificador do usuário
     * @param type   tipo do evento, definido no banco e no enum TokenEventType
     * @return evento de token criado
     */
    TokenEvent createEvent(UUID userId, String type);

    /**
     * Valida se um token é válido e não expirado.
     * @param id código do token
     * @return true se o token for válido, false caso contrário
     */
    boolean validateToken(int id);

    /**
     * Finaliza um token, marcando-o como utilizado.
     * @param token código do token
     */
    void finalizeToken(int token);

    /**
     * Obtém o identificador do usuário associado a um token.
     * @param token código do token
     * @return identificador do usuário
     */
    UUID getUserToken(int token);
}
