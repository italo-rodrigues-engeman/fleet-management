package com.indux.core.domain.service.auth;

import com.indux.core.application.dto.auth.*;
import com.indux.core.application.dto.generic.GenericMessage;
import jakarta.mail.MessagingException;

public interface AuthService {
    /**
     * Realiza o login de um usuário no sistema.
     * @param login dados de login
     * @return resposta de autenticação contendo token e informações do usuários
     */
    AuthResponse login(LoginRequest login);

    /**
     * Registra um novo usuário no sistema.
     * @param register dados do novo usuário
     * @return resposta de autenticação após registro bem-sucedido
     * @throws MessagingException se houver erro ao enviar email para envio do token
     */
    AuthResponse register(RegisterRequest register) throws MessagingException;

    /**
     * Registra múltiplos usuários em lote a partir de matrículas.
     * @param request dados do lote com matrículas e senha padrão
     * @return resposta contendo usuários criados e pulados
     */
    RegisterBatchResponse registerBatch(RegisterBatchRequest request);

    /**
     * Altera a senha de um usuário.
     * @param request dados para alteração de senha
     * @return mensagem genérica indicando sucesso ou falha
     */
    GenericMessage changePassword(ChangePasswordDTO request);
}
