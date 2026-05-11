package com.indux.modules.crm.application.service;

import com.indux.core.domain.service.user.UserService;
import com.indux.modules.crm.application.dto.filter.UnitFilter;
import com.indux.modules.crm.application.dto.filter.UnitFilterAll;
import com.indux.modules.crm.application.dto.request.UnitRequest;
import com.indux.modules.crm.application.dto.response.UnitResponse;
import com.indux.modules.crm.application.gateway.UnitFilterGateway;
import com.indux.modules.crm.application.mapper.UnitMapper;
import com.indux.modules.crm.domain.gateway.UnitGateway;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UnitService {

    private final UnitGateway gateway;
    private final UnitMapper mapper;
    private final UserService userService;
    private final UnitFilterGateway filterGateway;

    public UnitService(UnitGateway gateway, UnitMapper mapper, UserService userService, UnitFilterGateway filterGateway) {
        this.gateway = gateway;
        this.mapper = mapper;
        this.userService = userService;
        this.filterGateway = filterGateway;
    }

    @Transactional
    public UnitResponse create(UnitRequest request, String companyId, UUID user){
        var entity = mapper.fromRequest(request);
        entity.setCompany(companyId);

        var userName = userService.getUserById(String.valueOf(user)).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        entity.setResponsibleUser(userName.getNome());
        var result = gateway.save(entity, companyId);
        return mapper.toResponse(result);
    }

    @Transactional(readOnly = true)
    public Page<UnitResponse> getAll(Pageable pageable) {
        var models = gateway.getAll(pageable);

        return models.map(mapper::toResponse);
    }


    @Transactional(readOnly = true)
    public UnitResponse getById(String id) {
        var entity = gateway.getById(id);

        return mapper.toResponse(entity);
    }

    @Transactional
    public Page<UnitResponse> getAllByCompany(Pageable pageable, String companyId) {
        var units = gateway.getAllByCompany(pageable, companyId);

        return units.map(mapper::toResponse);
    }

    @Transactional
    public UnitResponse update(UnitRequest request, String id) {
        var unit = mapper.fromRequest(request);

        var result = gateway.update(unit, id);

        return mapper.toResponse(result);
    }

    @Transactional
    public Page<UnitResponse> filter(UnitFilter filter, String companyId, Pageable pageable) {
        var models = filterGateway.filter(filter, companyId, pageable);

        return models.map(mapper::fromModelToResponse);
    }

    @Transactional
    public Page<UnitResponse> filterAll(UnitFilterAll filter, Pageable pageable) {
        var models = filterGateway.filterAll(filter, pageable);

        return models.map(mapper::fromModelToResponse);
    }

    @Transactional
    public void delete(String id) {
        gateway.delete(id);
    }

    @Transactional
    public void toggleStatus(String id) {
        gateway.toggleStatus(id);
    }
}
