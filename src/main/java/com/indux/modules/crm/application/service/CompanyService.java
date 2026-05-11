package com.indux.modules.crm.application.service;

import com.indux.core.domain.model.modules.ModulePermission;
import com.indux.core.domain.model.modules.Modulo;
import com.indux.core.domain.service.module.ModuleManagementService;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.crm.application.dto.request.LeadRequest;
import com.indux.modules.crm.application.dto.request.UnitRequest;
import com.indux.modules.crm.application.dto.response.LeadResponse;
import com.indux.modules.crm.application.dto.response.UnitResponse;
import com.indux.modules.crm.application.gateway.CompanyFilterGateway;
import com.indux.modules.crm.application.dto.filter.CompanyFilter;
import com.indux.modules.crm.application.dto.request.CompanyRequest;
import com.indux.modules.crm.application.dto.response.CompanyResponse;
import com.indux.modules.crm.application.dto.response.CompanySummary;
import com.indux.modules.crm.application.mapper.CompanyMapper;
import com.indux.modules.crm.application.mapper.LeadMapper;
import com.indux.modules.crm.application.mapper.UnitMapper;
import com.indux.modules.crm.domain.entity.Company;
import com.indux.modules.crm.domain.entity.Lead;
import com.indux.modules.crm.domain.entity.Unit;
import com.indux.modules.crm.domain.gateway.CompanyGateway;
import com.indux.modules.crm.persistence.model.CompanyModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyMapper mapper;
    private final CompanyGateway gateway;
    private final CompanyFilterGateway filterGateway;
    private final UserService userService;
    private final UnitService unitService;
    private final LeadService leadService;
    private final ModuleManagementService moduleService;
    private final UnitMapper unitMapper;
    private final LeadMapper leadMapper;


    public CompanyService(CompanyMapper mapper, CompanyGateway gateway, CompanyFilterGateway filterGateway, UserService userService, UnitService unitService, LeadService leadService, ModuleManagementService moduleService, UnitMapper unitMapper, LeadMapper leadMapper) {
        this.mapper = mapper;
        this.gateway = gateway;
        this.filterGateway = filterGateway;
        this.userService = userService;
        this.unitService = unitService;
        this.leadService = leadService;
        this.moduleService = moduleService;
        this.unitMapper = unitMapper;
        this.leadMapper = leadMapper;
    }

    @Transactional
    public CompanyResponse create(CompanyRequest request, UUID user){
       Company entity = mapper.fromRequest(request);

       var userName = userService.getUserById(String.valueOf(user)).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

       entity.setResponsibleUser(userName.getNome());

       Company result = gateway.save(entity);

       addGenericUnitAndLead(result, user);

       return mapper.toResponse(result);
    }

    @Transactional(readOnly = true)
    public Page<CompanySummary> getAll(Pageable pageable) {
        Page<Company> result = gateway.getAll(pageable);

        return result.map(mapper::companyEntityToCompanySummaryDTO);
    }

    @Transactional(readOnly = true)
    public Page<CompanySummary> filter(CompanyFilter filter, Pageable pageable) {
        return filterGateway.filter(filter, pageable);
    }

    @Transactional(readOnly = true)
    public CompanyResponse getById(String id, UUID moduleId, UUID user) {
        Modulo modulo = moduleService.getModuleByID(moduleId);

        ModulePermission permission = modulo.getPermissoes().stream()
                .filter(permissao -> permissao.getResponsable().equals(user))
                .findFirst()
                .orElseThrow(() -> new ModuleNotFoundFailure("Permissão não encontrada"));

        Company result = gateway.getById(id);

        if (permission.getStepsAllowed().contains(2) || modulo.getGerentes().contains(user)) {
            return mapper.toResponse(result);
        }

        result.setPortalUrl("");
        result.setPortalPassword("");
        result.setPortalUser("");

        return mapper.toResponse(result);
    }

    @Transactional
    public CompanyResponse update(CompanyRequest requestDTO, String id) {

        Company request = mapper.fromRequest(requestDTO);

        Company result = gateway.update(request, id);

        return mapper.toResponse(result);
    }

    @Transactional
    public void toggleStatus(String id) {
        gateway.toggleStatus(id);
    }

    @Transactional
    public void delete(String id) {
        gateway.delete(id);
    }

    public void addGenericUnitAndLead(Company company, UUID user) {
        Unit unit = new Unit();
        UnitRequest unitRequest = unitMapper.toRequest(unit.createGeneric());
        UnitResponse unitResponse = unitService.create(unitRequest, company.getId(), user);


        Lead lead = new Lead();
        LeadRequest leadRequest = leadMapper.toRequest(lead.createGeneric());

        LeadResponse leadResponse = leadService.create(leadRequest, unitResponse.getId(), user);
    }
}
