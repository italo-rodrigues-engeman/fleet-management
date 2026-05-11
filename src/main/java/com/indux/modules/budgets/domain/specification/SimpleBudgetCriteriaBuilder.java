package com.indux.modules.budgets.domain.specification;

import com.indux.modules.budgets.application.dto.SimpleBudgetFilter;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SimpleBudgetCriteriaBuilder {

    private final List<Criteria> criteriaList = new ArrayList<>();

    public SimpleBudgetCriteriaBuilder withRegex(String field, String value) {
        if (value != null && !value.isEmpty()) {
            criteriaList.add(Criteria.where(field).regex(value, "i"));
        }
        return this;
    }

    public SimpleBudgetCriteriaBuilder withEquals(String field, Object value) {
        if (value != null) {
            criteriaList.add(Criteria.where(field).is(value));
        }
        return this;
    }

    public Optional<Criteria> build() {
        if (criteriaList.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
    }

    public static Optional<Criteria> fromFilter(SimpleBudgetFilter filter) {
        return new SimpleBudgetCriteriaBuilder()
                .withRegex("acOs", filter.getAcOs())
                .withRegex("setor", filter.getSetor())
                .withEquals("oportunidade", filter.getOportunidade())
                .withRegex("nomeOportunidade", filter.getNomeOportunidade())
                .withRegex("orcamentista", filter.getOrcamentista())
                .withEquals("clienteId", filter.getClienteId())
                .withIn("status", filter.getStatus())
                .withDateRange(filter.getStartDate(), filter.getEndDate())
                .build();
    }

    public SimpleBudgetCriteriaBuilder withIn(String field, java.util.Collection<?> values) {
        if (values != null && !values.isEmpty()) {
            criteriaList.add(Criteria.where(field).in(values));
        }
        return this;
    }

    public SimpleBudgetCriteriaBuilder withDateRange(java.time.LocalDate start, java.time.LocalDate end) {
        if (start != null || end != null) {
            Criteria dateCriteria = Criteria.where("criado_em");
            if (start != null) {
                dateCriteria.gte(java.util.Date.from(start.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()));
            }
            if (end != null) {
                dateCriteria.lte(java.util.Date.from(end.atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault()).toInstant()));
            }

            criteriaList.add(Criteria.where("stepLog").elemMatch(
                    Criteria.where("name").is("CRIACAO_ORCAMENTO").andOperator(dateCriteria)
            ));
        }
        return this;
    }
}
