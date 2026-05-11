package com.indux.modules.employee_history.infra.repository;

import com.indux.modules.employee_history.application.dto.CompetenceFilialProjection;
import com.indux.modules.employee_history.application.dto.FilterDaysWorked;
import com.indux.modules.employee_history.domain.entity.DaysWorked;
import com.indux.modules.employee_history.domain.repository.DaysWorkedRepositoryCustom;
import com.indux.modules.organization_chart.domain.entities.jpa.ContractEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.MegaEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.OrganizationEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.ProjectEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DaysWorkedRepositoryImpl implements DaysWorkedRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<DaysWorked> findFilterDaysWorked(FilterDaysWorked filter, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DaysWorked> query = cb.createQuery(DaysWorked.class);
        Root<DaysWorked> root = query.from(DaysWorked.class);

        List<Predicate> predicates = buildPredicates(filter, cb, root);
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        query.orderBy(cb.desc(root.get("competence")));

        List<DaysWorked> resultList = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long total = executeCountQuery(filter, cb);
        return new PageImpl<>(resultList, pageable, total);
    }

    @Override
    public Page<LocalDate> findDistinctCompetences(Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LocalDate> query = cb.createQuery(LocalDate.class);
        Root<DaysWorked> root = query.from(DaysWorked.class);

        query.select(root.get("competence")).distinct(true);
        query.orderBy(cb.desc(root.get("competence")));

        List<LocalDate> resultList = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<DaysWorked> countRoot = countQuery.from(DaysWorked.class);
        countQuery.select(cb.countDistinct(countRoot.get("competence")));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }

    @Override
    public Page<CompetenceFilialProjection> findDistinctCompetencesByFilial(FilterDaysWorked filter,
            Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Tuple> query = cb.createTupleQuery();

        Root<DaysWorked>      dw     = query.from(DaysWorked.class);
        Root<FilialHcmEntity> filial = query.from(FilialHcmEntity.class);


        Join<FilialHcmEntity, ProjectEntity> project = filial.join("project", JoinType.INNER);
        Join<ProjectEntity,   MegaEntity>    mega    = project.join("mega",   JoinType.LEFT);
        Join<ProjectEntity,   OrganizationEntity> subordinate = project.join("subordinate", JoinType.LEFT);
        Join<ProjectEntity,   ContractEntity> contract = project.join("contract", JoinType.LEFT);

        Expression<LocalDate> trunc = cb.function("date_trunc", LocalDate.class,
                cb.literal("month"), dw.get("competence"));

        List<Predicate> predicates = new ArrayList<>(buildPredicates(filter, cb, dw));

        predicates.add(cb.equal(
                dw.<Long>get("filialId"),
                filial.<Integer>get("filialId").as(Long.class)));

        if (filter != null && filter.getProjectType() != null && !filter.getProjectType().isEmpty()) {
            predicates.add(cb.equal(project.get("type"), filter.getProjectType()));
        }

        query.where(cb.and(predicates.toArray(new Predicate[0])));


        query.multiselect(
                trunc.alias("competence"),
                project.<Long>get("id").alias("projectId"));

        query.groupBy(
                trunc,
                project.get("id"),
                mega.get("cusInReduzido"),
                mega.get("cusStDescricao"),
                project.get("type"),
                project.get("ativo"),
                subordinate.get("position"),
                contract.get("name"));

        Expression<Integer> competenceNull = cb.<Integer>selectCase()
                .when(cb.isNull(trunc), 1).otherwise(0);
        Expression<Integer> megaNull = cb.<Integer>selectCase()
                .when(cb.isNull(mega.get("cusInReduzido")), 1).otherwise(0);

        if (filter != null && filter.getOrderBy() != null && !filter.getOrderBy().isEmpty()) {
            boolean isAsc = filter.getDirection() != null ? filter.getDirection() : true;

            if ("hierarquia".equals(filter.getOrderBy())) {
                if (isAsc) {
                    query.orderBy(
                            cb.asc(subordinate.get("position")),
                            cb.asc(contract.get("name")));
                } else {
                    query.orderBy(
                            cb.desc(subordinate.get("position")),
                            cb.desc(contract.get("name")));
                }
            } else {
                Expression<?> orderExpression = switch (filter.getOrderBy()) {
                    case "competencia" -> trunc;
                    case "megaReduzido" -> mega.get("cusInReduzido");
                    case "nome" -> mega.get("cusStDescricao");
                    case "tipo" -> project.get("type");
                    case "ativo" -> project.get("ativo");
                    case "filialHCM" -> cb.max(filial.get("filialId"));
                    default -> project.get("id");
                };
                if (isAsc) {
                    query.orderBy(cb.asc(orderExpression));
                } else {
                    query.orderBy(cb.desc(orderExpression));
                }
            }
        } else {
            query.orderBy(
                    cb.asc(competenceNull),
                    cb.desc(trunc),
                    cb.asc(megaNull),
                    cb.asc(mega.<Integer>get("cusInReduzido")));
        }

        long total = entityManager.createQuery(query).getResultList().size();

        List<Tuple> tuples = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        List<CompetenceFilialProjection> projections = tuples.stream()
                .map(t -> new CompetenceFilialProjection(
                        t.get("competence", LocalDate.class),
                        t.get("projectId",  Long.class)))
                .toList();

        return new PageImpl<>(projections, pageable, total);
    }

    private List<Predicate> buildPredicates(FilterDaysWorked filter, CriteriaBuilder cb, Root<DaysWorked> root) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter != null && filter.getCompetence() != null) {
            LocalDate refDate = filter.getCompetence().toInstant()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();
            YearMonth yearMonth = YearMonth.from(refDate);
            predicates.add(cb.between(root.get("competence"), yearMonth.atDay(1), yearMonth.atEndOfMonth()));
        }

        if (filter == null || (filter.getCompetence() == null && filter.getIsClose())) {
            LocalDate startOfLastMonth = YearMonth.now().minusMonths(1).atDay(1);
            predicates.add(cb.lessThanOrEqualTo(root.get("competence"), startOfLastMonth));
        }

        if (filter != null && filter.getFilialId() != null && !filter.getFilialId().isEmpty()) {
            predicates.add(root.get("filialId").in(filter.getFilialId()));
        }

        if (filter != null && filter.getRegistration() != null && !filter.getRegistration().isEmpty()) {
            predicates.add(cb.equal(cb.lower(root.get("registration")), filter.getRegistration().toLowerCase()));
        }

        if (filter != null && filter.getSituation() != null && !filter.getSituation().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("situation")), "%" + filter.getSituation().toLowerCase() + "%"));
        }

        if (filter != null && filter.getMovimentation() != null && !filter.getMovimentation().isEmpty()) {
            predicates.add(
                    cb.like(cb.lower(root.get("movimentation")), "%" + filter.getMovimentation().toLowerCase() + "%"));
        }

        return predicates;
    }

    private Long executeCountQuery(FilterDaysWorked filter, CriteriaBuilder cb) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<DaysWorked> countRoot = countQuery.from(DaysWorked.class);

        List<Predicate> countPredicates = buildPredicates(filter, cb, countRoot);
        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }
        countQuery.select(cb.count(countRoot));
        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
