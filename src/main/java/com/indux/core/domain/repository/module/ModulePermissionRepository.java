package com.indux.core.domain.repository.module;

import com.indux.core.domain.model.modules.ModulePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ModulePermissionRepository extends JpaRepository<ModulePermission, UUID> {
}
