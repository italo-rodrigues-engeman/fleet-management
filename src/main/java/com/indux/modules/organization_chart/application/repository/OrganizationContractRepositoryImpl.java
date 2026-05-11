package com.indux.modules.organization_chart.application.repository;

import com.indux.modules.organization_chart.domain.entities.jpa.ContractEntity;
import com.indux.modules.organization_chart.domain.entities.models.ContractType;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationContractRepositoryCustom;
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
public class OrganizationContractRepositoryImpl implements OrganizationContractRepositoryCustom {

    @PersistenceContext
    private final EntityManager entityManager;

    @Override
    public Page<ContractEntity> findAllWithFilters(String searchTerm, List<Long> organizationIds, ContractType tipo,
            Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<ContractEntity> query = cb.createQuery(ContractEntity.class);
        Root<ContractEntity> contractRoot = query.from(ContractEntity.class);

        List<Predicate> predicates = buildPredicates(cb, contractRoot, searchTerm, organizationIds, tipo);

        query.where(predicates.toArray(new Predicate[0]));

        query.orderBy(cb.asc(contractRoot.get("name")));

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<ContractEntity> countRoot = countQuery.from(ContractEntity.class);

        List<Predicate> countPredicates = buildPredicates(cb, countRoot, searchTerm, organizationIds, tipo);

        countQuery.select(cb.count(countRoot)).where(countPredicates.toArray(new Predicate[0]));

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        List<ContractEntity> results = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        return new PageImpl<>(results, pageable, total);
    }

    /**
     * Builds predicates for both main query and count query
     */
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<ContractEntity> root, String searchTerm,
            List<Long> organizationIds, ContractType tipo) {
        List<Predicate> predicates = new ArrayList<>();

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String lowerSearchTerm = searchTerm.toLowerCase();
            Predicate namePredicate = cb.like(cb.lower(root.get("name")), "%" + lowerSearchTerm + "%");
            Predicate nicknamePredicate = cb.like(cb.lower(root.get("nickname")), "%" + lowerSearchTerm + "%");
            Predicate osPredicate = cb.like(cb.lower(root.get("os")), "%" + lowerSearchTerm + "%");

            predicates.add(cb.or(namePredicate, nicknamePredicate, osPredicate));
        }

        if (tipo != null) {
            predicates.add(cb.equal(root.get("type"), tipo));
        }

        if (organizationIds != null && !organizationIds.isEmpty()) {
            List<Long> allSubordinateIds = new ArrayList<>();

            for (Long organizationId : organizationIds) {
                String sql = """
                        WITH RECURSIVE subordinates AS (
                            -- Base case: start with the given organization
                            SELECT id FROM tb_organograma WHERE id = :organizationId
                            UNION ALL
                            -- Recursive case: find all children
                            SELECT o.id FROM tb_organograma o
                            INNER JOIN subordinates s ON o.subordinado = s.id
                        )
                        SELECT id FROM subordinates
                        """;

                List<Long> subordinateIds = entityManager.createNativeQuery(sql)
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

                allSubordinateIds.addAll(subordinateIds);
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
