package com.indux.modules.organization_chart.application.services;

import com.indux.modules.organization_chart.domain.entities.jpa.MegaEntity;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationMegaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MegaService {
    private final OrganizationMegaRepository organizationMegaRepository;

    public MegaService(OrganizationMegaRepository organizationMegaRepository) {
        this.organizationMegaRepository = organizationMegaRepository;
    }

    public Page<MegaEntity> getAllMegas(Pageable pageable) {
        return organizationMegaRepository.findAllSortedByApelido(pageable);
    }

    public Page<MegaEntity> search(Object value, Pageable pageable) {
        if (value instanceof String strValue) {
                return organizationMegaRepository.searchByApelidoOrDescricao(strValue,strValue, pageable);

        }
        return Page.empty();
    }

    public Page<MegaEntity> notAssociated(Pageable pageable) {
        return organizationMegaRepository.findMegasWithoutProject(pageable);
    }

}
