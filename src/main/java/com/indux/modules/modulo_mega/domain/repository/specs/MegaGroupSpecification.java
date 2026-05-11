package com.indux.modules.modulo_mega.domain.repository.specs;

import com.indux.modules.modulo_mega.application.dto.groups.GroupFilter;
import com.indux.modules.modulo_mega.domain.entities.jpa.MegaGroupCode;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MegaGroupSpecification {

    public static Specification<MegaGroupCode> filterBy(GroupFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            if (filter.active() != null) {
                predicates.add(cb.equal(root.get("status"), filter.active()));
            }

            if (filter.term() != null && !filter.term().isBlank()) {
                String term = filter.term().trim();

                if (term.matches("\\d+")) {
                    predicates.add(cb.equal(root.get("id"), Integer.valueOf(term)));
                } else {
                    predicates.add(
                            cb.like(
                                    cb.lower(root.get("name")),
                                    "%" + term.toLowerCase() + "%"
                            )
                    );
                }
            }

            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}