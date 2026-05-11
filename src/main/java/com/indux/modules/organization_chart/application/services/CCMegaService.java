package com.indux.modules.organization_chart.application.services;

import com.indux.modules.organization_chart.domain.entities.jpa.CCMegaEntity;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationCCMegaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CCMegaService {
    private final OrganizationCCMegaRepository organizationMegaRepository;

    public CCMegaService(OrganizationCCMegaRepository organizationMegaRepository) {
        this.organizationMegaRepository = organizationMegaRepository;
    }


    public Page<CCMegaEntity> getAllMegas(Pageable pageable) {
        return organizationMegaRepository.findAllSortedByApelido(pageable);
    }

    public Page<CCMegaEntity> search(Object value, Pageable pageable) {
        if (value instanceof String strValue) {
                return organizationMegaRepository.searchByApelidoOrDescricao(strValue,strValue, pageable);
        }
        return Page.empty();
    }

}
