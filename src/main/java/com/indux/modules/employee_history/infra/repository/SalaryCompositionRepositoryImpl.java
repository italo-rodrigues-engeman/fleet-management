package com.indux.modules.employee_history.infra.repository;

import com.indux.modules.employee_history.application.dto.FilterSalaryComposition;
import com.indux.modules.employee_history.application.dto.SalaryCompositionResponse;
import com.indux.modules.employee_history.domain.entity.SalaryCompositionEntity;
import com.indux.modules.employee_history.domain.repository.SalaryCompositionRepositoryCustom;
import com.indux.modules.employee_history.infra.mapper.SalaryCompositionMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class SalaryCompositionRepositoryImpl implements SalaryCompositionRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    private final SalaryCompositionMapper salaryCompositionMapper;

    public SalaryCompositionRepositoryImpl(SalaryCompositionMapper salaryCompositionMapper) {
        this.salaryCompositionMapper = salaryCompositionMapper;
    }

    @Override
    public Page<SalaryCompositionResponse> findByFilter(FilterSalaryComposition filter, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SalaryCompositionEntity> query = cb.createQuery(SalaryCompositionEntity.class);
        Root<SalaryCompositionEntity> root = query.from(SalaryCompositionEntity.class);

        List<Predicate> predicates = buildPredicates(filter, cb, root);
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        query.orderBy(
                cb.asc(cb.length(root.get("registration"))),
                cb.asc(root.get("registration"))
        );

        List<SalaryCompositionEntity> results = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = executeCountQuery(filter, cb);

        return new PageImpl<>(salaryCompositionMapper.toResponseList(results), pageable, total);
    }

    private Long executeCountQuery(FilterSalaryComposition filter, CriteriaBuilder cb) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<SalaryCompositionEntity> countRoot = countQuery.from(SalaryCompositionEntity.class);

        List<Predicate> countPredicates = buildPredicates(filter, cb, countRoot);
        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }

        countQuery.select(cb.count(countRoot));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<Predicate> buildPredicates(FilterSalaryComposition filter,
            CriteriaBuilder cb,
            Root<SalaryCompositionEntity> root) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter == null) {
            return predicates;
        }

        if (filter.getRegistrations() != null && !filter.getRegistrations().isEmpty()) {
            predicates.add(root.get("registration").in(filter.getRegistrations()));
        }

        if (filter.getFilialIds() != null && !filter.getFilialIds().isEmpty()) {
            predicates.add(root.get("filialIdHcm").in(filter.getFilialIds()));
        }

        return predicates;
    }
}
