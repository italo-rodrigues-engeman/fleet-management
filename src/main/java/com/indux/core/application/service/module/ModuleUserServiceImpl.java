package com.indux.core.application.service.module;

import com.indux.core.application.dto.module.BatchModulePermissionsDTO;
import com.indux.core.application.dto.module.ModuleFilter;
import com.indux.core.application.dto.module.ModulePermissionInput;
import com.indux.core.application.dto.module.MultipleUsersModulePermissionsDTO;
import com.indux.core.domain.model.auth.User;
import com.indux.core.domain.model.modules.ModulePermission;
import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.model.modules.StepModule;
import com.indux.core.domain.repository.module.ModuleRepository;
import com.indux.core.domain.repository.user.UserRepository;
import com.indux.core.domain.service.module.ModuleUserService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ModuleUserServiceImpl implements ModuleUserService {
    private final ModuleRepository repository;
    private final UserRepository userRepository;

    public ModuleUserServiceImpl(ModuleRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Override
    public void addUserToModule(String moduleID, ModulePermissionInput permission) {
        Modulo modulo = repository.findById(UUID.fromString(moduleID))
                .orElseThrow(() -> new ModuleNotFoundFailure("Módulo não encontrado."));

        addUserPermission(permission, modulo);

        repository.save(modulo);
    }

    @Override
    public void addUserInAllModules(ModulePermissionInput permission) {
        List<Modulo> modules = repository.findAll();

        List<Modulo> modulesToSave = new ArrayList<>();

        modules.forEach(module -> {

            boolean userAlreadyHasPermission = module.getPermissoes().stream()
                    .anyMatch(p -> p.getResponsable().equals(permission.user()));

            if (userAlreadyHasPermission) {
                return;
            }

            addUserPermission(permission, module);

            modulesToSave.add(module);
        });

        repository.saveAll(modulesToSave);
    }

    @Override
    public void removeUsersFromModule(String moduleID, UUID userID) {
        Modulo modulo = repository.findById(UUID.fromString(moduleID)).orElseThrow(() -> new ModuleNotFoundFailure("Modulo não encontrado."));
        modulo.getPermissoes().removeIf(e -> e.getResponsable().equals(userID));
        repository.save(modulo);
    }

    @Override
    public void updateUserPermissions(BatchModulePermissionsDTO dto) {
        UUID userId = dto.userId();
        dto.permissoes().forEach(input -> {
            Modulo modulo = repository.findById(input.moduleId())
                    .orElseThrow(() -> new ModuleNotFoundFailure(
                            "Módulo não encontrado: " + input.moduleId()));
            modulo.updateOrCreatePermission(userId, input);
        });
    }

    @Override
    public void addPermissionsBatch(BatchModulePermissionsDTO dto) {
        UUID userId = dto.userId();
        dto.permissoes().forEach(input -> {
            Modulo modulo = repository.findById(input.moduleId())
                    .orElseThrow(() -> new ModuleNotFoundFailure(
                            "Módulo não encontrado: " + input.moduleId()));
            modulo.updateOrCreatePermission(userId, input);
        });
    }

    @Override
    public void removeUserFromAllModules(UUID userId) {
        List<Modulo> modulos = repository.findAllByResponsavel(userId);

        modulos.forEach(modulo -> {
            boolean removed = modulo.getPermissoes().removeIf(p -> p.getResponsable().equals(userId));
            if (removed) {
                repository.save(modulo);
            }
        });
    }

    @Override
    public List<Modulo> listModulesWithPermission(String userID) {
        return repository.findAllByResponsavel(UUID.fromString(userID));
    }

    @Override
    public List<String> filterPeopleInModule(UUID moduleID, ModuleFilter filter) {
        Modulo modulo = repository.findById(moduleID)
                .orElseThrow(() -> new ModuleNotFoundFailure("Módulo não encontrado"));

        if (filter == null) {
            throw new ModuleFailure("Filtro não pode ser nulo");
        }

        List<ModulePermission> filteredPermissions = modulo.getPermissoes()
                .stream()
                .filter(permission -> permission.getStepsAllowed().contains(filter.getStep()))
                .filter(permission -> {
                    boolean projetoOK =
                            filter.getProject() != null
                                    && (filter.getProject() == 0
                                    || permission.getProjetos().contains(0)
                                    || permission.getProjetos().contains(filter.getProject()));

                    boolean regionalOK =
                            filter.getNewRegional() != null
                                    && (filter.getNewRegional() == 0
                                    || permission.getRegionais().contains(0)
                                    || permission.getRegionais().contains(filter.getNewRegional()));

                    boolean contratoOK =
                            filter.getContract() != null
                                    && (filter.getContract() == 0
                                    || permission.getContratos().contains(0)
                                    || permission.getContratos().contains(filter.getContract()));

                    boolean filialOK =
                            filter.getRegional() != null
                                    && (filter.getRegional() == 0
                                    || permission.getFiliais().contains(0)
                                    || permission.getFiliais().contains(filter.getRegional()));

                    return (contratoOK || filialOK) || (projetoOK && regionalOK);
                })
                .toList();

        return userRepository.findAllById(
                filteredPermissions.stream()
                        .map(ModulePermission::getResponsable)
                        .collect(Collectors.toSet())).stream().map(User::getName).toList();
    }

    @Override
    public void addMultipleUsersToModule(String moduleID, MultipleUsersModulePermissionsDTO dto) {
        Modulo modulo = repository.findById(UUID.fromString(moduleID))
                .orElseThrow(() -> new ModuleNotFoundFailure("Módulo não encontrado."));

        dto.permissoes().forEach(userPerm -> {
            UUID userId = UUID.fromString(userPerm.responsable());

            // Verifica se o usuário existe
            userRepository.findById(userId)
                    .orElseThrow(() -> new ModuleFailure("Usuário não encontrado: " + userId));

            // Converte as listas para sets
            Set<Integer> filiais = new HashSet<>(userPerm.filiais());
            Set<Integer> contratos = new HashSet<>(userPerm.contratos());
            Set<Integer> actions = new HashSet<>(userPerm.actions());

            // Processa as etapas permitidas
            List<Integer> stepsAllowed = new ArrayList<>(userPerm.stepsAllowed());

            ModulePermission permissionUser = ModulePermission.builder()
                    .responsable(userId)
                    .isGroup(userPerm.group())
                    .filiais(filiais)
                    .contratos(contratos)
                    .stepsAllowed(stepsAllowed)
                    .actions(actions)
                    .build();

            modulo.addPermissao(permissionUser);
        });

        repository.save(modulo);
    }

    public void addUserPermission(ModulePermissionInput permission, Modulo module) {
        List<Integer> steps = permission.etapasResponsaveis().contains(-1)
                ? module.getConfigEtapas()
                .stream()
                .map(StepModule::getEtapa)
                .collect(Collectors.toList())
                : new ArrayList<>(permission.etapasResponsaveis());

        ModulePermission permissionUser = ModulePermission.builder()
                .responsable(permission.user())
                .isGroup(permission.isGroup())
                .regionais(permission.regionais())
                .projetos(permission.projetos())
                .stepsAllowed(steps)
                .actions(permission.acoes())
                .build();

        module.addPermissao(permissionUser);
    }
}
