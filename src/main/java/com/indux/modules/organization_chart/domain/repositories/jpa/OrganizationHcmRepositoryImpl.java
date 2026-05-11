package com.indux.modules.organization_chart.domain.repositories.jpa;

import com.indux.modules.organization_chart.domain.entities.jpa.HcmEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrganizationHcmRepositoryImpl implements OrganizationHcmRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<HcmEntity> findHcmUsedByActiveProjects(String search, Pageable pageable) {

        String baseJpql = "SELECT DISTINCT h FROM ProjectEntity p JOIN p.hcm h " +
                "WHERE p.ativo = true ";

        String searchCondition = "";
        if (search != null && !search.trim().isEmpty()) {
            String trimmed = search.trim();
            try {
                Integer.parseInt(trimmed);
                searchCondition = "AND (CAST(h.ccId AS string) LIKE :search " +
                        "OR LOWER(h.nomeCc) LIKE :searchLower) ";
            } catch (NumberFormatException e) {
                searchCondition = "AND LOWER(h.nomeCc) LIKE :searchLower ";
            }
        }

        String orderClause = "ORDER BY h.ccId ASC";

        String fullJpql = baseJpql + searchCondition + orderClause;
        String countJpql = "SELECT COUNT(DISTINCT h.ccId) FROM ProjectEntity p JOIN p.hcm h " +
                "WHERE p.ativo = true " + searchCondition;

        TypedQuery<HcmEntity> query = entityManager.createQuery(fullJpql, HcmEntity.class);
        TypedQuery<Long> countQuery = entityManager.createQuery(countJpql, Long.class);

        if (search != null && !search.trim().isEmpty()) {
            String trimmed = search.trim();
            try {
                Integer.parseInt(trimmed);
                query.setParameter("search", trimmed + "%");
                countQuery.setParameter("search", trimmed + "%");
            } catch (NumberFormatException ignored) {
            }
            query.setParameter("searchLower", "%" + trimmed.toLowerCase() + "%");
            countQuery.setParameter("searchLower", "%" + trimmed.toLowerCase() + "%");
        }

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<HcmEntity> results = query.getResultList();
        Long total = countQuery.getSingleResult();

        return new PageImpl<>(results, pageable, total);
    }
}
