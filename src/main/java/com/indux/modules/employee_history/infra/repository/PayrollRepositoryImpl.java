package com.indux.modules.employee_history.infra.repository;

import com.indux.modules.employee_history.application.dto.FilterPayroll;
import com.indux.modules.employee_history.application.dto.PayrollResponse;
import com.indux.modules.employee_history.domain.entity.PayrollEntity;
import com.indux.modules.employee_history.domain.repository.PayrollRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Repository
public class PayrollRepositoryImpl implements PayrollRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<PayrollResponse> findFilterPayroll(FilterPayroll filter, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<PayrollEntity> root = query.from(PayrollEntity.class);

        query.multiselect(
                root.get("id").alias("id"),
                root.get("competence").alias("competence"),
                root.get("filialHcm").alias("filialHcm"),
                root.get("registration").alias("registration"),
                root.get("paymentDate").alias("paymentDate"),
                root.get("eventName").alias("eventName"),
                root.get("eventDescription").alias("eventDescription"),
                root.get("value").alias("value")
        );

        List<Predicate> predicates = buildPredicates(filter, cb, root);
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        query.orderBy(
                cb.desc(root.get("competence")),
                cb.asc(root.get("registration")),
                cb.desc(root.get("value"))
        );

        List<Tuple> tuples = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = executeCountQuery(filter, cb);

        List<PayrollResponse> results = tuples.stream().map(t -> PayrollResponse.builder()
                .id(t.get("id", String.class))
                .competence(t.get("competence", Date.class))
                .filialHcm(t.get("filialHcm", Long.class))
                .registration(t.get("registration", String.class))
                .paymentDate(t.get("paymentDate", Date.class))
                .eventName(t.get("eventName", String.class))
                .eventDescription(t.get("eventDescription", String.class))
                .value(t.get("value", Double.class))
                .build()
        ).toList();

        return new PageImpl<>(results, pageable, total);
    }


    private List<Predicate> buildPredicates(FilterPayroll filter, CriteriaBuilder cb, Root<PayrollEntity> root) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter == null) {
            return predicates;
        }

        if (filter.getCompetenceStart() != null && filter.getCompetenceEnd() != null) {
            predicates.add(cb.between(root.get("competence"), filter.getCompetenceStart(), filter.getCompetenceEnd()));
        } else if (filter.getCompetenceStart() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("competence"), filter.getCompetenceStart()));
        } else if (filter.getCompetenceEnd() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("competence"), filter.getCompetenceEnd()));
        }

        if (filter.getRegistration() != null && !filter.getRegistration().isBlank()) {
            predicates.add(cb.equal(cb.lower(root.get("registration")), filter.getRegistration().toLowerCase()));
        }

        if (filter.getEventName() != null && !filter.getEventName().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("eventName")), "%" + filter.getEventName().toLowerCase() + "%"));
        }

        if (filter.getEventDescription() != null && !filter.getEventDescription().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("eventDescription")), "%" + filter.getEventDescription().toLowerCase() + "%"));
        }

        if (filter.getMinValue() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("value"), filter.getMinValue()));
        }

        if (filter.getMaxValue() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("value"), filter.getMaxValue()));
        }

        if (filter.getFilialHcm() != null && !filter.getFilialHcm().isEmpty()) {
            predicates.add(root.get("filialHcm").in(filter.getFilialHcm()));
        }

        return predicates;
    }

    private Long executeCountQuery(FilterPayroll filter, CriteriaBuilder cb) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<PayrollEntity> countRoot = countQuery.from(PayrollEntity.class);

        List<Predicate> countPredicates = buildPredicates(filter, cb, countRoot);

        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }

        countQuery.select(cb.count(countRoot));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    @Override
    public Page<String> findDistinctEventNames(String search, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<PayrollEntity> root = query.from(PayrollEntity.class);

        query.select(root.get("eventName")).distinct(true);

        if (search != null && !search.isBlank()) {
            query.where(cb.like(cb.lower(root.get("eventName")), "%" + search.toLowerCase() + "%"));
        }

        query.orderBy(cb.asc(root.get("eventName")));

        List<String> resultList = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = countDistinctEventNames(cb, search);

        return new PageImpl<>(resultList, pageable, total);
    }

    @Override
    public Page<String> findDistinctEventDescriptions(Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<PayrollEntity> root = query.from(PayrollEntity.class);

        query.select(root.get("eventDescription")).distinct(true);
        query.orderBy(cb.asc(root.get("eventDescription")));

        List<String> resultList = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = countDistinct(cb, "eventDescription");

        return new PageImpl<>(resultList, pageable, total);
    }

    private Long countDistinct(CriteriaBuilder cb, String field) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<PayrollEntity> countRoot = countQuery.from(PayrollEntity.class);

        countQuery.select(cb.countDistinct(countRoot.get(field)));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private Long countDistinctEventNames(CriteriaBuilder cb, String search) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<PayrollEntity> countRoot = countQuery.from(PayrollEntity.class);

        if (search != null && !search.isBlank()) {
            countQuery.where(cb.like(cb.lower(countRoot.get("eventName")), "%" + search.toLowerCase() + "%"));
        }

        countQuery.select(cb.countDistinct(countRoot.get("eventName")));
        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
