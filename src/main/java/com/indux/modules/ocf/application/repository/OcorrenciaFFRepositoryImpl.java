package com.indux.modules.ocf.application.repository;

import com.indux.modules.ocf.application.dto.OccurrenceFilter;
import com.indux.modules.ocf.domain.model.OcorrenciaFF;
import com.indux.modules.ocf.domain.repository.CustomOccurrenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Repository
public class OcorrenciaFFRepositoryImpl implements CustomOccurrenceRepository {
    private static final Logger logger = LoggerFactory.getLogger(OcorrenciaFFRepositoryImpl.class);
    private final MongoTemplate mongo;

    public OcorrenciaFFRepositoryImpl(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public Page<OcorrenciaFF> findByFilter(OccurrenceFilter f, Pageable pageable) {
        List<Criteria> criteria = new ArrayList<>();

        if (f.statuses() != null && !f.statuses().isEmpty()) {
            criteria.add(Criteria.where("status").in(f.statuses()));
        }

        if (f.currentSteps() != null
                && !f.currentSteps().isEmpty()
                && (f.situations() == null || !f.situations().contains("FINALIZADO"))) {

            criteria.add(Criteria.where("etapa_atual").in(f.currentSteps()));

        }

        if (f.createdFrom() != null || f.createdTo() != null) {
            Criteria dateCrit = Criteria.where("criado_em");
            if (f.createdFrom() != null) {
                // Converter LocalDateTime para Date
                Date dateFrom = Date.from(f.createdFrom().atZone(ZoneId.systemDefault()).toInstant());
                dateCrit = dateCrit.gte(dateFrom);
            }
            if (f.createdTo() != null) {
                // Converter LocalDateTime para Date
                Date dateTo = Date.from(f.createdTo().atZone(ZoneId.systemDefault()).toInstant());
                dateCrit = dateCrit.lte(dateTo);
            }
            criteria.add(dateCrit);
        }

        if (f.id() != null) {
            criteria.add(Criteria.where("codigo").is(f.id()));
        }

        if (f.codeId() != null) {
            criteria.add(Criteria.where("codeID").is(f.codeId()));
        }

        if (f.origin() != null) {
            criteria.add(Criteria.where("origem").is(f.origin()));
        }

        if (f.type() != null) {
            criteria.add(Criteria.where("tipo").is(f.type()));
        }

        if (f.tipoFluxo() != null && !f.tipoFluxo().isBlank()) {
            criteria.add(Criteria.where("tipoFluxo").is(f.tipoFluxo()));
        }

        if (f.competencias() != null && !f.competencias().isEmpty()) {
            criteria.add(Criteria.where("competencia").in(f.competencias()));
        }

        if (f.regionaisIds() != null && !f.regionaisIds().isEmpty() && !f.regionaisIds().contains(0)) {
            criteria.add(Criteria.where("regionalId").in(f.regionaisIds()));
        }

        if (f.projectsIds() != null && !f.projectsIds().isEmpty() && !f.projectsIds().contains(0)) {
            criteria.add(Criteria.where("projectId").in(f.projectsIds()));
        }

        if (f.requesterName() != null && !f.requesterName().isBlank()) {
            Pattern requesterNamePattern = Pattern.compile(Pattern.quote(f.requesterName()), Pattern.CASE_INSENSITIVE);
            criteria.add(Criteria.where("solicitante.nome").regex(requesterNamePattern));
        }

        if (f.complainantId() != null && !f.complainantId().isBlank()) {
            Pattern complainantNamePattern = Pattern.compile(Pattern.quote(f.complainantId()), Pattern.CASE_INSENSITIVE);
            criteria.add(Criteria.where("colaborador.name").regex(complainantNamePattern));
        }

        if (f.situations() != null && !f.situations().isEmpty()) {
            criteria.add(Criteria.where("situacao").in(f.situations()));
        }

        if (f.stepLogUserId() != null) {

            List<String> values = List.of("Finalização", "Rejeição em grupo", "Aprovado");
            criteria.add(Criteria.where("etapa_log").elemMatch(
                    Criteria.where("usuario").is(UUID.fromString(f.stepLogUserId()))
                            .and("name").in(values)
            ));
        }

        // Filtro direto por filial_id_hcm quando fornecido
        if (f.filialHcmIds() != null && !f.filialHcmIds().isEmpty()) {
            // Converter List<Long> para List<Integer> porque o campo é salvo como Integer no MongoDB
            List<Integer> filialHcmIdsAsInt = f.filialHcmIds().stream()
                .map(Long::intValue)
                .collect(Collectors.toList());
            
            criteria.add(Criteria.where("colaborador.filial_id_hcm").in(filialHcmIdsAsInt));
        }

        // Filtros hierárquicos otimizados - serão aplicados diretamente na query MongoDB
        if (f.diretoriaIds() != null && !f.diretoriaIds().isEmpty()) {
            // Este filtro será aplicado no service com os HCM IDs coletados
            // Mantido aqui apenas para estrutura, mas será substituído no service
        }

        if (f.superintendenciaIds() != null && !f.superintendenciaIds().isEmpty()) {
            // Este filtro será aplicado no service com os HCM IDs coletados
            // Mantido aqui apenas para estrutura, mas será substituído no service
        }

        if (f.regionalIds() != null && !f.regionalIds().isEmpty()) {
            // Este filtro será aplicado no service com os HCM IDs coletados
            // Mantido aqui apenas para estrutura, mas será substituído no service
        }

        // Filtro por teamIds será tratado no service após popular os teamIds
        // Removido daqui pois teamId é populado após a query

        Criteria combined = criteria.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(criteria.toArray(new Criteria[0]));

        Query countQuery = Query.query(combined);
        long total = mongo.count(countQuery, OcorrenciaFF.class, "ocorrencia_ff");

        Query pagedQuery = Query.query(combined)
                .with(pageable);
        List<OcorrenciaFF> results = mongo.find(pagedQuery, OcorrenciaFF.class, "ocorrencia_ff");
        return new PageImpl<>(results, pageable, total);
    }

    public List<OcorrenciaFF> findAllForExport() {
        // Buscar todas as ocorrências sem paginação
        Query query = new Query();
        query.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "criado_em"));
        return mongo.find(query, OcorrenciaFF.class, "ocorrencia_ff");
    }

    @Override
    public Page<OcorrenciaFF> findByFilterWithHierarchicalFilters(OccurrenceFilter f, Pageable pageable, List<String> hcmIds) {
        List<Criteria> criteria = new ArrayList<>();

        // Aplicar todos os filtros básicos
        if (f.statuses() != null && !f.statuses().isEmpty()) {
            criteria.add(Criteria.where("status").in(f.statuses()));
        }

        if (f.currentSteps() != null
                && !f.currentSteps().isEmpty()
                && (f.situations() == null || !f.situations().contains("FINALIZADO"))) {
            criteria.add(Criteria.where("etapa_atual").in(f.currentSteps()));
        }

        if (f.createdFrom() != null || f.createdTo() != null) {
            Criteria dateCrit = Criteria.where("criado_em");
            if (f.createdFrom() != null) {
                Date dateFrom = Date.from(f.createdFrom().atZone(ZoneId.systemDefault()).toInstant());
                dateCrit = dateCrit.gte(dateFrom);
            }
            if (f.createdTo() != null) {
                Date dateTo = Date.from(f.createdTo().atZone(ZoneId.systemDefault()).toInstant());
                dateCrit = dateCrit.lte(dateTo);
            }
            criteria.add(dateCrit);
        }

        if (f.id() != null) {
            criteria.add(Criteria.where("codigo").is(f.id()));
        }

        if (f.codeId() != null) {
            criteria.add(Criteria.where("codeID").is(f.codeId()));
        }

        if (f.origin() != null) {
            criteria.add(Criteria.where("origem").is(f.origin()));
        }

        if (f.type() != null) {
            criteria.add(Criteria.where("tipo").is(f.type()));
        }

        if (f.tipoFluxo() != null && !f.tipoFluxo().isBlank()) {
            criteria.add(Criteria.where("tipoFluxo").is(f.tipoFluxo()));
        }

        if (f.competencias() != null && !f.competencias().isEmpty()) {
            criteria.add(Criteria.where("competencia").in(f.competencias()));
        }

        if (f.regionaisIds() != null && !f.regionaisIds().isEmpty() && !f.regionaisIds().contains(0)) {
            criteria.add(Criteria.where("regionalId").in(f.regionaisIds()));
        }

        if (f.projectsIds() != null && !f.projectsIds().isEmpty() && !f.projectsIds().contains(0)) {
            criteria.add(Criteria.where("projectId").in(f.projectsIds()));
        }

        if (f.requesterName() != null && !f.requesterName().isBlank()) {
            Pattern requesterNamePattern = Pattern.compile(Pattern.quote(f.requesterName()), Pattern.CASE_INSENSITIVE);
            criteria.add(Criteria.where("solicitante.nome").regex(requesterNamePattern));
        }

        if (f.complainantId() != null && !f.complainantId().isBlank()) {
            Pattern complainantNamePattern = Pattern.compile(Pattern.quote(f.complainantId()), Pattern.CASE_INSENSITIVE);
            criteria.add(Criteria.where("colaborador.name").regex(complainantNamePattern));
        }

        if (f.situations() != null && !f.situations().isEmpty()) {
            criteria.add(Criteria.where("situacao").in(f.situations()));
        }

        if (f.stepLogUserId() != null) {
            List<String> values = List.of("Finalização", "Rejeição em grupo", "Aprovado");
            criteria.add(Criteria.where("etapa_log").elemMatch(
                    Criteria.where("usuario").is(UUID.fromString(f.stepLogUserId()))
                            .and("name").in(values)
            ));
        }

        // Filtro hierárquico por centro_custos_id (de projetos, contratos, diretorias, etc)
        // This now properly applies cascade filtering since hcmIds already contains the intersection
        if (hcmIds != null && !hcmIds.isEmpty()) {
            criteria.add(Criteria.where("colaborador.centro_custos_id").in(hcmIds));
        }

        // Filtro direto por filial_id_hcm quando fornecido (deve ser AND com os filtros hierárquicos)
        if (f.filialHcmIds() != null && !f.filialHcmIds().isEmpty()) {
            // Converter List<Long> para List<Integer> porque o campo é salvo como Integer no MongoDB
            List<Integer> filialHcmIdsAsInt = f.filialHcmIds().stream()
                .map(Long::intValue)
                .collect(Collectors.toList());
            
            criteria.add(Criteria.where("colaborador.filial_id_hcm").in(filialHcmIdsAsInt));
        }

        Criteria combined = criteria.isEmpty()
                ? new Criteria()
                : new Criteria().andOperator(criteria.toArray(new Criteria[0]));

        Query countQuery = Query.query(combined);
        long total = mongo.count(countQuery, OcorrenciaFF.class, "ocorrencia_ff");

        Query pagedQuery = Query.query(combined)
                .with(pageable);
        List<OcorrenciaFF> results = mongo.find(pagedQuery, OcorrenciaFF.class, "ocorrencia_ff");
        
        return new PageImpl<>(results, pageable, total);
    }

}

