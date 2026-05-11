package com.indux.core.application.repository;

import com.indux.core.application.dto.cbo.FilterFuncao;
import com.indux.core.domain.model.cbo.FuncaoHCM;
import com.indux.core.domain.repository.cbo.FuncaoRepositoryCustom;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class FuncaoHCMRepositoryImpl implements FuncaoRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    public FuncaoHCMRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<FuncaoHCM> getWithFilterFuncao(FilterFuncao filter, Pageable pageable){
        List<Criteria> criteriaList = new ArrayList<>();

        if (filter.filialHCM() != null && !filter.filialHCM().isEmpty()) {
            criteriaList.add(Criteria.where("filial_hcm").in(filter.filialHCM()));
        }

        if (filter.idAuto() != null ) {
            criteriaList.add(Criteria.where("autoIncrementId").in(filter.idAuto()));
        }

        if (filter.codCBO() != null && !filter.codCBO().isEmpty()) {
            criteriaList.add(Criteria.where("cbo_id").in(filter.codCBO()));
        }

        if (filter.status() != null && !filter.status().isEmpty()) {
            List<String> likeStatuses = new ArrayList<>();
            List<String> exactStatuses = new ArrayList<>();

            for (String s : filter.status()) {
                if ("RH".equalsIgnoreCase(s) || "SGI".equalsIgnoreCase(s)) {
                    likeStatuses.add(s);
                } else {
                    exactStatuses.add(s);
                }
            }

            Document lastAction = new Document("$ifNull", List.of(
                    new Document("$arrayElemAt", List.of("$data_log.acao", -1)),
                    ""
            ));

            List<Document> orConditions = new ArrayList<>();

            if (!likeStatuses.isEmpty()) {
                orConditions.add(
                        new Document("$anyElementTrue",
                                new Document("$map", new Document()
                                        .append("input", likeStatuses)
                                        .append("as", "s")
                                        .append("in", new Document("$regexMatch", new Document()
                                                .append("input", lastAction)
                                                .append("regex", "$$s")
                                                .append("options", "i")
                                        ))
                                )
                        )
                );
            }

            if (!exactStatuses.isEmpty()) {
                orConditions.add(
                        new Document("$in", List.of(lastAction, exactStatuses))
                );
            }

            Document expr;
            if (orConditions.size() == 1) {
                expr = orConditions.get(0);
            } else {
                expr = new Document("$or", orConditions);
            }

            criteriaList.add(Criteria.where("$expr").is(expr));
        }

        if (filter.solicitante() != null && !filter.solicitante().isEmpty()) {
            criteriaList.add(Criteria.where("data_log.0.nome").in(filter.solicitante()));
        }

        if (filter.nome() != null && !filter.nome().isBlank()) {
            String escapedNome = filter.nome().replaceAll("([\\\\.\\[\\]{}()*+?^$|])", "\\\\$1");
            criteriaList.add(new Criteria().orOperator(
                    Criteria.where("nome_hcm").regex(escapedNome, "i"),
                    Criteria.where("nomeCBOFilho").regex(escapedNome, "i")
            ));
        }

        Criteria criteria;

        if (criteriaList.isEmpty()) {
            criteria = new Criteria();
        } else {
            criteria = new Criteria().andOperator(criteriaList.toArray(new Criteria[0]));
        }
        Query query = Query.query(criteria).with(pageable);

        query.with(Sort.by(Sort.Direction.DESC, "autoIncrementId"));

        Query countQuery = Query.query(criteria);
        long count = mongoTemplate.count(countQuery, FuncaoHCM.class);

        var result = mongoTemplate.find(query, FuncaoHCM.class);

        return new PageImpl<>(result, pageable, count);
    }
}
