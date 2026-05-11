package com.indux.core.domain.service.module;

import com.indux.core.application.dto.module.CreateModuleDTO;
import com.indux.core.application.dto.module.ModuleResponseDTO;
import com.indux.core.domain.model.modules.Modulo;

import java.util.List;
import java.util.UUID;

public interface ModuleManagementService {
    /**
     * Cria um novo módulo no sistema.
     * Apenas desenvolvedores tem acesso.
     * @param request   dados para criação do módulo
     * @param creatorId identificador do usuário criador
     */
    void createModule(CreateModuleDTO request, UUID creatorId);

    /**
     * Remove permanentemente um módulo do sistema.
     * Apensa desenvolvedores tem acesso.
     * @param moduleID identificador do módulo
     */
    void deleteModule(String moduleID);

    /**
     * Atualiza o nome de um módulo existente.
     * @param moduleID identificador do módulo
     * @param newName  novo nome do módulo
     */
    void updateModuleName(String moduleID, String newName);

    /**
     * Lista todos os módulos ativos do sistema.
     * @return lista de módulos
     */
    List<Modulo> listAllModules();

    /**
     * Lista todos os módulos pelo setor do sistema.
     * @return lista de módulos
     */
    List<ModuleResponseDTO> listBySetor(UUID user, Integer setor, boolean isAdmin);

    /**
     * Desativa um módulo no sistema.
     * @param moduleID identificador do módulo
     */
    void disableModule(UUID moduleID);

    /**
     * Busca um módulo pelo seu ID.
     * @param id identificador do módulo
     * @return módulo encontrado
     */
    Modulo getModuleByID(UUID id);

    /**
     * Busca um módulo pelo seu nome.
     * @param name nome do módulo
     * @return módulo encontrado
     */
    Modulo getByName(String name);

    /**
     * Lista todos os módulos onde um usuário é responsável.
     * @param user identificador do usuário
     * @return lista de módulos
     */
    List<ModuleResponseDTO> getAllByResponsavel(UUID user, boolean isAdmin);

    /**
     * Lista todos os módulos do sistema, independente do status.
     * @return lista de todos os módulos
     */
    List<Modulo> getAll();

    /**
     * Atualiza informações básicas de um módulo.
     * @param moduleID    identificador do módulo
     * @param name        novo nome do módulo
     * @param description nova descrição do módulo
     */
    void updateModule(String moduleID, String name, String description, List<Integer> setores);

    /**
     * Adiciona um gerente a um módulo.
     * @param moduleId identificador do módulo
     * @param userId   identificador do usuário a ser adicionado como gerente
     */
    void addGerente(UUID moduleId, UUID userId);

    /**
     * Remove um gerente de um módulo.
     * @param moduleId identificador do módulo
     * @param userId   identificador do usuário a ser removido como gerente
     */
    void removeGerente(UUID moduleId, UUID userId);

    /**
     * Verifica se o usuário é um gerente de módulo.
     * @param moduleId
     * @param userId
     * @return true para gerente e false para não gerente.
     */
    boolean isGerente(String moduleId, String userId);

    /**
     * Verifica se o usuário é gerente de pelo menos um módulo.
     * @param userId identificador do usuário
     * @return true se for gerente de qualquer módulo
     */
    boolean isGerenteOfAnyModule(String userId);

}
