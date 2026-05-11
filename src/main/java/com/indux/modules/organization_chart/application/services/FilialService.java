package com.indux.modules.organization_chart.application.services;

import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationFilialRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FilialService {
    private final OrganizationFilialRepository  organizationFilialRepository;

    public FilialService(OrganizationFilialRepository organizationFilialRepository) {
        this.organizationFilialRepository = organizationFilialRepository;
    }

    public Page<FilialHcmEntity> getAllFilialHcm(Pageable pageable) {
        return organizationFilialRepository.findAllSortedById(pageable);
    }

    public Page<FilialHcmEntity> search(Object value, Pageable pageable) {
        if (value instanceof String strValue) {
            Integer parsedInt = tryParseInt(strValue);
            if (parsedInt != null) {
                return organizationFilialRepository.findByFilialId(parsedInt, pageable)
                        .orElse(Page.empty());
            } else {
                return organizationFilialRepository.findByNomeFilialContainingIgnoreCase(strValue, pageable)
                        .orElse(Page.empty());
            }
        }
        return Page.empty();
    }

    private Integer tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public List<Map<String, Object>> filialOrganization(List<Integer> filialHCM){
        return organizationFilialRepository.findFilialOrganization(filialHCM, null);
    }

    public List<Map<String, Object>> filialOrganizationSearch(List<Integer> filialHCM, String search){
        return organizationFilialRepository.findFilialOrganization(filialHCM, search);
    }

    public Page<FilialHcmEntity> findNotAssociated(Pageable pageable){
        return organizationFilialRepository.findAllWithStatusNullSortedById(pageable);
    }

    public Optional<FilialHcmEntity> getFilialById(Integer filialId) {
        return organizationFilialRepository.findById(filialId);
    }
}
