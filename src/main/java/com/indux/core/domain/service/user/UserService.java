package com.indux.core.domain.service.user;

import com.indux.core.application.dto.generic.EmployeeDTO;
import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.application.dto.user.SystemUserInfoDTO;
import com.indux.core.application.dto.user.UserFilter;
import com.indux.core.application.dto.user.UserNameProjection;
import com.indux.core.domain.model.auth.User;
import com.indux.core.domain.model.employee.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {
    /**
     * Retorna todos os usuários cadastrados no sistema.
     * @return lista de todos os usuários
     */
    List<SimpleUser> getAllUsers();

    /**
     * Busca um usuário pelo seu ID.
     * @param id identificador do usuário
     * @return Optional contendo o usuário se encontrado
     */
    Optional<SimpleUser> getUserById(String id);

    /**
     * Pesquisa usuários com base em um filtro de texto.
     * @param filter texto para filtrar usuários
     * @return lista de usuários que correspondem ao filtro
     */
    Page<SimpleUser> searchUsers(Pageable pageable, String filter);

    /**
     * Busca um usuário pelo seu CPF.
     * @param cpf CPF do usuário
     * @return usuário encontrado
     */
    User getUserByCPF(String cpf);

    /**
     * Conta o número total de usuários no sistema.
     * @return quantidade total de usuários
     */
    long countUsers();

    /**
     * Obtém informações do sistema relacionadas aos usuários.
     * @return DTO contendo informações do sistema
     */
    SystemUserInfoDTO getSystemInfo();

    /**
     * Atualiza a role de um usuário.
     * @param id     identificador do usuário
     * @param roleID identificador da nova role
     */
    void updateRoleUser(UUID id, long roleID);

    /**
     * Desativa um usuário no sistema.
     * @param id identificador do usuário
     */
    void disableUser(UUID id);

    /**
     * Reativa um usuário previamente desativado.
     * @param id identificador do usuário
     */
    void enableUser(UUID id);

    /**
     * Remove permanentemente um usuário do sistema.
     * @param id identificador do usuário
     */
    void deleteUser(UUID id);

    /**
     * Atualiza a imagem de perfil de um usuário.
     * Salva a imagem comprimida e uma thumb para ser utilizada nos ícones.
     * @param user identificador do usuário
     * @param file arquivo de imagem
     * @return URI da imagem atualizada
     */
    String updateProfileImageUser(UUID user, MultipartFile file);

    EmployeeDTO getEmployeeFromUser(UUID user);

    Employee getCompleteEmployeeFromUser(UUID user);

    /**
     * Obtém uma lista de nomes de usuários por uma lista de ids.
     * @param ids
     * @return lista de usuários
     */
    List<UserNameProjection> getUserNamesIn(List<UUID> ids);

    /**
     * Retorna lista de usuários (apenas ID e Nome) com filtro opcional por nome (Case Sensitive).
     * @param nameFilter filtro de nome (opcional)
     * @param pageable paginação
     * @return página de projeções de usuário
     */
    Page<UserNameProjection> getBasicUsers(String nameFilter, Pageable pageable);

    Page<SimpleUser> getFilteredUsers(UserFilter filter, Pageable pageable);
}
