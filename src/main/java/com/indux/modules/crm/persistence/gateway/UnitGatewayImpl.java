package com.indux.modules.crm.persistence.gateway;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.crm.application.dto.filter.UnitFilter;
import com.indux.modules.crm.application.dto.filter.UnitFilterAll;
import com.indux.modules.crm.application.gateway.UnitFilterGateway;
import com.indux.modules.crm.application.mapper.UnitMapper;
import com.indux.modules.crm.domain.entity.Company;
import com.indux.modules.crm.domain.entity.Unit;
import com.indux.modules.crm.domain.gateway.CompanyGateway;
import com.indux.modules.crm.domain.gateway.UnitGateway;
import com.indux.modules.crm.persistence.model.CompanyModel;
import com.indux.modules.crm.persistence.model.UnitModel;
import com.indux.modules.crm.persistence.repository.company.CompanyRepository;
import com.indux.modules.crm.persistence.repository.unit.UnitRepositories;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class UnitGatewayImpl implements UnitGateway, UnitFilterGateway {

    private final UnitRepositories repository;
    private final CompanyGateway companyGateway;
    private final CompanyRepository companyRepository; // Adicionado para gerenciar o DBRef
    private final UnitMapper mapper;

    public UnitGatewayImpl(UnitRepositories repository, CompanyGateway companyGateway, CompanyRepository companyRepository, UnitMapper mapper) {
        this.repository = repository;
        this.companyGateway = companyGateway;
        this.companyRepository = companyRepository;
        this.mapper = mapper;
    }

    @Override
    public Unit save(Unit unit, String companyId) {
        UnitModel model = mapper.fromEntity(unit);

        model.setCompany(companyId);
        var savedUnitModel = repository.save(model);

        CompanyModel companyModel = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Empresa não encontrada"));

        if (companyModel.getUnits() == null) {
            companyModel.setUnits(new ArrayList<>());
        }
        companyModel.getUnits().add(savedUnitModel);

        companyRepository.save(companyModel);

        return mapper.fromModel(savedUnitModel);
    }

    @Override
    public Page<Unit> getAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::fromModel);
    }

    @Override
    public Unit getById(String id) {
        UnitModel model = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

        return mapper.fromModel(model);
    }

    @Override
    public Page<Unit> getAllByCompany(Pageable pageable, String companyId) {
        Company company = companyGateway.getById(companyId); // Valida se a empresa existe
        Page<UnitModel> models = repository.findAllByCompany(pageable, companyId);
        return models.map(mapper::fromModel);
    }

    @Override
    public Unit update(Unit unit, String id) {
        UnitModel exists = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

        if (unit.getName() != null) {exists.setName(unit.getName());}
        if (unit.getType() != null) {exists.setType(unit.getType());}
        if (unit.getCity() != null) {exists.setCity(unit.getCity());}
        if (unit.getCep() != null) {exists.setCep(unit.getCep());}
        if (unit.getAddress() != null) {exists.setAddress(unit.getAddress());}
        if (unit.getState() != null) {exists.setState(unit.getState());}
        if (unit.getGoogleMapsLink() != null) {exists.setGoogleMapsLink(unit.getGoogleMapsLink());}
        if (unit.getNumber() != null) {exists.setNumber(unit.getNumber());}
        if (unit.getObservations() != null) {exists.setObservations(unit.getObservations());}

        repository.save(exists);
        return mapper.fromModel(exists);
    }

    @Override
    public Page<UnitModel> filter(UnitFilter filter, String companyId, Pageable pageable) {
        return repository.filter(filter, companyId, pageable);
    }

    @Override
    public Page<UnitModel> filterAll(UnitFilterAll filter, Pageable pageable) {
        return repository.filterAll(filter, pageable);
    }

    @Override
    public void delete(String id) {
        UnitModel unitExists = repository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Unidade não encontrada"));

        if (unitExists.getType().equals("MATRIZ")) {
            throw new ModuleFailure("Não é possível deletar a unidade Matriz");
        }

        if (unitExists.getCompany() != null) {
            companyRepository.findById(unitExists.getCompany()).ifPresent(company -> {
                if (company.getUnits() != null) {
                    company.getUnits().removeIf(u -> u.getId().equals(id));

                    companyRepository.save(company);
                }
            });
        }

        repository.deleteById(id);
    }

    @Override
    public void toggleStatus(String id) {
        UnitModel exists = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Unidade não encontrada"));

        exists.toggleStatus();
        repository.save(exists);
    }
}