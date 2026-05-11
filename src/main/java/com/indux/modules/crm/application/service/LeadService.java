package com.indux.modules.crm.application.service;

import com.indux.core.domain.service.user.UserService;
import com.indux.modules.crm.application.dto.filter.LeadFilter;
import com.indux.modules.crm.application.dto.request.LeadRequest;
import com.indux.modules.crm.application.dto.response.LeadResponse;
import com.indux.modules.crm.application.dto.response.UnitResponse;
import com.indux.modules.crm.application.gateway.LeadFilterGateway;
import com.indux.modules.crm.application.mapper.LeadMapper;
import com.indux.modules.crm.domain.entity.Lead;
import com.indux.modules.crm.domain.gateway.LeadGateway;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class LeadService {

    private final LeadGateway gateway;
    private final LeadMapper mapper;
    private final LeadFilterGateway filterGateway;
    private final UnitService unitService;
    private final UserService userService;

    public LeadService(LeadGateway gateway, LeadMapper mapper, LeadFilterGateway filterGateway, UnitService unitService, UserService userService) {
        this.gateway = gateway;
        this.mapper = mapper;
        this.filterGateway = filterGateway;
        this.unitService = unitService;
        this.userService = userService;
    }

    @Transactional
    public LeadResponse create(LeadRequest request, String unitId, UUID user) {
        Lead req = mapper.fromRequest(request);

        UnitResponse unitExists = unitService.getById(unitId);

        var userName = userService.getUserById(String.valueOf(user)).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        req.setResponsibleUser(userName.getNome());

        req.setUnit(unitExists.getId());
        req.setCompany(unitExists.getCompany());

        var result = gateway.save(req, unitId);

        return mapper.toResponse(result);
    }

    @Transactional(readOnly = true)
    public Page<LeadResponse> getAll(Pageable pageable) {
        var leads = gateway.getAll(pageable);

        return leads.map(mapper::toResponse);
    }

    @Transactional
    public LeadResponse getById(String id) {
        Lead entity = gateway.getById(id);

        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public Page<LeadResponse> getAllByCompany(Pageable pageable, String id) {
        var leads = gateway.getAllByCompany(pageable, id);

        return leads.map(mapper::toResponse);
    }

    @Transactional
    public Page<LeadResponse> filter(Pageable pageable, LeadFilter filter) {
        var leads = filterGateway.filter(pageable, filter);

        return leads;
    }

    @Transactional
    public LeadResponse update(LeadRequest request, String id) {
        var lead = mapper.fromRequest(request);

        var result = gateway.update(lead, id);

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
}
