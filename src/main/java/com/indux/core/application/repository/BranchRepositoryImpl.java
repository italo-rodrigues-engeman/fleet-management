package com.indux.core.application.repository;

import com.indux.core.domain.model.employee.Filial;
import com.indux.core.domain.repository.generic.BranchRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class BranchRepositoryImpl implements BranchRepositoryCustom {

    @PersistenceContext
    private final EntityManager em;

    public BranchRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Page<Filial> findWithSearch(String search, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<Filial> cq = cb.createQuery(Filial.class);
        Root<Filial> root = cq.from(Filial.class);

        List<Predicate> predicates = buildPredicates(cb, root, search);

        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(Predicate[]::new));
        }

        // Ordenar por branchId ASC
        cq.orderBy(cb.asc(root.get("branchId")));

        List<Filial> resultList = em.createQuery(cq)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        // Count query
        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<Filial> countRoot = countCq.from(Filial.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, search);
        countCq.select(cb.count(countRoot));
        if (!countPredicates.isEmpty()) {
            countCq.where(countPredicates.toArray(Predicate[]::new));
        }
        Long total = em.createQuery(countCq).getSingleResult();

        return new PageImpl<>(resultList, pageable, total);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Filial> root, String search) {
        List<Predicate> predicates = new ArrayList<>();

        if (search != null && !search.isBlank()) {
            String likePattern = "%" + search.toLowerCase() + "%";

            // Busca textual em branchName e cnpj (LIKE case-insensitive)
            Predicate branchNamePred = cb.like(cb.lower(root.get("branchName")), likePattern);
            Predicate cnpjPred = cb.like(cb.lower(root.get("cnpj")), likePattern);

            // Busca numérica em branchId — converte o termo para Long se possível
            Predicate branchIdPred;
            try {
                Long searchId = Long.parseLong(search.trim());
                branchIdPred = cb.equal(root.get("branchId"), searchId);
            } catch (NumberFormatException e) {
                branchIdPred = cb.disjunction(); // nunca bate se não for número
            }

            predicates.add(cb.or(branchIdPred, cnpjPred, branchNamePred));
        }

        return predicates;
    }
}
