package com.indux.modules.modulo_mega.domain.repository.mongo;

import com.indux.modules.modulo_mega.domain.entities.mongo.ItemSolicitationEntity;
import com.indux.modules.modulo_mega.domain.enums.ItemSolicitationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ItemSolicitationRepositoryImpl implements ItemSolicitationRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<ItemSolicitationEntity> findAllWithFilters(LocalDate dataInicial, LocalDate dataFinal, ItemSolicitationStatus status,
                                                           String createdBy, String mainName, String description, Pageable pageable) {

        Criteria criteria = new Criteria();

        // Filtro por data de criação
        if (dataInicial != null && dataFinal != null) {
            LocalDateTime inicio = dataInicial.atStartOfDay();
            LocalDateTime fim = dataFinal.atTime(23, 59, 59);
            criteria.and("creationDate").gte(inicio).lte(fim);
        } else if (dataInicial != null) {
            LocalDateTime inicio = dataInicial.atStartOfDay();
            LocalDateTime fim = dataInicial.atTime(23, 59, 59);
            criteria.and("creationDate").gte(inicio).lte(fim);
        } else if (dataFinal != null) {
            LocalDateTime fim = dataFinal.atTime(23, 59, 59);
            criteria.and("creationDate").lte(fim);
        }

        // Filtro por status
        if (status != null) {
            criteria.and("status").is(status);
        }

        // Filtro por solicitante (createdBy)
        if (createdBy != null && !createdBy.isBlank()) {
            criteria.and("createdBy").is(createdBy);
        }

        // NOVO: Filtro por Nome (busca parcial e case-insensitive)
        if (mainName != null && !mainName.isBlank()) {
            // O "i" significa ignorar Case (Case-Insensitive)
            criteria.and("mainName").regex(mainName, "i");
        }

        // NOVO: Filtro por Descrição (busca parcial e case-insensitive)
        if (description != null && !description.isBlank()) {
            criteria.and("description").regex(description, "i");
        }

        Query query = new Query(criteria);

        // Aplicar paginação
        query.with(pageable);

        // Executar query e contar total
        List<ItemSolicitationEntity> content = mongoTemplate.find(query, ItemSolicitationEntity.class);
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), ItemSolicitationEntity.class);

        // Retornar Page com conteúdo e total
        return new PageImpl<>(content, pageable, total);
    }
}
