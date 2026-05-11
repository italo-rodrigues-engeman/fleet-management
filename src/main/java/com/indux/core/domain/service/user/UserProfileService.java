package com.indux.core.domain.service.user;

import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.application.dto.user.UserProfileDTO;
import com.indux.core.domain.model.user.UserProfile;

import java.util.UUID;

public interface UserProfileService {
    /**
     * Obtém os dados do perfil de um usuário.
     * @param id identificador do usuário
     * @return dados do perfil do usuário
     */
    SimpleUser getProfile(UUID id);

    /**
     * Atualiza os dados do perfil de um usuário.
     * @param id  identificador do usuário
     * @param dto dados atualizados do perfil
     * @return perfil atualizado do usuário
     */
    UserProfile updateProfile(UUID id, UserProfileDTO dto);
}
