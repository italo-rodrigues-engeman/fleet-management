package com.indux.modules.crm.persistence.gateway;

import com.indux.modules.crm.application.dto.filter.LeadFilter;
import com.indux.modules.crm.application.dto.response.LeadResponse;
import com.indux.modules.crm.application.gateway.LeadFilterGateway;
import com.indux.modules.crm.application.mapper.LeadMapper;
import com.indux.modules.crm.domain.entity.Lead;
import com.indux.modules.crm.domain.gateway.LeadGateway;
import com.indux.modules.crm.persistence.model.CompanyModel;
import com.indux.modules.crm.persistence.model.LeadModel;
import com.indux.modules.crm.persistence.model.UnitModel;
import com.indux.modules.crm.persistence.repository.company.CompanyRepository;
import com.indux.modules.crm.persistence.repository.lead.LeadRepository;
import com.indux.modules.crm.persistence.repository.unit.UnitRepositories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class LeadGatewayImpl implements LeadGateway, LeadFilterGateway {

    private final LeadRepository repository;
    private final LeadMapper mapper;
    private final UnitRepositories unitRepository;
    private final CompanyRepository companyRepository;

    public LeadGatewayImpl(LeadRepository repository, LeadMapper mapper, UnitRepositories unitRepository, CompanyRepository companyRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.unitRepository = unitRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public Lead save(Lead lead, String unitId) {
        LeadModel model = repository.save(mapper.fromEntity(lead));

        UnitModel unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

        CompanyModel company = companyRepository.findById(unit.getCompany())
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

        if (unit.getLeads() == null) {
            unit.setLeads(new ArrayList<>());
        }
        unit.getLeads().add(model);
        unitRepository.save(unit);

        if (company.getLeads() == null) {
            company.setLeads(new ArrayList<>());
        }
        company.getLeads().add(model);
        companyRepository.save(company);

        return mapper.fromModel(model);
    }

    @Override
    public Lead getById(String id) {
        LeadModel model = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead não encontrado") );

        return mapper.fromModel(model);
    }

    @Override
    public Page<Lead> getAllByCompany(Pageable pageable, String companyId) {
        return repository.getAllByCompany(pageable, companyId).map(mapper::fromModel);
    }

    @Override
    public Page<Lead> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::fromModel);
    }

    @Override
    public Page<LeadResponse> filter(Pageable pageable, LeadFilter filter) {
        Page<LeadModel> leads = repository.filter(pageable, filter);

        return leads.map(mapper::toResponseFromModel);
    }

    @Override
    public Lead update(Lead entity, String id) {
        LeadModel exists = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead não encontrado"));

        if (entity.getName() != null) { exists.setName(entity.getName()); }

        if (entity.getFunction() != null) { exists.setFunction(entity.getFunction()); }

        if (entity.getDecisionMakeLevel() != null) { exists.setDecisionMakeLevel(entity.getDecisionMakeLevel()); }

        if (entity.getUnit() != null) { exists.setUnit(entity.getUnit()); }

        if (entity.getMainEmail() != null) { exists.setMainEmail(entity.getMainEmail()); }

        if (entity.getAlternativeEmail() != null) { exists.setAlternativeEmail(entity.getAlternativeEmail()); }

        if (entity.getMainNumber() != null) { exists.setMainNumber(entity.getMainNumber()); }

        if (entity.getAlternativeNumber() != null) { exists.setAlternativeNumber(entity.getAlternativeNumber()); }

        if (entity.getLinkedinAccount() != null) { exists.setLinkedinAccount(entity.getLinkedinAccount()); }

        if (entity.getObservations() != null) { exists.setObservations(entity.getObservations()); }

        if (entity.getStatus() != null) { exists.setStatus(entity.getStatus()); }

        repository.save(exists);

        return mapper.fromModel(exists);
    }

    @Override
    public void toggleStatus(String id) {
        LeadModel exists = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead não encontrado"));

        exists.toggleStatus();
        repository.save(exists);
    }

    @Override
    public void delete(String id) {
        LeadModel leadExists = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead não encontrado"));

        if (leadExists.getUnit() != null) {
            unitRepository.findById(leadExists.getUnit()).ifPresent(unit -> {
                if (unit.getLeads() != null) {
                    unit.getLeads().removeIf(l -> l.getId().equals(id));
                    unitRepository.save(unit);
                }
            });
        }

        if (leadExists.getCompany() != null) {
            companyRepository.findById(leadExists.getCompany()).ifPresent(company -> {
                if (company.getLeads() != null) {
                    company.getLeads().removeIf(l -> l.getId().equals(id));
                    companyRepository.save(company);
                }
            });
        }

        repository.deleteById(id);
    }
}
