package com.indux.modules.ppu.application.services.rdo.operation;

import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.core.infra.filestorage.exception.StorageException;
import com.indux.modules.ppu.application.dtos.requests.RDOFlowRequest;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusDP;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLogger;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerType;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.mapper.RDOLoggerUserMapper;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class RDOApprovalUseCase {
    private final RDORepository repository;
    private final StorageService storage;
    private final UserService userService;
    private final RDOLoggerUserMapper userMapper;

    public RDOApprovalUseCase(RDORepository repository, StorageService storage, UserService userService, RDOLoggerUserMapper userMapper) {
        this.repository = repository;
        this.storage = storage;
        this.userService = userService;
        this.userMapper = userMapper;
    }

    public RDOEntity approve(RDOFlowRequest request, Boolean nextItem, String userId) {
        var entity = findRDOOrThrow(request.rdo());
        if(entity.getStatusOP() == RDOStatusOP.APPROVED) {
            throw new ModuleNotFoundFailure("RDO já aprovada.");
        }
        var user = findUserOrThrow(userId);

        updateRDOData(entity, request);

        storeAndSetAttachments(entity, request);
        addApprovalLogger(entity, user, request.justificativa());
        repository.save(entity);
        return entity;
    }
    private void updateRDOData(RDOEntity entity, RDOFlowRequest request) {
        entity.setDivergences(request.divergencias());
        entity.setStatusOP(RDOStatusOP.APPROVED);
        entity.setStatusDP(RDOStatusDP.PENDING);
    }

    private void addApprovalLogger(RDOEntity entity, SimpleUser user, String justification) {
        var logger = createLogger(userMapper.toLogger(user), justification, RDOLoggerType.APPROVAL);

        if (entity.getLoggers() == null || entity.getLoggers().isEmpty()) {
            entity.setLoggers(new ArrayList<>(List.of(logger)));
        } else {
            entity.getLoggers().add(logger);
        }
    }
    private RDOLogger createLogger(RDOLoggerUser user, String justification, RDOLoggerType action) {
        final String sector = "OP";
        return RDOLogger.builder()
                .wasAnalyzed(true)
                .isInfoCorrect(true)
                .action(action)
                .justification(justification)
                .date(LocalDateTime.now())
                .sector(sector)
                .user(user)
                .build();
    }

    private RDOEntity findRDOOrThrow(String rdoId) {
        return repository.findById(rdoId)
                .orElseThrow(() -> new ModuleNotFoundFailure("RDO não encontrada no sistema."));
    }

    private SimpleUser findUserOrThrow(String userId) {
        return userService.getUserById(userId)
                .orElseThrow(() -> new NotFoundEmployee("Usuário não encontrado."));
    }

    private void storeAndSetAttachments(RDOEntity entity, RDOFlowRequest request) {
        List<String> uploadedUris = storeAttachments(request, entity);
        if (!uploadedUris.isEmpty()) {
            entity.setAttachments(uploadedUris);
        }
    }


    private List<String> storeAttachments(RDOFlowRequest request, RDOEntity rdo) {
        if (request.anexos() == null || request.anexos().isEmpty()) {
            return Collections.emptyList();
        }
        List<String> uploadList = new ArrayList<>();
        for(var entity : request.anexos()){
            if (entity.file() == null || entity.file().isEmpty()) {
                continue;
            }
            var fileName = storage.store(entity.file(), "rdo/anexos/" + rdo.getId() + "/" + UUID.randomUUID());
            if (fileName == null) {
                throw new StorageException("Erro ao armazenar arquivo: " + entity.file().getName());
            }

            String uri = storage.getRootLocation().relativize(fileName).toString();
            uploadList.add(uri.replace("\\", "/"));
        }
        return uploadList;
    }
}
