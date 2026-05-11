package com.indux.modules.crm.application.service;

import com.indux.modules.crm.application.dto.filter.EngemanAgentFilter;
import com.indux.modules.crm.application.dto.request.EngemanAgentRequest;
import com.indux.modules.crm.application.dto.response.EngemanAgentResponse;
import com.indux.modules.crm.application.gateway.EngemanAgentFilterGateway;
import com.indux.modules.crm.application.mapper.EngemanAgentMapper;
import com.indux.modules.crm.domain.entity.EngemanAgent;
import com.indux.modules.crm.domain.gateway.EngemanAgentGateway;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.service.AttachmentService;

import java.util.List;

@Service
public class EngemanAgentService {

    private final EngemanAgentGateway gateway;
    private final EngemanAgentFilterGateway filterGateway;
    private final EngemanAgentMapper mapper;
    private final AttachmentService attachmentService;

    public EngemanAgentService(EngemanAgentGateway gateway, EngemanAgentFilterGateway filterGateway, EngemanAgentMapper mapper, AttachmentService attachmentService) {
        this.gateway = gateway;
        this.filterGateway = filterGateway;
        this.mapper = mapper;
        this.attachmentService = attachmentService;
    }

    @Transactional
    public EngemanAgentResponse create(EngemanAgentRequest request, List<MultipartFile> anexos) {
        EngemanAgent agent = mapper.fromRequest(request);

        if (anexos != null && !anexos.isEmpty()) {
            List<AttachmentEntity> anexedArchives = attachmentService.createAttachmentsFromMultipartFiles(anexos, "crm/engeman_agent");
            agent.setAttachments(anexedArchives);
        }

        EngemanAgent entity = gateway.save(agent);
        return mapper.toResponse(entity);
    }

    @Transactional
    public EngemanAgentResponse update(EngemanAgentRequest req, String id, List<MultipartFile> anexos) {
        EngemanAgent request = mapper.fromRequest(req);

        if (anexos != null && !anexos.isEmpty()) {
            List<AttachmentEntity> processedAnexos = attachmentService.createAttachmentsFromMultipartFiles(anexos, "crm/engeman_agent");
            request.setAttachments(processedAnexos);
        }

        EngemanAgent entity = gateway.update(request, id);
        return mapper.toResponse(entity);
    }

    @Transactional(readOnly = true)
    public EngemanAgentResponse getById(String id) {
        return mapper.toResponse(gateway.getById(id));
    }

    @Transactional(readOnly = true)
    public Page<EngemanAgentResponse> getAll(Pageable pageable) {
        return gateway.getAll(pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<EngemanAgentResponse> filter(EngemanAgentFilter filter, Pageable pageable) {
        return filterGateway.filter(filter, pageable);
    }

    @Transactional
    public void toggleStatus(String id) {
        gateway.toggleStatus(id);
    }
}