package com.indux.modules.organization_chart.application.services;

import com.indux.modules.organization_chart.domain.entities.jpa.HcmEntity;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationHcmRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class HcmService {
    private final OrganizationHcmRepository organizationHcmRepository;

    public HcmService(OrganizationHcmRepository organizationHcmRepository) {
        this.organizationHcmRepository = organizationHcmRepository;
    }

    public Page<HcmEntity> getAllHcm(Pageable pageable) {
        return organizationHcmRepository.findAllSortedById(pageable);
    }

    public Page<HcmEntity> search(Object value, Pageable pageable) {
        if (value instanceof String strValue) {
            Integer parsedInt = tryParseInt(strValue);
            if (parsedInt != null) {
                return organizationHcmRepository.findByCcId(parsedInt, pageable)
                        .orElse(Page.empty());
            } else {
                return organizationHcmRepository.findByNomeCcContainingIgnoreCase(strValue, pageable)
                        .orElse(Page.empty());
            }
        }
        return Page.empty();
    }

    public Page<HcmEntity> getHcmFromActiveProjects(String search, Pageable pageable) {
        return organizationHcmRepository.findHcmUsedByActiveProjects(search, pageable);
    }

    private Integer tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
