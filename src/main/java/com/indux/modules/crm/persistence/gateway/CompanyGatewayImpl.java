package com.indux.modules.crm.persistence.gateway;

import com.indux.core.infra.exception.module.ModuleBadRequest;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.crm.application.mapper.CompanyMapper;
import com.indux.modules.crm.domain.entity.Company;
import com.indux.modules.crm.domain.gateway.CompanyGateway;
import com.indux.modules.crm.persistence.model.CompanyModel;
import com.indux.modules.crm.persistence.repository.company.CompanyRepository;
import com.indux.modules.crm.persistence.repository.lead.LeadRepository;
import com.indux.modules.crm.persistence.repository.unit.UnitRepositories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import com.indux.modules.crm.application.dto.filter.CompanyFilter;
import com.indux.modules.crm.application.dto.response.CompanySummary;
import com.indux.modules.crm.application.gateway.CompanyFilterGateway;

import java.util.Objects;
import java.util.Optional;

@Component
public class CompanyGatewayImpl implements CompanyGateway, CompanyFilterGateway {

    private final CompanyRepository repository;
    private final CompanyMapper mapper;
    private final UnitRepositories unitRepository;
    private final LeadRepository leadRepository;

    public CompanyGatewayImpl(CompanyRepository repository, CompanyMapper mapper, UnitRepositories unitRepository, LeadRepository leadRepository) {
        this.repository = repository;
        this.mapper = mapper;
        this.unitRepository = unitRepository;
        this.leadRepository = leadRepository;
    }

    @Override
    public Company save(Company company) {

        Optional<CompanyModel> exists = repository.findByCnpj(company.getCnpj());

        if (exists.isPresent()) {
            throw new RuntimeException("Empresa já cadastrada");
        }

        CompanyModel model = mapper.fromEntity(company);

        CompanyModel result = repository.save(model);
        return mapper.fromModel(result);
    }

    @Override
    public Page<Company> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::fromModel);
    }

    @Override
    public Company getById(String id) {
        CompanyModel exists = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

        return mapper.fromModel(exists);
    }

    @Override
    public Company update(Company company, String id) {

        Optional<CompanyModel> exists = repository.findByCnpj(company.getCnpj());

        if (exists.isPresent() && !Objects.equals(exists.get().getId(), id)) {
            throw new ModuleBadRequest("Este CNPJ pertence a uma empresa já cadastrada!");
        }

        CompanyModel find = repository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Empresa não encontrada"));

        Company entity = mapper.fromModel(find);

        if (company.getCnpj() != null) { entity.setCnpj(company.getCnpj()); }

        if (company.getName() != null) { entity.setName(company.getName()); }

        if (company.getMarket() != null) { entity.setMarket(company.getMarket()); }

        if (company.getSector() != null) { entity.setSector(company.getSector()); }

        if (company.getModality() != null) { entity.setModality(company.getModality()); }

        if (company.getPortalUrl() != null) { entity.setPortalUrl(company.getPortalUrl()); }

        if (company.getPortalUser() != null) { entity.setPortalUser(company.getPortalUser()); }

        if (company.getDetails() != null) { entity.setDetails(company.getDetails()); }

        if (company.getRegistrationCondition() != null) {entity.setRegistrationCondition(company.getRegistrationCondition());}

        CompanyModel model = mapper.fromEntity(entity);

        CompanyModel savedModel = repository.save(model);

        return mapper.fromModel(savedModel);
    }

    @Override
    public void toggleStatus(String id) {
        CompanyModel exists = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

        exists.toggleStatus();
        repository.save(exists);
    }

    @Override
    public Page<CompanySummary> filter(CompanyFilter filter, Pageable pageable) {
        Page<CompanyModel> models = repository.filter(filter, pageable);
        return models.map(model -> mapper.companyEntityToCompanySummaryDTO(mapper.fromModel(model)));
    }

    @Override
    public void delete(String id) {
        repository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Empresa não encontrada"));

        leadRepository.deleteAllByCompany(id);
        unitRepository.deleteAllByCompany(id);

        repository.deleteById(id);
    }
}
