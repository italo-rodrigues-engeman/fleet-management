package com.indux.core.domain.repository.module;

import com.indux.core.domain.model.modules.ActionModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActionsModuleRepository extends JpaRepository<ActionModule, Long> {
}
