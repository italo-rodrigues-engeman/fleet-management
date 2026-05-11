package com.indux.modules.organization_chart.application.repository;

import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import com.indux.modules.organization_chart.domain.entities.jpa.ProjectEntity;
import com.indux.modules.organization_chart.application.dtos.ProjectFilter;
import com.indux.modules.organization_chart.domain.repositories.jpa.OrganizationProjectRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class OrganizationProjectRepositoryImpl implements OrganizationProjectRepositoryCustom {

    private final EntityManager em;

    public OrganizationProjectRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public Page<ProjectEntity> getWithFilter(ProjectFilter filter, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // Count Query para obter o total real de linhas sem carregar todos à memória
        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<ProjectEntity> countRoot = countCq.from(ProjectEntity.class);
        countCq.select(cb.countDistinct(countRoot));
        
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, filter);
        if (!countPredicates.isEmpty()) {
            countCq.where(countPredicates.toArray(new Predicate[0]));
        }
        Long totalRows = em.createQuery(countCq).getSingleResult();

        // Data Query
        CriteriaQuery<ProjectEntity> cq = cb.createQuery(ProjectEntity.class);
        Root<ProjectEntity> project = cq.from(ProjectEntity.class);

        List<Predicate> predicates = buildPredicates(cb, project, filter);

        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(Predicate[]::new));
        }

        cq.orderBy(cb.asc(project.get("mega").get("cusInReduzido")));

        TypedQuery<ProjectEntity> query = em.createQuery(cq);
        
        // Aplicação correta de paginação na query original
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<ProjectEntity> resultList = query.getResultList();

        return new PageImpl<>(resultList, pageable, totalRows);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<ProjectEntity> project,
            ProjectFilter filter) {
        List<Predicate> predicates = new ArrayList<>();

        // Filtros do ProjectFilter
        if (filter != null) {
            if (filter.getAtivo() != null) {
                predicates.add(cb.equal(project.get("ativo"), filter.getAtivo()));
            }

            if (filter.getMega() != null) {
                predicates.add(cb.equal(project.get("mega").get("cusInReduzido"), filter.getMega()));
            }

            if (filter.getHcm() != null) {
                predicates.add(cb.equal(project.get("hcm").get("ccId"), filter.getHcm()));
            }

            if (filter.getSubordinate() != null) {
                predicates.add(cb.equal(project.get("subordinate").get("id"), filter.getSubordinate()));
            }

            if (filter.getContracts() != null && !filter.getContracts().isEmpty()) {
                predicates.add(project.get("contract").get("id").in(filter.getContracts()));
            }

            if (filter.getFilial() != null && !filter.getFilial().isEmpty()) {
                Join<ProjectEntity, FilialHcmEntity> filialJoin = project.join("filial");
                predicates.add(filialJoin.get("filialId").in(filter.getFilial()));
            }
        }


        if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
            String likePattern = "%" + filter.getSearch().toLowerCase() + "%";

            Predicate apelidoPred = cb.like(
                    cb.lower(project.get("mega").get("cusStApelido")), likePattern);
            Predicate descricaoPred = cb.like(
                    cb.lower(project.get("mega").get("cusStDescricao")), likePattern);

            Predicate reduzidoPred;
            try {
                Integer searchInt = Integer.parseInt(filter.getSearch().trim());
                reduzidoPred = cb.equal(project.get("mega").get("cusInReduzido"), searchInt);
            } catch (NumberFormatException e) {
                reduzidoPred = cb.disjunction();
            }

            predicates.add(cb.or(reduzidoPred, apelidoPred, descricaoPred));
        }

        return predicates;
    }
}
