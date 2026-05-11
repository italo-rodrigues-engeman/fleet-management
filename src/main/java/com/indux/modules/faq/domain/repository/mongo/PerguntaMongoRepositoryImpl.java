package com.indux.modules.faq.domain.repository.mongo;

import com.indux.modules.faq.application.dto.PerguntaFiltrosDTO;
import com.indux.modules.faq.application.dto.PerguntaTemaFiltrosDTO;
import com.indux.modules.faq.domain.entities.mongo.PerguntaMongo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class PerguntaMongoRepositoryImpl implements PerguntaMongoRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public PerguntaMongoRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<PerguntaMongo> findWithFilters(Pageable pageable, PerguntaFiltrosDTO filtros) {
        Query query = new Query();
        Criteria criteria = new Criteria();

        // Filtro por tema
        if (filtros.getTemaId() != null) {
            criteria.and("temaId").is(filtros.getTemaId());
        }

        // Filtro por setor
        if (filtros.getSetorId() != null) {
            criteria.and("setorId").is(filtros.getSetorId());
        }

        // Filtro por regional
        if (filtros.getRegionalId() != null) {
            criteria.and("regionalId").is(filtros.getRegionalId());
        }

        // Filtro por contrato - buscar por rateio_id na lista de contratos
        if (filtros.getContratoId() != null) {
            criteria.and("contratos").in(filtros.getContratoId());
        }

        // Filtro por título (busca case-insensitive)
        if (filtros.hasSearchTerm()) {
            criteria.and("titulo").regex(filtros.getQ().trim(), "i");
        }

        // Filtro por categorias (busca case-insensitive)
        if (filtros.hasCategoriaFilter()) {
            List<Criteria> categoriaCriteriaList = filtros.getCategoria().stream()
                .map(cat -> Criteria.where("categoria").regex(cat, "i"))
                .toList();
            criteria.orOperator(categoriaCriteriaList.toArray(new Criteria[0]));
        }

        // Filtro por status (busca case-insensitive)
        if (filtros.hasStatusFilter()) {
            criteria.and("status").regex(filtros.getStatus(), "i");
        }

        // Filtro por público (busca na pergunta e nas respostas)
        if (filtros.hasPublicoFilter()) {
            Criteria publicoCriteria = new Criteria().orOperator(
                Criteria.where("publico").regex(filtros.getPublico(), "i"),
                Criteria.where("respostas.publico").regex(filtros.getPublico(), "i")
            );
            criteria.andOperator(publicoCriteria);
        }

        // Filtro por data
        if (filtros.hasDateFilter()) {
            if (filtros.getDataInicio() != null && filtros.getDataFim() != null) {
                LocalDateTime inicio = filtros.getDataInicio().atStartOfDay();
                LocalDateTime fim = filtros.getDataFim().atTime(23, 59, 59);
                criteria.and("dataCriacao").gte(inicio).lte(fim);
            } else if (filtros.getDataInicio() != null) {
                LocalDateTime inicio = filtros.getDataInicio().atStartOfDay();
                criteria.and("dataCriacao").gte(inicio);
            } else {
                LocalDateTime fim = filtros.getDataFim().atTime(23, 59, 59);
                criteria.and("dataCriacao").lte(fim);
            }
        }

        // Filtros pelos novos campos
        if (filtros.getDiretoriaId() != null) {
            criteria.and("diretoriaId").is(filtros.getDiretoriaId());
        }

        if (filtros.getSuperintendenciaId() != null) {
            criteria.and("superintendenciaId").is(filtros.getSuperintendenciaId());
        }

        if (filtros.getProjetoId() != null) {
            criteria.and("projetoId").is(filtros.getProjetoId());
        }

        // Filtro por filial HCM (pode ser uma lista de IDs)
        if (filtros.getFilialHcmId() != null && !filtros.getFilialHcmId().isEmpty()) {
            criteria.and("filialHcmId").in(filtros.getFilialHcmId());
        }

        // Aplicar critérios à query
        query.addCriteria(criteria);

        // Aplicar paginação
        query.with(pageable);

        // Executar query e contar total
        List<PerguntaMongo> content = mongoTemplate.find(query, PerguntaMongo.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), PerguntaMongo.class);

        // Retornar Page com conteúdo e total
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<PerguntaMongo> findWithFiltersByTema(Long temaId, Pageable pageable, PerguntaTemaFiltrosDTO filtros) {
        Query query = new Query();
        Criteria criteria = new Criteria();

        // Filtro obrigatório por tema
        criteria.and("temaId").is(temaId);

        // Filtro por regional
        if (filtros.getRegionalId() != null) {
            criteria.and("regionalId").is(filtros.getRegionalId());
        }

        // Filtro por contrato - buscar por rateio_id na lista de contratos
        if (filtros.getContratoId() != null) {
            criteria.and("contratos").in(filtros.getContratoId());
        }

        // Filtro por título (busca case-insensitive)
        if (filtros.hasSearchTerm()) {
            criteria.and("titulo").regex(filtros.getQ().trim(), "i");
        }

        // Filtro por público (busca na pergunta e nas respostas)
        if (filtros.hasPublicoFilter()) {
            Criteria publicoCriteria = new Criteria().orOperator(
                Criteria.where("publico").regex(filtros.getPublico(), "i"),
                Criteria.where("respostas.publico").regex(filtros.getPublico(), "i")
            );
            criteria.andOperator(publicoCriteria);
        }

        // Filtros pelos novos campos
        if (filtros.getDiretoriaId() != null) {
            criteria.and("diretoriaId").is(filtros.getDiretoriaId());
        }

        if (filtros.getSuperintendenciaId() != null) {
            criteria.and("superintendenciaId").is(filtros.getSuperintendenciaId());
        }

        if (filtros.getProjetoId() != null) {
            criteria.and("projetoId").is(filtros.getProjetoId());
        }

        // Filtro por filial HCM (pode ser uma lista de IDs)
        if (filtros.getFilialHcmId() != null && !filtros.getFilialHcmId().isEmpty()) {
            criteria.and("filialHcmId").in(filtros.getFilialHcmId());
        }

        // Aplicar critérios à query
        query.addCriteria(criteria);

        // Aplicar paginação
        query.with(pageable);

        // Executar query e contar total
        List<PerguntaMongo> content = mongoTemplate.find(query, PerguntaMongo.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), PerguntaMongo.class);

        // Retornar Page com conteúdo e total
        return new PageImpl<>(content, pageable, total);
    }
}