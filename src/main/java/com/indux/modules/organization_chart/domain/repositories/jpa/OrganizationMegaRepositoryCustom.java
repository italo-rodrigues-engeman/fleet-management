package com.indux.modules.organization_chart.domain.repositories.jpa;

import com.indux.modules.organization_chart.domain.entities.jpa.MegaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrganizationMegaRepositoryCustom {
    Page<MegaEntity> findAllSortedByApelido(Pageable pageable);

    Page<MegaEntity> searchByApelidoOrDescricao(String apelido, String descricao, Pageable pageable);

    Page<MegaEntity> findMegasWithoutProject(Pageable pageable);
}
