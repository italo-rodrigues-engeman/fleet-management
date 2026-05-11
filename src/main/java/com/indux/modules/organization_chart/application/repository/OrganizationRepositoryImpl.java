package com.indux.modules.organization_chart.application.repository;

import com.indux.modules.organization_chart.domain.entities.jpa.OrganizationEntity;
import com.indux.modules.organization_chart.domain.entities.models.OrganizationType;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class OrganizationRepositoryImpl implements OrganizationRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Page<OrganizationEntity> findAllByTypeWithFilters(Pageable pageable, OrganizationType type,
            String searchTerm, List<Long> subordinadoIds) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<OrganizationEntity> query = cb.createQuery(OrganizationEntity.class);
        Root<OrganizationEntity> root = query.from(OrganizationEntity.class);

        List<Predicate> predicates = buildPredicates(cb, root, type, searchTerm, subordinadoIds);

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.asc(root.get("position")));

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<OrganizationEntity> countRoot = countQuery.from(OrganizationEntity.class);

        List<Predicate> countPredicates = buildPredicates(cb, countRoot, type, searchTerm, subordinadoIds);

        countQuery.select(cb.count(countRoot)).where(countPredicates.toArray(new Predicate[0]));

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        List<OrganizationEntity> results = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new PageImpl<>(results, pageable, total);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<OrganizationEntity> root,
            OrganizationType type, String searchTerm, List<Long> subordinadoIds) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(root.get("type"), type));

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String lowerSearchTerm = searchTerm.toLowerCase();
            Predicate positionPredicate = cb.like(cb.lower(root.get("position")),
                    "%" + lowerSearchTerm + "%");
            Predicate collaboratorNamePredicate = cb.like(cb.lower(root.get("collaborator").get("name")),
                    "%" + lowerSearchTerm + "%");

            predicates.add(cb.or(positionPredicate, collaboratorNamePredicate));
        }

        if (subordinadoIds != null && !subordinadoIds.isEmpty()) {
            List<Long> allSubordinateIds = new ArrayList<>();

            for (Long organizationId : subordinadoIds) {
                String sql = """
                        WITH RECURSIVE subordinates AS (
                            SELECT id FROM tb_organograma WHERE id = :organizationId
                            UNION ALL
                            SELECT o.id FROM tb_organograma o
                            INNER JOIN subordinates s ON o.subordinado = s.id
                        )
                        SELECT id FROM subordinates
                        """;

                List<Long> recursiveIds = entityManager.createNativeQuery(sql)
                        .setParameter("organizationId", organizationId)
                        .getResultList()
                        .stream()
                        .map(result -> {
                            if (result instanceof Number) {
                                return ((Number) result).longValue();
                            }
                            return Long.valueOf(result.toString());
                        })
                        .toList();

                allSubordinateIds.addAll(recursiveIds);
            }

            if (!allSubordinateIds.isEmpty()) {
                predicates.add(root.get("subordinate").get("id").in(allSubordinateIds));
            } else {
                predicates.add(cb.equal(cb.literal(1), 2));
            }
        }

        return predicates;
    }
}
