package com.indux.core.domain.service.auth;

import com.indux.core.application.dto.generic.GenericMessage;
import jakarta.mail.MessagingException;

public interface ResetPasswordService {
    /**
     * Solicita a redefinição de senha para um usuário.
     * Envia um e-mail com um token de redefinição para o usuário.
     * @param cpf CPF do usuário que solicita a redefinição
     * @throws MessagingException se houver erro ao enviar o e-mail
     */
    void requestReset(String cpf) throws MessagingException;

    /**
     * Valida um token de redefinição de senha.
     * @param token código de redefinição de senha
     * @return mensagem indicando se o token é válido
     */
    GenericMessage validateToken(int token);

    /**
     * Redefine a senha do usuário usando um token válido.
     * @param token       código de redefinição de senha
     * @param newPassword nova senha do usuário
     * @return mensagem de confirmação da redefinição
     */
    GenericMessage resetPassword(int token, String newPassword);
}
