package com.indux.modules.crm.domain.gateway;

import com.indux.modules.crm.domain.entity.Unit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UnitGateway {
    /**
     * Salvar uma nova unidade, <b>não é possível existir</b> uma unidade sem empresa.
     * @param unit Entidade da Unidade
     * @param companyId ID da empresa que a unidade faz parte.
     * @return
     */
    Unit save(Unit unit, String companyId);

    Page<Unit> getAll(Pageable pageable);

    Unit getById(String id);

    Page<Unit> getAllByCompany(Pageable pageable, String companyId);

    Unit update(Unit unit, String id);

    void toggleStatus(String id);

    void delete(String id);
}
