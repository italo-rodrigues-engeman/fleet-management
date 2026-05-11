package com.indux.core.application.repository;

import com.indux.core.application.dto.cbo.FilterCBO;
import com.indux.core.domain.model.employee.Cargo;
import com.indux.core.domain.repository.generic.CargoRepositoryCustom;
import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class CargoRepositoryImpl implements CargoRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Cargo> findFilterCargo(FilterCBO filter, Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<Cargo> query = cb.createQuery(Cargo.class);
        Root<Cargo> root = query.from(Cargo.class);

        List<Predicate> predicates = buildPredicates(cb, root, filter);

        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        query.orderBy(cb.asc(root.get("idHcm")));

        List<Cargo> resultList = entityManager.createQuery(query)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Cargo> countRoot = countQuery.from(Cargo.class);

        List<Predicate> countPredicates = buildPredicates(cb, countRoot, filter);

        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }
        countQuery.select(cb.count(countRoot));
        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Cargo> root, FilterCBO filter) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter == null)
            return predicates;

        if (filter.getCodCBO() != null && !filter.getCodCBO().isEmpty()) {
            predicates.add(root.get("codeCbo").in(filter.getCodCBO()));
        }

        if (filter.getNomeCBO() != null && !filter.getNomeCBO().isEmpty()) {
            List<Predicate> nomePredicates = new ArrayList<>();
            for (String nome : filter.getNomeCBO()) {
                String pattern = "%" + nome.toLowerCase() + "%";
                nomePredicates.add(cb.or(
                        cb.like(cb.lower(root.get("nameTitle")), pattern),
                        cb.like(cb.lower(root.get("nameTitleCbo")), pattern)));
            }
            predicates.add(cb.or(nomePredicates.toArray(new Predicate[0])));
        }

        if (filter.getIdHCM() != null && !filter.getIdHCM().isEmpty()) {
            predicates.add(root.get("idHcm").in(filter.getIdHCM()));
        }

        if (filter.getFilialIdHcm() != null && !filter.getFilialIdHcm().isEmpty()) {
            Join<Cargo, FilialHcmEntity> filialJoin = root.join("filial", JoinType.INNER);
            predicates.add(filialJoin.get("filialId").in(filter.getFilialIdHcm()));
        }

        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            String term = filter.getSearch().trim();
            String pattern = "%" + term.toLowerCase() + "%";

            Predicate nameTitlePred = cb.like(cb.lower(root.get("nameTitle")), pattern);
            Predicate nameTitleCboPred = cb.like(cb.lower(root.get("nameTitleCbo")), pattern);

            Predicate codesCboPred = cb.like(cb.lower(root.get("codeCbo").as(String.class)), pattern);

            Predicate idHcmPred = cb.like(cb.lower(root.get("idHcm").as(String.class)), pattern);

            predicates.add(cb.or(nameTitlePred, nameTitleCboPred, codesCboPred, idHcmPred));
        }

        return predicates;
    }
}
