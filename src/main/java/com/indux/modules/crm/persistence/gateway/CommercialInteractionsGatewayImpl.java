package com.indux.modules.crm.persistence.gateway;

import com.indux.modules.crm.application.dto.filter.CommercialInteractionsFilter;
import com.indux.modules.crm.application.dto.response.CommercialInteractionsResponse;
import com.indux.modules.crm.application.dto.response.UnitResponse;
import com.indux.modules.crm.application.gateway.CommercialInteractionsFilterGateway;
import com.indux.modules.crm.application.mapper.CommercialInteractionsMapper;
import com.indux.modules.crm.application.service.UnitService;
import com.indux.modules.crm.domain.entity.CommercialInteractions;
import com.indux.modules.crm.domain.entity.EngemanAgent;
import com.indux.modules.crm.domain.gateway.CommercialInteractionsGateway;
import com.indux.modules.crm.application.mapper.EngemanAgentMapper;
import java.util.stream.Collectors;
import com.indux.modules.crm.persistence.model.CommercialInteractionsModel;
import com.indux.modules.crm.persistence.model.CompanyModel;
import com.indux.modules.crm.persistence.model.UnitModel;
import com.indux.modules.crm.persistence.model.AlertModel;
import com.indux.modules.crm.persistence.repository.alert.AlertRepository;
import com.indux.modules.crm.persistence.repository.commercialInteractions.CommercialInteractionsRepository;
import com.indux.modules.crm.persistence.repository.company.CompanyRepository;
import com.indux.modules.crm.persistence.repository.unit.UnitRepositories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CommercialInteractionsGatewayImpl implements CommercialInteractionsGateway, CommercialInteractionsFilterGateway {

    private final CommercialInteractionsRepository repository;
    private final CompanyRepository companyRepository;
    private final UnitRepositories unitRepository;
    private final UnitService unitService;
    private final CommercialInteractionsMapper mapper;
    private final EngemanAgentMapper engemanAgentMapper;
    private final AlertRepository alertRepository;

    public CommercialInteractionsGatewayImpl(CommercialInteractionsRepository repository, CompanyRepository companyRepository, UnitRepositories unitRepository, UnitService unitService, CommercialInteractionsMapper mapper, EngemanAgentMapper engemanAgentMapper, AlertRepository alertRepository) {
        this.repository = repository;
        this.companyRepository = companyRepository;
        this.unitRepository = unitRepository;
        this.unitService = unitService;
        this.mapper = mapper;
        this.engemanAgentMapper = engemanAgentMapper;
        this.alertRepository = alertRepository;
    }

    @Override
    public CommercialInteractions create(CommercialInteractions entity) {
        CommercialInteractionsModel model = mapper.toModel(entity);

        if (entity.getEngemanAgent() != null) {
            model.setEngemanAgent(entity.getEngemanAgent().stream().map(engemanAgentMapper::fromEntity).collect(Collectors.toList()));
        }

        List<AlertModel> alerts = model.getAlertModel();
        model.setAlertModel(null);
        CommercialInteractionsModel savedModel = repository.save(model);

        if (alerts != null && !alerts.isEmpty()) {
            final CommercialInteractionsModel finalSavedModel = savedModel;
            alerts.forEach(alert -> alert.setCommercialInteraction(finalSavedModel));

            List<AlertModel> savedAlerts = alertRepository.saveAll(alerts);
            savedModel.setAlertModel(savedAlerts);
            savedModel = repository.save(savedModel);
        }

        addingRelations(entity, savedModel);

        return mapper.toEntity(savedModel);
    }

    @Override
    public Page<CommercialInteractions> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toEntity);
    }

    @Override
    public Page<CommercialInteractions> getAllByCompany(Pageable pageable, String companyId) {
        return repository.findAllByCompany(pageable, companyId).map(mapper::toEntity);
    }

    @Override
    public CommercialInteractions getById(String id) {
        CommercialInteractionsModel model = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interação comercial não encontrada"));

        return mapper.toEntity(model);
    }

    @Override
    public void toggleStatus(String id) {
        CommercialInteractionsModel exists = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interação comercial não encontrada"));

        exists.toggleStatus();
        repository.save(exists);
    }

    @Override
    public CommercialInteractions update(CommercialInteractions entity, String id) {
        CommercialInteractionsModel exists = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Interação comercial não encontrada"));

        if (entity.getContactType() != null) exists.setContactType(entity.getContactType());
        if (entity.getDate() != null) exists.setDate(entity.getDate());
        if (entity.getDescription() != null) exists.setDescription(entity.getDescription());
        if (entity.getAttention() != null) exists.setAttention(entity.getAttention());
        if (entity.getWindowOfOpportunity() != null) exists.setWindowOfOpportunity(entity.getWindowOfOpportunity());
        if (entity.getCompany() != null) exists.setCompany(entity.getCompany());
        if (entity.getUnit() != null) exists.setUnit(entity.getUnit());
        if (entity.getLead() != null) exists.setLead(entity.getLead());

        if (entity.getDescriptionAttachments() != null && !entity.getDescriptionAttachments().isEmpty()) {
            if (exists.getDescriptionAttachments() == null) exists.setDescriptionAttachments(new ArrayList<>());
            exists.getDescriptionAttachments().addAll(entity.getDescriptionAttachments());
        }

        if (entity.getAttentionAttachments() != null && !entity.getAttentionAttachments().isEmpty()) {
            if (exists.getAttentionAttachments() == null) exists.setAttentionAttachments(new ArrayList<>());
            exists.getAttentionAttachments().addAll(entity.getAttentionAttachments());
        }

        if (entity.getWindowOfOpportunityAttachments() != null && !entity.getWindowOfOpportunityAttachments().isEmpty()) {
            if (exists.getWindowOfOpportunityAttachments() == null) exists.setWindowOfOpportunityAttachments(new ArrayList<>());
            exists.getWindowOfOpportunityAttachments().addAll(entity.getWindowOfOpportunityAttachments());
        }

        if (entity.getEngemanAgent() != null) {
            exists.setEngemanAgent(entity.getEngemanAgent().stream().map(engemanAgentMapper::fromEntity).collect(Collectors.toList()));
        }

        if (entity.getAlerts() != null) {
            List<AlertModel> updatedAlerts = mapper.toModel(entity).getAlertModel();

            final CommercialInteractionsModel finalExists = exists;
            updatedAlerts.forEach(alert -> alert.setCommercialInteraction(finalExists));

            updatedAlerts = alertRepository.saveAll(updatedAlerts);
            exists.setAlertModel(updatedAlerts);
        }

        CommercialInteractionsModel updatedModel = repository.save(exists);

        addingRelations(entity, updatedModel);

        return mapper.toEntity(updatedModel);
    }

    @Override
    public Page<CommercialInteractionsResponse> filter(CommercialInteractionsFilter filter, Pageable pageable) {
        Page<CommercialInteractionsModel> models = repository.filter(filter, pageable);
        return models.map(mapper::modelToResponse);
    }

    private void addingRelations(CommercialInteractions entity, CommercialInteractionsModel savedModel) {
        if (entity.getCompany() != null) {
            CompanyModel company = companyRepository.findById(entity.getCompany())
                    .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

            if (company.getCommercialInteractions() == null) {
                company.setCommercialInteractions(new ArrayList<>());
            }
            company.getCommercialInteractions().add(savedModel);
            companyRepository.save(company);
        }

        if (entity.getUnit() != null) {
            if (entity.getCompany() == null) {
                throw new RuntimeException("É necessário informar a empresa para vincular uma unidade.");
            }

            Page<UnitResponse> companyUnits = unitService.getAllByCompany(Pageable.unpaged(), entity.getCompany());

            boolean isUnitValid = companyUnits.stream()
                    .anyMatch(unitResponse -> unitResponse.getId().equals(entity.getUnit()));

            if (!isUnitValid) {
                throw new RuntimeException("A unidade informada não pertence à empresa selecionada.");
            }

            UnitModel unit = unitRepository.findById(entity.getUnit())
                    .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

            if (unit.getCommercialInteractionsList() == null) {
                unit.setCommercialInteractionsList(new ArrayList<>());
            }
            unit.getCommercialInteractionsList().add(savedModel);
            unitRepository.save(unit);
        }
    }
}