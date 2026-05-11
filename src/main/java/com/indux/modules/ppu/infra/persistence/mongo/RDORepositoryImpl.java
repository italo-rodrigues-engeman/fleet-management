package com.indux.modules.ppu.infra.persistence.mongo;

import com.indux.modules.ppu.application.dtos.requests.RDOFilter;
import com.indux.modules.ppu.application.projection.RDOGridProjection;
import com.indux.modules.ppu.application.projection.RDOGridProjectionImpl;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusDP;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepositoryCustom;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.CriteriaDefinition;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class RDORepositoryImpl implements RDORepositoryCustom {
    private final MongoTemplate mongoTemplate;

    public RDORepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    private Criteria buildCriteria(RDOFilter filter, Boolean isFromRH) {
        List<Criteria> criteria = new ArrayList<>();

        if (filter.ppuId() != null) {
            criteria.add(Criteria.where("ppuId").is(filter.ppuId()));
        }
        if (filter.plataforma() != null) {
            criteria.add(Criteria.where("platform").is(filter.plataforma()));
        }
        if (filter.idSequencial() != null) {
            criteria.add(Criteria.where("sequentialId").is(filter.idSequencial()));
        }
        if (filter.dataInicio() != null || filter.dataFim() != null) {
            Criteria dateCriteria = Criteria.where("date");
            if (filter.dataInicio() != null) {
                dateCriteria = dateCriteria.gte(filter.dataInicio());
            }
            if (filter.dataFim() != null) {
                dateCriteria = dateCriteria.lte(filter.dataFim());
            }
            criteria.add(dateCriteria);
        } else if (filter.data() != null) {
            criteria.add(Criteria.where("date").is(filter.data()));
        }
        if (filter.supervisor() != null) {
            criteria.add(Criteria.where("creatorRegistration").is(filter.supervisor()));
        }
        if (filter.supervisorNome() != null) {
            criteria.add(Criteria.where("creatorName").is(filter.supervisorNome()));
        }
        if (filter.cliente() != null) {
            criteria.add(Criteria.where("clientName").is(filter.cliente()));
        }
        if (filter.regional() != null) {
            criteria.add(Criteria.where("regionalNome").is(filter.regional()));
        }
        if (filter.competencia() != null) {
            criteria.add(Criteria.where("competence").is(filter.competencia()));
        }
        if (Boolean.TRUE.equals(isFromRH)) {
            criteria.add(defaultRHStatusCriteria());
        }
        if (filter.statusOP() != null) {
            criteria.add(Criteria.where("statusOP").is(filter.statusOP()));
        }
        if (filter.statusDP() != null) {
            criteria.add(Criteria.where("statusDP").is(filter.statusDP()));
        }
        if (filter.contractProjectName() != null && !filter.contractProjectName().isBlank()) {
            criteria.add(Criteria.where("contract.projectName")
                    .regex(filter.contractProjectName().trim(), "i"));
        }
        if (filter.colaborador() != null) {
            criteria.add(Criteria.where("services.name").is(filter.colaborador()));
        }
        if (filter.contratos() != null && !filter.contratos().isEmpty()) {
            criteria.add(Criteria.where("contract.id").in(filter.contratos()));
        }
        if (filter.projeto() != null && !filter.projeto().isEmpty()) {
            criteria.add(Criteria.where("projectId").in(filter.projeto()));
        }

        return criteria.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(criteria.toArray(new Criteria[0]));
    }

    @Override
    public Page<RDOGridProjection> filter(RDOFilter filter, Pageable pageable, Boolean isFromRH) {

        Criteria finalCriteria = buildCriteria(filter, isFromRH);
        Object sortOrderExpr = ordering(isFromRH);

        var addFields = Aggregation.addFields()
                .addFieldWithValue("sortOrder", sortOrderExpr)
                .build();

        var sort = Sort.by(Sort.Direction.ASC, "sortOrder").and(Sort.by(Sort.Direction.ASC, "date"));

        Query query = Query.query(finalCriteria).with(pageable).with(sort);
        Query countQuery = Query.query(finalCriteria);

        long total = mongoTemplate.count(countQuery, RDOEntity.class);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(
                        new CriteriaDefinition() {
                            public Document getCriteriaObject() {
                                return query.getQueryObject();
                            }

                            public String getKey() {
                                return null;
                            }
                        }),
                addFields,
                Aggregation.sort(sort),
                Aggregation.skip(pageable.getOffset()),
                Aggregation.limit(pageable.getPageSize()));

        String collection = mongoTemplate.getCollectionName(RDOEntity.class);

        List<RDOEntity> entities = mongoTemplate
                .aggregate(aggregation, collection, RDOEntity.class)
                .getMappedResults();

        List<RDOGridProjection> projections = entities.stream()
                .map(RDOGridProjectionImpl::fromEntity)
                .map(proj -> (RDOGridProjection) proj)
                .toList();

        return new PageImpl<>(projections, pageable, total);
    }

    @NotNull
    private static Object ordering(Boolean isFromRH) {
        Object sortOrderExpr;
        if (isFromRH) {
            sortOrderExpr = ConditionalOperators.switchCases(
                    ConditionalOperators.Switch.CaseOperator
                            .when(ComparisonOperators.valueOf("statusDP").equalToValue("PENDING")).then(0),
                    ConditionalOperators.Switch.CaseOperator
                            .when(ComparisonOperators.valueOf("statusDP").equalToValue("CORRECTION_OP")).then(1),
                    ConditionalOperators.Switch.CaseOperator
                            .when(ComparisonOperators.valueOf("statusDP").equalToValue("APPROVED")).then(2))
                    .defaultTo(3);
        } else {
            sortOrderExpr = ConditionalOperators.switchCases(
                    ConditionalOperators.Switch.CaseOperator
                            .when(ComparisonOperators.valueOf("statusOP").equalToValue("CORRECTION")).then(0),
                    ConditionalOperators.Switch.CaseOperator
                            .when(ComparisonOperators.valueOf("statusOP").equalToValue("PENDING")).then(1),
                    ConditionalOperators.Switch.CaseOperator
                            .when(ComparisonOperators.valueOf("statusOP").equalToValue("APPROVED")).then(2))
                    .defaultTo(3);
        }
        return sortOrderExpr;
    }

    @Override
    public Page<RDOEntity> findEntitiesByFilter(RDOFilter filter, Pageable pageable, Boolean isFromRH) {
        Criteria finalCriteria = buildCriteria(filter, isFromRH);

        Query query = Query.query(finalCriteria).with(pageable);
        Query countQuery = Query.query(finalCriteria);

        long total = mongoTemplate.count(countQuery, RDOEntity.class);
        List<RDOEntity> entities = mongoTemplate.find(query, RDOEntity.class);

        return new PageImpl<>(entities, pageable, total);
    }

    @Override
    public Integer countByFiltersOP(RDOStatusOP status, String platform, LocalDate start, LocalDate end,
            RDOFilter filter) {
        var criteria = new ArrayList<Criteria>();
        criteria.add(buildCriteria(filter, false));
        criteria.add(Criteria.where("statusOP").is(status));
        if (platform != null && !platform.isBlank())
            criteria.add(Criteria.where("platform").is(platform));
        if (start != null && end != null)
            criteria.add(Criteria.where("date").gte(start).lte(end));
        Query query = new Query(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        return Math.toIntExact(mongoTemplate.count(query, RDOEntity.class));
    }

    @Override
    public Integer countByFiltersRH(RDOStatusDP statusDP, List<RDOStatusOP> excludedStatusOP, String platform,
            LocalDate start, LocalDate end, RDOFilter filter) {
        var criteria = new ArrayList<Criteria>();
        criteria.add(buildCriteria(filter, true));
        criteria.add(Criteria.where("statusDP").is(statusDP));
        if (excludedStatusOP != null && !excludedStatusOP.isEmpty())
            criteria.add(Criteria.where("statusOP").nin(excludedStatusOP));
        if (platform != null && !platform.isBlank())
            criteria.add(Criteria.where("platform").is(platform));
        if (start != null && end != null)
            criteria.add(Criteria.where("date").gte(start).lte(end));
        Query query = new Query(new Criteria().andOperator(criteria.toArray(new Criteria[0])));
        return Math.toIntExact(mongoTemplate.count(query, RDOEntity.class));
    }

    private Criteria defaultRHStatusCriteria() {
        return Criteria.where("statusOP").in(
                RDOStatusOP.APPROVED,
                RDOStatusOP.CORRECTION,
                RDOStatusOP.BM);
    }

    @Override
    public Page<RDOGridProjection> findAllOrdered(Pageable pageable, Set<Integer> regionais, Set<Integer> projetos) {
        List<Criteria> criteriaList = new ArrayList<>();

        boolean filterByRegional = regionais != null && !regionais.isEmpty() && !regionais.contains(0);
        boolean filterByProject = projetos != null && !projetos.isEmpty() && !projetos.contains(0);

        if (filterByRegional) {
            criteriaList.add(Criteria.where("regionalId").in(regionais));
        }

        if (filterByProject) {
            criteriaList.add(Criteria.where("projectId").in(projetos));
        }

        Criteria criteria = new Criteria();
        if (!criteriaList.isEmpty()) {
            criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
        }

        MatchOperation match = criteriaList.isEmpty()
                ? null
                : Aggregation.match(criteria);

        var sortOrderExpr = ConditionalOperators.switchCases(
                ConditionalOperators.Switch.CaseOperator
                        .when(ComparisonOperators.valueOf("statusOP").equalToValue("CORRECTION")).then(0),
                ConditionalOperators.Switch.CaseOperator
                        .when(ComparisonOperators.valueOf("statusOP").equalToValue("PENDING")).then(1),
                ConditionalOperators.Switch.CaseOperator
                        .when(ComparisonOperators.valueOf("statusOP").equalToValue("APPROVED")).then(2))
                .defaultTo(3);

        var addFields = Aggregation.addFields()
                .addFieldWithValue("sortOrder", sortOrderExpr)
                .build();

        var sort = Aggregation.sort(
                Sort.by(Sort.Direction.ASC, "sortOrder")
                        .and(Sort.by(Sort.Direction.ASC, "date")));

        var skip = Aggregation.skip(pageable.getOffset());
        var limit = Aggregation.limit(pageable.getPageSize());

        var project = Aggregation.project(
                "date", "statusOP", "statusDP", "platform", "sequentialId",
                "regionalNome", "creatorName", "clientName", "contract", "competence")
                .and("_id").as("id");

        List<AggregationOperation> operations = new ArrayList<>();

        if (match != null) {
            operations.add(match);
        }

        operations.add(addFields);
        operations.add(sort);
        operations.add(skip);
        operations.add(limit);
        operations.add(project);

        var aggregation = Aggregation.newAggregation(operations);

        var results = mongoTemplate.aggregate(aggregation, "ppu_rdos", RDOGridDTO.class);

        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        var content = results.getMappedResults().stream()
                .map(d -> factory.createProjection(RDOGridProjection.class, d))
                .toList();

        Query countQuery = new Query();
        if (!criteriaList.isEmpty()) {
            countQuery.addCriteria(criteria);
        }

        long total = mongoTemplate.count(countQuery, "ppu_rdos");

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<RDOGridProjection> findAllByStatusOPInOrdered(
            List<RDOStatusOP> statusOPs,
            Pageable pageable,
            Set<Integer> regionais,
            Set<Integer> projetos) {
        List<Criteria> criteriaList = new ArrayList<>();

        criteriaList.add(Criteria.where("statusOP").in(statusOPs));

        boolean filterByRegional = regionais != null && !regionais.isEmpty() && !regionais.contains(0);
        boolean filterByProject = projetos != null && !projetos.isEmpty() && !projetos.contains(0);

        if (filterByRegional) {
            criteriaList.add(Criteria.where("regionalId").in(regionais));
        }

        if (filterByProject) {
            criteriaList.add(Criteria.where("projectId").in(projetos));
        }

        Criteria criteria = criteriaList.size() == 1
                ? criteriaList.get(0)
                : new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));

        var match = Aggregation.match(criteria);

        var sortOrderExpr = ConditionalOperators.switchCases(
                ConditionalOperators.Switch.CaseOperator
                        .when(ComparisonOperators.valueOf("statusDP").equalToValue("PENDING")).then(0),
                ConditionalOperators.Switch.CaseOperator
                        .when(ComparisonOperators.valueOf("statusDP").equalToValue("CORRECTION_OP")).then(1),
                ConditionalOperators.Switch.CaseOperator
                        .when(ComparisonOperators.valueOf("statusDP").equalToValue("APPROVED")).then(2))
                .defaultTo(3);

        var addFields = Aggregation.addFields()
                .addFieldWithValue("sortOrder", sortOrderExpr)
                .build();

        var sort = Aggregation.sort(
                Sort.by(Sort.Direction.ASC, "sortOrder")
                        .and(Sort.by(Sort.Direction.ASC, "date"))
                        .and(Sort.by(Sort.Direction.ASC, "sequentialId")));

        var skip = Aggregation.skip(pageable.getOffset());
        var limit = Aggregation.limit(pageable.getPageSize());

        var project = Aggregation.project(
                "date", "statusOP", "statusDP", "platform", "sequentialId",
                "regionalNome", "creatorName", "clientName", "contract", "competence")
                .and("_id").as("id");

        var aggregation = Aggregation.newAggregation(match, addFields, sort, skip, limit, project);

        var results = mongoTemplate.aggregate(aggregation, "ppu_rdos", RDOGridDTO.class);

        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        var content = results.getMappedResults().stream()
                .map(d -> factory.createProjection(RDOGridProjection.class, d))
                .toList();

        var totalQuery = new Query(criteria);
        long total = mongoTemplate.count(totalQuery, "ppu_rdos");

        return new PageImpl<>(content, pageable, total);
    }

    public record RDOGridDTO(
            String id,
            LocalDate date,
            Long sequentialId,
            String regionalNome,
            String platform,
            RDOStatusDP statusDP,
            RDOStatusOP statusOP,
            String clientName,
            String competence,
            Map<String, Object> contract,
            String creatorName) {
    }

    @Override
    public List<RDOEntity> findAllByPlatformInAndStatusOPAndDateBetween(
            List<String> platforms,
            RDOStatusOP status,
            LocalDate start,
            LocalDate end) {
        Criteria criteria = new Criteria();
        criteria.and("platform").in(platforms);
        criteria.and("statusOP").is(status);
        criteria.and("date")
                .gte(start)
                .lte(end.plusDays(1));

        Query query = new Query(criteria);
        return mongoTemplate.find(query, RDOEntity.class);
    }

    @Override
    public List<RDOEntity> findAllByPlatformAndStatusOPAndDateBetween(
            String platform,
            RDOStatusOP status,
            LocalDate start,
            LocalDate end) {
        Criteria criteria = Criteria.where("platform").is(platform)
                .and("statusOP").is(status)
                .and("date").gte(start).lte(end);

        Query query = new Query(criteria);
        return mongoTemplate.find(query, RDOEntity.class);
    }

    @Override
    public List<RDOEntity> findByPlatformAndDateRange(String platform, LocalDate startDate, LocalDate endDate) {
        Criteria criteria = new Criteria();
        criteria.and("platform").is(platform);
        criteria.and("date")
                .gte(startDate)
                .lte(endDate.plusDays(1));

        Query query = new Query(criteria);
        return mongoTemplate.find(query, RDOEntity.class);
    }

    @Override
    public List<RDOEntity> findAllByDateAndPpuID(LocalDate start, LocalDate end, String ppuId) {
        var criteria = new Criteria().andOperator(
                Criteria.where("ppuId").is(ppuId),
                Criteria.where("date").gte(start).lt(end.plusDays(1)));

        return mongoTemplate.find(Query.query(criteria), RDOEntity.class);
    }

    @Override
    public List<RDOEntity> findAllByPlatformAndDateAndPpuID(String platform, LocalDate start, LocalDate end,
            String ppuId) {
        var criteria = new Criteria().andOperator(
                Criteria.where("platform").is(platform),
                Criteria.where("ppuId").is(ppuId),
                Criteria.where("date").gte(start).lt(end.plusDays(1)));

        return mongoTemplate.find(Query.query(criteria), RDOEntity.class);
    }
}
