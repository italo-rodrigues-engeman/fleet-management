package com.indux.core.domain.service.module;

import com.indux.core.application.dto.module.BatchModulePermissionsDTO;
import com.indux.core.application.dto.module.ModuleFilter;
import com.indux.core.application.dto.module.ModulePermissionInput;
import com.indux.core.application.dto.module.MultipleUsersModulePermissionsDTO;
import com.indux.core.domain.model.modules.ModulePermission;
import com.indux.core.domain.model.modules.Modulo;

import java.util.List;
import java.util.UUID;

/**
 * Interface que define as operações de gerenciamento de permissões de usuários
 * em módulos.
 * Fornece métodos para adicionar, remover e atualizar permissões de usuários em
 * módulos,
 * além de consultar módulos aos quais um usuário tem acesso.
 * @author Indux Team
 */
public interface ModuleUserService {
    /**
     * Adiciona um usuário a um módulo com permissões específicas.
     * @param moduleID   identificador do módulo
     * @param permission dados de permissão do usuário
     */
    void addUserToModule(String moduleID, ModulePermissionInput permission);

    /**
     * Remove um usuário de um módulo.
     * @param moduleID identificador do módulo
     * @param userID   identificador do usuário
     */
    void removeUsersFromModule(String moduleID, UUID userID);

    /**
     * Atualiza as permissões de múltiplos usuários em um módulo.
     * @param dto dados contendo as atualizações de permissões
     */
    void updateUserPermissions(BatchModulePermissionsDTO dto);

    /**
     * Adiciona permissões em lote para múltiplos usuários em um módulo.
     * @param dto dados contendo as permissões a serem adicionadas
     */
    void addPermissionsBatch(BatchModulePermissionsDTO dto);

    /**
     * Adiciona múltiplos usuários de uma vez ao módulo com suas permissões.
     * @param moduleID identificador do módulo
     * @param dto dados contendo as permissões de múltiplos usuários
     */
    void addMultipleUsersToModule(String moduleID, MultipleUsersModulePermissionsDTO dto);

    /**
     * Lista todos os módulos aos quais um usuário tem acesso.
     * @param userID identificador do usuário
     * @return lista de módulos acessíveis
     */
    List<Modulo> listModulesWithPermission(String userID);

    /**
     * Remove um usuário de todo modulo que ele estiver.
     * @param userId identificador do usuário
     */
    void removeUserFromAllModules(UUID userId);

    /**
     * Adiciona o usuário em todos os modulos.
     */
    void addUserInAllModules(ModulePermissionInput permission);

    /**
     * Filtra pessoas em um módulo com base em critérios específicos.
     * @param moduleID
     * @param filter
     */
    List<String> filterPeopleInModule(UUID moduleID, ModuleFilter filter);
}
