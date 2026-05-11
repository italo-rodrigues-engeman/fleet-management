package com.indux.core.application.service.module;

import com.indux.core.application.dto.module.CreateModuleDTO;
import com.indux.core.application.dto.module.ModuleResponseDTO;
import com.indux.core.application.dto.user.UserNameProjection;
import com.indux.core.application.mapper.ModuleResponseMapper;
import com.indux.core.domain.model.modules.ModulePermission;
import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.model.modules.StepModule;
import com.indux.core.domain.repository.module.ModuleRepository;
import com.indux.core.domain.service.module.ModuleManagementService;
import com.indux.core.domain.service.user.FavoriteModuleService;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service("moduleService")
@Transactional
public class ModuleManagementServiceImpl implements ModuleManagementService {
    private final ModuleRepository repository;
    private final ModuleResponseMapper moduleResponseMapper;
    private final FavoriteModuleService favoriteService;
    private final UserService userService;

    public ModuleManagementServiceImpl(ModuleRepository repository, ModuleResponseMapper moduleResponseMapper, FavoriteModuleService favoriteService, UserService userService) {
        this.repository = repository;
        this.moduleResponseMapper = moduleResponseMapper;
        this.favoriteService = favoriteService;
        this.userService = userService;
    }

    @Override
    public void createModule(CreateModuleDTO request, UUID creatorId) {
        if (request.etapas().size() != request.quantidadeEtapas()) {
            throw new ModuleFailure(
                    "A quantidade de etapas não bate com a quantidade informada");
        }

        Set<StepModule> steps = new HashSet<>(request.etapas().size());
        Set<ModulePermission> permissoes = new HashSet<>(request.permissoes().size());

        for (var etapa : request.etapas()) {
            steps.add(StepModule.builder()
                    .etapa(etapa.numEtapa())
                    .tempo(etapa.tempoAceitavel())
                    .nome(etapa.nome())
                    .build());
        }

        for (var dto : request.permissoes()) {
            permissoes.add(ModulePermission.builder()
                    .responsable(dto.user())
                    .isGroup(dto.isGroup())
                    .regionais(Optional.ofNullable(dto.regionais()).orElse(Set.of()))
                    .projetos(Optional.ofNullable(dto.projetos()).orElse(Set.of()))
                    .stepsAllowed(new ArrayList<>(Optional.ofNullable(dto.etapasResponsaveis()).orElse(Set.of())))
                    .actions(dto.acoes())
                    .build());
        }

        Modulo newModule = Modulo.builder()
                .name(request.nome())
                .stepsQuantity(request.quantidadeEtapas())
                .configEtapas(steps)
                .desativado(false)
                .permissoes(new ArrayList<>())
                .setores(Optional.ofNullable(request.setores())
                        .map(HashSet::new)
                        .orElseGet(HashSet::new))
                .build();

        for (var permissao : permissoes) {
            newModule.addPermissao(permissao);
        }

        newModule.addGerente(creatorId);

        repository.save(newModule);
    }

    @Override
    public void deleteModule(String moduleID) {
        UUID moduleUuid = UUID.fromString(moduleID);        
        favoriteService.removeAllUsersFromModule(moduleUuid);
        repository.deleteById(moduleUuid);
    }

    @Override
    public void updateModuleName(String moduleID, String newName) {
        Modulo modulo = repository.findById(UUID.fromString(moduleID))
                .orElseThrow(() -> new ModuleNotFoundFailure("Modulo não encontrado"));
        modulo.setName(newName);
        repository.save(modulo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Modulo> listAllModules() {
        return repository.findAllByDesativadoFalse();
    }

    @Override
    public List<ModuleResponseDTO> listBySetor(UUID user, Integer setor, boolean isAdmin) {
        Set<UUID> favoriteIds = favoriteService.listFavorites(user).stream()
                .map(ModuleResponseDTO::id)
                .collect(Collectors.toSet());
        var result = repository.findAllBySetoresContaining(setor);
        return result.stream().map(mod -> moduleResponseMapper.toDTO(
                        mod,
                        user,
                        isAdmin,
                        favoriteIds.contains(mod.getId())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModuleResponseDTO> getAllByResponsavel(UUID user, boolean isAdmin) {
        Set<UUID> favoriteIds = favoriteService.listFavorites(user).stream()
                .map(ModuleResponseDTO::id)
                .collect(Collectors.toSet());
        return repository.findAllByResponsavel(user).stream()
                .map(mod -> moduleResponseMapper.toDTO(
                        mod,
                        user,
                        isAdmin,
                        favoriteIds.contains(mod.getId())
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Modulo getModuleByID(UUID id) {
        Modulo modulo = repository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Módulo não encontrado."));

        if (modulo.getPermissoes() == null || modulo.getPermissoes().isEmpty()) {
            return modulo;
        }

        List<UUID> userIds = modulo.getPermissoes().stream()
                .map(ModulePermission::getResponsable)
                .distinct()
                .toList();

        Map<UUID, UserNameProjection> usersMap = userService.getUserNamesIn(userIds).stream()
                .collect(Collectors.toMap(UserNameProjection::getId, Function.identity()));

        modulo.getPermissoes().forEach(perm -> {
            UserNameProjection user = usersMap.get(perm.getResponsable());
            if (user != null) {
                perm.setResponsableName(user.getName());
            }
        });

        return modulo;
    }

    @Override
    @Transactional(readOnly = true)
    public Modulo getByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new ModuleNotFoundFailure("Módulo não encontrado."));
    }

    @Override
    public void disableModule(UUID moduleID) {
        Modulo modulo = repository.findById(moduleID)
                .orElseThrow(() -> new ModuleNotFoundFailure("Modulo não encontrado"));
        modulo.setDesativado(true);
    }

    @Override
    public List<Modulo> getAll() {
        return repository.findAll();
    }

    @Override
    public void updateModule(String moduleID, String name, String description, List<Integer> setores) {
        Modulo module = repository.findById(UUID.fromString(moduleID))
                .orElseThrow(() -> new ModuleNotFoundFailure("Modulo não encontrado"));

        if (name != null && !name.isBlank()) module.setName(name);
        if (description != null && !description.isBlank()) module.setDescription(description);
        if (setores != null && !setores.isEmpty()) module.setSetores(new HashSet<>(setores));

        repository.save(module);
    }


    @Override
    public void addGerente(UUID moduleId, UUID userId) {
        Modulo modulo = repository.findById(moduleId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Módulo não encontrado"));
        modulo.addGerente(userId);
        repository.save(modulo);
    }

    @Override
    public void removeGerente(UUID moduleId, UUID userId) {
        Modulo modulo = repository.findById(moduleId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Módulo não encontrado"));
        modulo.removeGerente(userId);
        repository.save(modulo);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isGerente(String moduleId, String userId) {
        try {
            UUID moduleUuid = UUID.fromString(moduleId);
            UUID userUuid = UUID.fromString(userId);
            return repository.findById(moduleUuid).map(m -> m.isGerente(userUuid)).orElse(false);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isGerenteOfAnyModule(String userId) {
        try {
            UUID userUuid = UUID.fromString(userId);
            return repository.existsModuleWhereUserIsGerente(userUuid);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
