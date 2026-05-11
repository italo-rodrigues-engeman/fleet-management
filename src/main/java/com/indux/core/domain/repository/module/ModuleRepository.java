package com.indux.core.domain.repository.module;

import com.indux.core.domain.model.modules.Modulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ModuleRepository extends JpaRepository<Modulo, UUID> {
    Optional<Modulo> findByName(String name);

    @Query("SELECT m FROM Modulo m JOIN m.permissoes p WHERE p.responsable = :userId")
    List<Modulo> findAllByResponsavel(@Param("userId") UUID userId);

    List<Modulo> findAllByDesativadoFalse();

    List<Modulo> findAllBySetoresContaining(Integer setor);

    @Query("SELECT COUNT(m) > 0 FROM Modulo m WHERE :userId MEMBER OF m.gerentes")
    boolean existsModuleWhereUserIsGerente(@Param("userId") UUID userId);

}
