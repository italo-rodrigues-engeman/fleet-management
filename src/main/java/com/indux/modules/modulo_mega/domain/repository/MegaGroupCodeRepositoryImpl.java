package com.indux.modules.modulo_mega.domain.repository;

import com.indux.modules.modulo_mega.application.dto.AutocompleteDTO;
import com.indux.modules.modulo_mega.application.dto.groups.GroupFilter;
import com.indux.modules.modulo_mega.domain.entities.jpa.MegaGroupCode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MegaGroupCodeRepositoryImpl implements MegaGroupCodeRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<AutocompleteDTO> autocomplete(String term, Pageable pageable) {
        String safeTerm = term == null ? "" : term.trim().toLowerCase();
        String likeTerm = "%" + safeTerm + "%";

        CriteriaBuilder cb = em.getCriteriaBuilder();

        CriteriaQuery<AutocompleteDTO> query = cb.createQuery(AutocompleteDTO.class);
        Root<MegaGroupCode> root = query.from(MegaGroupCode.class);

        Predicate predicate = buildPredicate(cb, root, likeTerm);

        query.select(cb.construct(
                        AutocompleteDTO.class,
                        root.get("id"),
                        root.get("name")
                ))
                .where(predicate)
                .distinct(true);

        applySort(cb, query, root, pageable);

        TypedQuery<AutocompleteDTO> typedQuery = em.createQuery(query);

        if (pageable.isPaged()) {
            typedQuery.setFirstResult((int) pageable.getOffset());
            typedQuery.setMaxResults(pageable.getPageSize());
        }

        List<AutocompleteDTO> content = typedQuery.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<MegaGroupCode> countRoot = countQuery.from(MegaGroupCode.class);

        Predicate countPredicate = buildPredicate(cb, countRoot, likeTerm);

        countQuery.select(cb.countDistinct(countRoot.get("id")))
                .where(countPredicate);

        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }

    private Predicate buildPredicate(CriteriaBuilder cb, Root<MegaGroupCode> root, String likeTerm) {
        Predicate active = cb.isTrue(root.get("status"));

        Expression<String> idAsText = cb.function(
                "TO_CHAR",
                String.class,
                root.get("id"),
                cb.literal("FM999999999999999")
        );

        Predicate byId = cb.like(cb.lower(idAsText), likeTerm);
        Predicate byName = cb.like(cb.lower(root.get("name")), likeTerm);

        return cb.and(active, cb.or(byId, byName));
    }

    private void applySort(
            CriteriaBuilder cb,
            CriteriaQuery<AutocompleteDTO> query,
            Root<MegaGroupCode> root,
            Pageable pageable
    ) {
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order s : pageable.getSort()) {
                String property = s.getProperty();
                if (!isAllowedSort(property)) continue;
                orders.add(s.isAscending() ? cb.asc(root.get(property)) : cb.desc(root.get(property)));
            }
            if (!orders.isEmpty()) {
                query.orderBy(orders);
                return;
            }
        }

        query.orderBy(cb.asc(root.get("id")));
    }

    private boolean isAllowedSort(String property) {
        return "id".equals(property) || "name".equals(property);
    }

}
