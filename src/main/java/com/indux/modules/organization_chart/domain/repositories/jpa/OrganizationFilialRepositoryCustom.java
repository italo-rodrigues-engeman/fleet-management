package com.indux.modules.organization_chart.domain.repositories.jpa;

import java.util.List;
import java.util.Map;

public interface OrganizationFilialRepositoryCustom {
    List<Map<String, Object>> findFilialOrganization(List<Integer> filialHCM, String search);
}