package com.indux.modules.purchase_occurrence.application.repository;

import com.indux.modules.purchase_occurrence.domain.dto.PurchaseOccurrenceFilter;
import com.indux.modules.purchase_occurrence.domain.entities.PurchaseOccurrence;
import com.indux.modules.purchase_occurrence.domain.repository.CustomPurchaseOccurrenceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public class PurchaseOccurrenceRepositoryImpl  implements CustomPurchaseOccurrenceRepository {
    private final MongoTemplate mongo;

    public PurchaseOccurrenceRepositoryImpl (MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public Page<PurchaseOccurrence> findByFilter(PurchaseOccurrenceFilter f, Pageable pageable) {
        List<Criteria> criteria = new ArrayList<>();

        // Status
        if (f.status() != null && !f.status().isEmpty()) {
            criteria.add(Criteria.where("status").in(f.status()));
        }

        // Situação
        if (f.situacoes() != null && !f.situacoes().isEmpty()) {
            criteria.add(Criteria.where("situacao").in(f.situacoes()));
        }

        // Etapas
        if (f.etapasAtuais() != null
                && !f.etapasAtuais().isEmpty()
                && (f.situacoes() == null || !f.situacoes().contains("FINALIZADO"))) {

            criteria.add(Criteria.where("etapa_atual").in(f.etapasAtuais()));
        }

        // Datas
        if (f.criadoDe() != null || f.criadoAte() != null) {
            Criteria dateCrit = Criteria.where("criado_em");
            if (f.criadoDe() != null) dateCrit = dateCrit.gte(Date.from(f.criadoDe()));
            if (f.criadoAte() != null) dateCrit = dateCrit.lte(Date.from(f.criadoAte()));
            criteria.add(dateCrit);
        }

        // ID
        if (f.id() != null) {
            try {
                Long codeId = Long.valueOf(f.id());
                criteria.add(Criteria.where("codigo").is(codeId));
            } catch (NumberFormatException e) {
                // Se não for um número válido, ignora o filtro
            }
        }

        // Filiais
        if (f.regionais() != null && !f.regionais().isEmpty() && !f.regionais().contains(0)) {
            criteria.add(Criteria.where("regionalId").in(f.regionais()));
        }

        // Contratos
        if (f.projetos() != null && !f.projetos().isEmpty() && !f.projetos().contains(0)) {
            criteria.add(Criteria.where("projectId").in(f.projetos()));
        }

        // Nome do comprador
        if (f.nomeComprador() != null) {
            criteria.add(Criteria.where("buyer.name").regex(f.nomeComprador(), "i"));
        }

        // Solicitante
        if (f.solicitante() != null) {
            // Tenta buscar por ID primeiro (mais eficiente)
            try {
                UUID solicitanteId = UUID.fromString(f.solicitante());
                criteria.add(Criteria.where("applicant.id").is(solicitanteId));
            } catch (IllegalArgumentException e) {
                // Se não for um UUID válido, busca por nome
                criteria.add(Criteria.where("applicant.nome").regex(f.solicitante(), "i"));
            }
        }

        // Causas
        if (f.causas() != null && !f.causas().isEmpty()) {
            criteria.add(Criteria.where("causes._id").in(
                    f.causas().stream().map(Long::valueOf).toList()
            ));
        }

        // Monta a query final
        Criteria combined = criteria.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(criteria.toArray(new Criteria[0]));

        Query countQuery = Query.query(combined);
        long total = mongo.count(countQuery, PurchaseOccurrence.class, "purchase_occurrence");

        Query pagedQuery = Query.query(combined).with(pageable);
        List<PurchaseOccurrence> results = mongo.find(pagedQuery, PurchaseOccurrence.class, "purchase_occurrence");

        return new PageImpl<>(results, pageable, total);
    }
}
