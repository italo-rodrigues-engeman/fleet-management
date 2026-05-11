package com.indux.modules.organization_chart.domain.repositories.jpa;

import com.indux.modules.organization_chart.domain.entities.jpa.MegaEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.ProjectEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrganizationMegaRepositoryImpl implements OrganizationMegaRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<MegaEntity> findAllSortedByApelido(Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<MegaEntity> countRoot = countQuery.from(MegaEntity.class);
        countQuery.select(cb.count(countRoot));
        countQuery.where(cb.equal(countRoot.get("type"), "Analítico"));

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        CriteriaQuery<MegaEntity> criteriaQuery = cb.createQuery(MegaEntity.class);
        Root<MegaEntity> root = criteriaQuery.from(MegaEntity.class);

        Predicate typePredicate = cb.equal(root.get("type"), "Analítico");
        criteriaQuery.where(typePredicate);

        Expression<String> replaced = cb.function(
                "REPLACE",
                String.class,
                root.get("cusStApelido"),
                cb.literal("."),
                cb.literal(""));

        Expression<Number> ordered = cb.function(
                "to_number",
                Number.class,
                replaced,
                cb.literal("999999"));

        criteriaQuery.orderBy(cb.asc(ordered));

        TypedQuery<MegaEntity> query = entityManager.createQuery(criteriaQuery);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<MegaEntity> results = query.getResultList();

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<MegaEntity> searchByApelidoOrDescricao(String apelido, String descricao, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<MegaEntity> countRoot = countQuery.from(MegaEntity.class);
        countQuery.select(cb.count(countRoot));

        Predicate typePredicateCount = cb.equal(countRoot.get("type"), "Analítico");

        Predicate apelidoPredicateCount = cb.like(
                cb.lower(countRoot.get("cusStApelido")),
                "%" + apelido.toLowerCase() + "%");

        Predicate descricaoPredicateCount = cb.like(
                cb.lower(countRoot.get("cusStDescricao")),
                "%" + descricao.toLowerCase() + "%");

        Predicate searchPredicateCount = cb.or(apelidoPredicateCount, descricaoPredicateCount);
        Predicate finalPredicateCount = cb.and(typePredicateCount, searchPredicateCount);

        countQuery.where(finalPredicateCount);
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        CriteriaQuery<MegaEntity> criteriaQuery = cb.createQuery(MegaEntity.class);
        Root<MegaEntity> root = criteriaQuery.from(MegaEntity.class);
        Predicate typePredicate = cb.equal(root.get("type"), "Analítico");

        Predicate apelidoPredicate = cb.like(
                cb.lower(root.get("cusStApelido")),
                "%" + apelido.toLowerCase() + "%");

        Predicate descricaoPredicate = cb.like(
                cb.lower(root.get("cusStDescricao")),
                "%" + descricao.toLowerCase() + "%");

        Predicate searchPredicate = cb.or(apelidoPredicate, descricaoPredicate);
        Predicate finalPredicate = cb.and(typePredicate, searchPredicate);

        criteriaQuery.where(finalPredicate);

        TypedQuery<MegaEntity> query = entityManager.createQuery(criteriaQuery);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<MegaEntity> results = query.getResultList();

        return new PageImpl<>(results, pageable, total);
    }

    @Override
    public Page<MegaEntity> findMegasWithoutProject(Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<MegaEntity> countRoot = countQuery.from(MegaEntity.class);
        countQuery.select(cb.count(countRoot));
        Subquery<Long> subquery = countQuery.subquery(Long.class);
        Root<ProjectEntity> projectRoot = subquery.from(ProjectEntity.class);
        subquery.select(projectRoot.get("mega").get("cusInReduzido"));
        subquery.where(cb.equal(projectRoot.get("mega").get("cusInReduzido"), countRoot.get("cusInReduzido")));

        Predicate notExistsPredicate = cb.not(cb.exists(subquery));
        Predicate typePredicate = cb.equal(countRoot.get("type"), "Analítico");

        countQuery.where(cb.and(notExistsPredicate, typePredicate));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        CriteriaQuery<MegaEntity> criteriaQuery = cb.createQuery(MegaEntity.class);
        Root<MegaEntity> root = criteriaQuery.from(MegaEntity.class);

        Subquery<Long> dataSubquery = criteriaQuery.subquery(Long.class);
        Root<ProjectEntity> dataProjectRoot = dataSubquery.from(ProjectEntity.class);
        dataSubquery.select(dataProjectRoot.get("mega").get("cusInReduzido"));
        dataSubquery.where(cb.equal(dataProjectRoot.get("mega").get("cusInReduzido"), root.get("cusInReduzido")));

        Predicate dataNotExistsPredicate = cb.not(cb.exists(dataSubquery));
        Predicate dataTypePredicate = cb.equal(root.get("type"), "Analítico");

        criteriaQuery.where(cb.and(dataNotExistsPredicate, dataTypePredicate));
        Expression<String> dataReplaced = cb.function(
                "REPLACE",
                String.class,
                root.get("cusStApelido"),
                cb.literal("."),
                cb.literal(""));

        Expression<Number> dataOrdered = cb.function(
                "to_number",
                Number.class,
                dataReplaced,
                cb.literal("999999"));

        criteriaQuery.orderBy(cb.asc(dataOrdered));

        TypedQuery<MegaEntity> query = entityManager.createQuery(criteriaQuery);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<MegaEntity> results = query.getResultList();

        return new PageImpl<>(results, pageable, total);
    }
}
