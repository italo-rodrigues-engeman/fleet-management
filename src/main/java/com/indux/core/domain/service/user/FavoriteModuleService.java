package com.indux.core.domain.service.user;

import com.indux.core.application.dto.module.ModuleResponseDTO;

import java.util.List;
import java.util.UUID;

public interface FavoriteModuleService {
    /**
     * Adiciona um módulo à lista de favoritos do usuário.
     * @param userId   identificador do usuário que está adicionando o favorito
     * @param moduleId identificador do módulo a ser adicionado aos favoritos
     */
    void addFavorite(UUID userId, UUID moduleId);

    /**
     * Remove um módulo da lista de favoritos do usuário.
     * @param userId   identificador do usuário que está removendo o favorito
     * @param moduleId identificador do módulo a ser removido dos favoritos
     */
    void removeFavorite(UUID userId, UUID moduleId);

    /**
     * Lista todos os módulos favoritos do usuário.
     * @param userId identificador do usuário que está consultando os favoritos
     * @return lista de módulos favoritos do usuário
     */
    List<ModuleResponseDTO> listFavorites(UUID userId);
    
    /**
     * Remove todos os usuários favoritos de um módulo específico.
     * @param moduleId identificador do módulo
     */
    void removeAllUsersFromModule(UUID moduleId);
}