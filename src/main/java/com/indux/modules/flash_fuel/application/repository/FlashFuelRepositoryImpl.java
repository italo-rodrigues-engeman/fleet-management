package com.indux.modules.flash_fuel.application.repository;

import com.indux.modules.flash_fuel.domain.dtos.FlashFuelFilter;
import com.indux.modules.flash_fuel.domain.entities.FlashFuel;
import com.indux.modules.flash_fuel.domain.repository.CustomFlashFuelRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class FlashFuelRepositoryImpl implements CustomFlashFuelRepository {
    private final MongoTemplate mongo;

    public FlashFuelRepositoryImpl(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public Page<FlashFuel> findByFilter(FlashFuelFilter filter, Pageable pageable) {
        List<Criteria> criteria = new ArrayList<>();

        // Status
        if (filter.status() != null && !filter.status().isEmpty()) {
            criteria.add(Criteria.where("status").in(filter.status()));
        }

        // Situações
        if (filter.situacoes() != null && !filter.situacoes().isEmpty()) {
            criteria.add(Criteria.where("situacao").in(filter.situacoes()));
        }

        // Etapas atuais
        if (filter.etapasAtuais() != null
                && !filter.etapasAtuais().isEmpty()
                && (filter.situacoes() == null || !filter.situacoes().contains("FINALIZADO"))) {
            criteria.add(Criteria.where("etapa_atual").in(filter.etapasAtuais()));
        }

        if (filter.usuarioLogadoID() != null) {
            List<String> values = List.of("Finalização", "Rejeição em grupo", "Aprovado");
            criteria.add(Criteria.where("etapa_log").elemMatch(
                    Criteria.where("usuario").is(UUID.fromString(filter.usuarioLogadoID()))
                            .and("name").in(values)
            ));
        }

        // Filtro de data específica (campo correto: dataSolicitacao)
        if (filter.data() != null) {
            criteria.add(Criteria.where("dataSolicitacao").is(filter.data().toString()));
        }

        // Filtro de período de data (campo correto: dataSolicitacao)
        if (filter.dataInicio() != null || filter.dataFim() != null) {
            Criteria dateCrit = Criteria.where("dataSolicitacao");
            if (filter.dataInicio() != null) dateCrit = dateCrit.gte(filter.dataInicio().toString());
            if (filter.dataFim() != null) dateCrit = dateCrit.lte(filter.dataFim().toString());
            criteria.add(dateCrit);
        }

        // Período de criação
        if (filter.criadoDe() != null || filter.criadoAte() != null) {
            Criteria dateCrit = Criteria.where("criado_em");
            if (filter.criadoDe() != null) {
                // Converte LocalDateTime para Date para compatibilidade com MongoDB
                java.util.Date criadoDeDate = java.sql.Timestamp.valueOf(filter.criadoDe());
                dateCrit = dateCrit.gte(criadoDeDate);
            }
            if (filter.criadoAte() != null) {
                // Converte LocalDateTime para Date para compatibilidade com MongoDB
                java.util.Date criadoAteDate = java.sql.Timestamp.valueOf(filter.criadoAte());
                dateCrit = dateCrit.lte(criadoAteDate);
            }
            criteria.add(dateCrit);
        }

        // ID da ocorrência
        if (filter.id() != null) {
            criteria.add(Criteria.where("codigo").is(filter.id()));
        }

        // Filiais
        if (filter.regionaisId() != null && !filter.regionaisId().isEmpty() && !filter.regionaisId().contains(0)) {
            criteria.add(Criteria.where("regionalId").in(filter.regionaisId()));
        }

        if (filter.regionais() != null && !filter.regionais().isEmpty()) {
            criteria.add(Criteria.where("filialNome").in(filter.regionais()));
        }

        // Contratos
        if (filter.projetcs() != null && !filter.projetcs().isEmpty() && !filter.projetcs().contains(0)) {
            criteria.add(Criteria.where("projectId").in(filter.projetcs()));
        }

        // Nome do Requerente
        if (filter.nomeRequerente() != null) {
            criteria.add(Criteria.where("claimantName").regex(filter.nomeRequerente(), "i"));
        }

        // Nome do Solicitante (applicant)
        if (filter.nomeSolicitante() != null) {
            criteria.add(Criteria.where("applicant.nome").regex(filter.nomeSolicitante(), "i"));
        }

        // Matrícula do Requerente
        if (filter.matriculaRequerente() != null) {
            criteria.add(Criteria.where("registration").regex(filter.matriculaRequerente(), "i"));
        }

        // Valor
        if (filter.valor() != null) {
            try {
                BigDecimal val = new BigDecimal(filter.valor());
                criteria.add(Criteria.where("value").is(val));
            } catch (NumberFormatException ignored) {
            }
        }

        // Aviso de Recebimento
        if (filter.avisoRecebimento() != null) {
            criteria.add(Criteria.where("receiptNotice").regex(filter.avisoRecebimento(), "i"));
        }

        // Combina critérios
        Criteria combined = criteria.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(criteria.toArray(new Criteria[0]));

        Query countQuery = Query.query(combined);
        long total = mongo.count(countQuery, FlashFuel.class, "flash_combustivel");

        Query pagedQuery = Query.query(combined).with(pageable);
        List<FlashFuel> results = mongo.find(pagedQuery, FlashFuel.class, "flash_combustivel");

        return new PageImpl<>(results, pageable, total);
    }

}
