package com.indux.modules.employee_history.infra.repository;

import com.indux.modules.employee_history.application.dto.FilterHistory;
import com.indux.modules.employee_history.domain.entity.History;
import com.indux.modules.employee_history.domain.repository.HistoryRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Repository
public class HistoryRepositoryImpl implements HistoryRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<History> findFilterHistory(FilterHistory filter, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<History> query = cb.createQuery(History.class);
        Root<History> root = query.from(History.class);

        List<Predicate> predicates = buildPredicates(filter, cb, root);

        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        query.orderBy(
                cb.asc(cb.length(root.get("registration"))),
                cb.asc(root.get("registration")),
                cb.desc(root.get("competence"))
        );

        List<History> resultList = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = executeCountQuery(filter, cb);

        return new PageImpl<>(resultList, pageable, total);
    }

    private List<Predicate> buildPredicates(FilterHistory filter, CriteriaBuilder cb, Root<History> root) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter.getCompetence() != null) {
            // Calcula o primeiro dia do mês às 00:00:00
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(filter.getCompetence());
            startCal.set(Calendar.DAY_OF_MONTH, 1);
            startCal.set(Calendar.HOUR_OF_DAY, 0);
            startCal.set(Calendar.MINUTE, 0);
            startCal.set(Calendar.SECOND, 0);
            startCal.set(Calendar.MILLISECOND, 0);
            Date startDate = startCal.getTime();

            // Calcula o último dia do mês às 23:59:59
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(filter.getCompetence());
            endCal.set(Calendar.DAY_OF_MONTH, endCal.getActualMaximum(Calendar.DAY_OF_MONTH));
            endCal.set(Calendar.HOUR_OF_DAY, 23);
            endCal.set(Calendar.MINUTE, 59);
            endCal.set(Calendar.SECOND, 59);
            endCal.set(Calendar.MILLISECOND, 999);
            Date endDate = endCal.getTime();

            // Filtro BETWEEN para pegar todo o mês
            predicates.add(cb.between(root.get("competence"), startDate, endDate));
        }

        if (filter.getFilialId() != null && !filter.getFilialId().isEmpty()) {
            predicates.add(root.get("filialId").in(filter.getFilialId()));
        }

        if (filter.getEvent() != null && !filter.getEvent().isEmpty()) {
            List<Predicate> eventPredicates = filter.getEvent().stream()
                    .map(e -> cb.like(cb.lower(root.get("event")), "%" + e.toLowerCase() + "%"))
                    .toList();
            predicates.add(cb.or(eventPredicates.toArray(new Predicate[0])));
        }

        if (filter.getEventDescription() != null && !filter.getEventDescription().isEmpty()) {
            List<Predicate> descPredicates = filter.getEventDescription().stream()
                    .map(d -> cb.like(cb.lower(root.get("description")), "%" + d.toLowerCase() + "%"))
                    .toList();
            predicates.add(cb.or(descPredicates.toArray(new Predicate[0])));
        }

        if (filter.getRegistration() != null && !filter.getRegistration().isEmpty()) {
            List<String> lowerRegistrations = filter.getRegistration().stream()
                    .map(String::toLowerCase)
                    .toList();
            predicates.add(cb.lower(root.get("registration")).in(lowerRegistrations));
        }

        return predicates;
    }

    private Long executeCountQuery(FilterHistory filter, CriteriaBuilder cb) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<History> countRoot = countQuery.from(History.class);

        List<Predicate> countPredicates = buildPredicates(filter, cb, countRoot);

        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }

        countQuery.select(cb.count(countRoot));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    @Override
    public Page<String> findDistinctEvents(Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<History> root = query.from(History.class);

        query.select(root.get("event")).distinct(true);
        query.orderBy(cb.asc(root.get("event")));

        List<String> resultList = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = executeCountDistinctEvents(cb);

        return new PageImpl<>(resultList, pageable, total);
    }

    private Long executeCountDistinctEvents(CriteriaBuilder cb) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<History> countRoot = countQuery.from(History.class);

        countQuery.select(cb.countDistinct(countRoot.get("event")));
        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
