package com.indux.core.application.service.user;

import com.indux.core.application.dto.module.ModuleResponseDTO;
import com.indux.core.application.mapper.ModuleResponseMapper;
import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.model.user.UserFavoriteModule;
import com.indux.core.domain.model.user.UserModuleKey;
import com.indux.core.domain.repository.module.ModuleRepository;
import com.indux.core.domain.repository.user.UserFavoriteModuleRepository;
import com.indux.core.domain.repository.user.UserRepository;
import com.indux.core.domain.service.user.FavoriteModuleService;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class FavoriteModuleServiceImpl implements FavoriteModuleService {
    private final UserRepository userRepo;
    private final ModuleRepository moduloRepo;
    private final UserFavoriteModuleRepository favRepo;
    private final ModuleResponseMapper moduleResponseMapper;

    public FavoriteModuleServiceImpl(
            UserRepository userRepo,
            ModuleRepository moduloRepo,
            UserFavoriteModuleRepository favRepo,
            ModuleResponseMapper moduleResponseMapper) {
        this.userRepo = userRepo;
        this.moduloRepo = moduloRepo;
        this.favRepo = favRepo;
        this.moduleResponseMapper = moduleResponseMapper;
    }

    @Override
    public void addFavorite(UUID userId, UUID moduleId) {
        var user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundEmployee("Usuário não existe: " + userId));
        var modulo = moduloRepo.findById(moduleId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Módulo não existe: " + moduleId));
        var key = new UserModuleKey(userId, moduleId);
        if (favRepo.existsById(key))
            return;
        var fav = new UserFavoriteModule();
        fav.setId(key);
        fav.setUser(user);
        fav.setModule(modulo);
        favRepo.save(fav);
    }

    @Override
    public void removeFavorite(UUID userId, UUID moduleId) {
        favRepo.deleteById(new UserModuleKey(userId, moduleId));
    }

    @Override
    public List<ModuleResponseDTO> listFavorites(UUID userId) {
        return favRepo.findByUserId(userId).stream()
                .map(fav -> {
                    Modulo modulo = fav.getModule();
                    return moduleResponseMapper.toDTO(modulo, userId, false, true);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void removeAllUsersFromModule(UUID moduleId) {
        moduloRepo.findById(moduleId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Módulo não existe: " + moduleId));
        
        favRepo.deleteByModuleId(moduleId);
    }
}
