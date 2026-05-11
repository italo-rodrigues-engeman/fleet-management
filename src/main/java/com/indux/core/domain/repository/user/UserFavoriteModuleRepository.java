package com.indux.core.domain.repository.user;

import com.indux.core.domain.model.user.UserFavoriteModule;
import com.indux.core.domain.model.user.UserModuleKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserFavoriteModuleRepository
        extends JpaRepository<UserFavoriteModule, UserModuleKey> {
    List<UserFavoriteModule> findByUserId(UUID userId);
    
    void deleteByModuleId(UUID moduleId);
}
