package com.indux.modules.crm.application.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.service.AttachmentService;
import com.indux.core.domain.service.user.UserService;
import com.indux.modules.crm.application.dto.filter.CommercialInteractionsFilter;
import com.indux.modules.crm.application.dto.request.CommercialInteractionsRequest;
import com.indux.modules.crm.application.dto.response.CommercialInteractionsResponse;
import com.indux.modules.crm.application.gateway.CommercialInteractionsFilterGateway;
import com.indux.modules.crm.application.mapper.CommercialInteractionsMapper;
import com.indux.modules.crm.domain.entity.CommercialInteractions;
import com.indux.modules.crm.domain.entity.EngemanAgent;
import com.indux.modules.crm.domain.gateway.CommercialInteractionsGateway;
import com.indux.modules.crm.domain.gateway.EngemanAgentGateway;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommercialInteractionsService {

    private final CommercialInteractionsGateway gateway;
    private final CommercialInteractionsFilterGateway filterGateway;
    private final CommercialInteractionsMapper mapper;
    private final AttachmentService attachmentService;
    private final EngemanAgentGateway engemanAgentGateway;
    private final UserService userService;

    public CommercialInteractionsService(CommercialInteractionsGateway gateway, CommercialInteractionsFilterGateway filterGateway, CommercialInteractionsMapper mapper, AttachmentService attachmentService, EngemanAgentGateway engemanAgentGateway, UserService userService) {
        this.gateway = gateway;
        this.filterGateway = filterGateway;
        this.mapper = mapper;
        this.attachmentService = attachmentService;
        this.engemanAgentGateway = engemanAgentGateway;
        this.userService = userService;
    }

    @Transactional
    public CommercialInteractionsResponse create(
            CommercialInteractionsRequest request,
            List<MultipartFile> arquivosAtencao,
            List<MultipartFile> arquivosDescricao,
            List<MultipartFile> arquivosOportunidade) {

        CommercialInteractions req = mapper.fromRequest(request);

        if (request.representantes_engeman() != null) {
            List<EngemanAgent> agentsEntities = request.representantes_engeman().stream()
                    .map(engemanAgentGateway::getById)
                    .collect(Collectors.toList());
            req.setEngemanAgent(agentsEntities);
        }

        addAttachments(arquivosAtencao, arquivosDescricao, arquivosOportunidade, req);

        CommercialInteractions savedEntity = gateway.create(req);

        return mapper.toResponse(savedEntity);
    }

    @Transactional
    public CommercialInteractionsResponse update(
            CommercialInteractionsRequest request,
            String id,
            List<MultipartFile> arquivosAtencao,
            List<MultipartFile> arquivosDescricao,
            List<MultipartFile> arquivosOportunidade) {

        CommercialInteractions req = mapper.fromRequest(request);

        if (request.representantes_engeman() != null) {
            List<EngemanAgent> agentsEntities = request.representantes_engeman().stream()
                    .map(engemanAgentGateway::getById)
                    .collect(Collectors.toList());
            req.setEngemanAgent(agentsEntities);
        }

        addAttachments(arquivosAtencao, arquivosDescricao, arquivosOportunidade, req);

        CommercialInteractions result = gateway.update(req, id);

        return mapper.toResponse(result);
    }

    @Transactional(readOnly = true)
    public Page<CommercialInteractionsResponse> getAll(Pageable pageable) {
        Page<CommercialInteractions> result = gateway.getAll(pageable);
        return result.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<CommercialInteractionsResponse> getAllByCompany(Pageable pageable, String companyId) {
        Page<CommercialInteractions> result = gateway.getAllByCompany(pageable, companyId);

        return result.map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public CommercialInteractionsResponse getById(String id) {
        CommercialInteractions result = gateway.getById(id);
        return mapper.toResponse(result);
    }

    @Transactional
    public Page<CommercialInteractionsResponse> filter(CommercialInteractionsFilter filter, Pageable pageable) {
        return filterGateway.filter(filter, pageable);
    }

    @Transactional
    public void toggleStatus(String id) {
        gateway.toggleStatus(id);
    }

    private void addAttachments(
            List<MultipartFile> arquivosAtencao,
            List<MultipartFile> arquivosDescricao,
            List<MultipartFile> arquivosOportunidade,
            CommercialInteractions model) {

        if (arquivosAtencao != null && !arquivosAtencao.isEmpty()) {
            List<AttachmentEntity> anexos = attachmentService.createAttachmentsFromMultipartFiles(arquivosAtencao, "crm/commercial_interactions");
            model.setAttentionAttachments(anexos);
        }

        if (arquivosDescricao != null && !arquivosDescricao.isEmpty()) {
            List<AttachmentEntity> anexos = attachmentService.createAttachmentsFromMultipartFiles(arquivosDescricao, "crm/commercial_interactions");
            model.setDescriptionAttachments(anexos);
        }

        if (arquivosOportunidade != null && !arquivosOportunidade.isEmpty()) {
            List<AttachmentEntity> anexos = attachmentService.createAttachmentsFromMultipartFiles(arquivosOportunidade, "crm/commercial_interactions");
            model.setWindowOfOpportunityAttachments(anexos);
        }
    }
}