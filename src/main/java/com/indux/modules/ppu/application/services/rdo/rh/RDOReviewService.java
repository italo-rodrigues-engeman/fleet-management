package com.indux.modules.ppu.application.services.rdo.rh;

import com.indux.core.application.service.module.ModulePermissionChecker;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.application.dtos.NextRdoDTO;
import com.indux.modules.ppu.application.dtos.requests.RDOFlowRequest;
import com.indux.modules.ppu.application.dtos.response.RDOPageResponse;
import com.indux.modules.ppu.domain.entities.item.RDORejectionType;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusDP;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLogger;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerType;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.mapper.RDOLoggerUserMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RDOReviewService {
        private final RDORepository repository;
        private final UserService userService;
        private final RDOLoggerUserMapper userMapper;
        private final ModulePermissionChecker permissionChecker;
        @Value("${module.rdo.rh.id}")
        private String rdoModuleId;

        public RDOReviewService(RDORepository repository, UserService userService,
                        RDOLoggerUserMapper userMapper, ModulePermissionChecker permissionChecker) {
                this.repository = repository;
                this.userService = userService;
                this.userMapper = userMapper;
                this.permissionChecker = permissionChecker;
        }

        public NextRdoDTO approve(RDOFlowRequest request, Boolean nextItem, UUID user) {
                var rdo = repository.findById(request.rdo())
                                .orElseThrow(() -> new ModuleNotFoundFailure("RDO não encontrada no sistema."));
                var userEntity = userService.getUserById(user.toString())
                                .orElseThrow(() -> new ModuleNotFoundFailure("Usuário não encontrado."));

                rdo.setStatusDP(RDOStatusDP.APPROVED);
                rdo.getLoggers().add(createLogger(userMapper.toLogger(userEntity), request.justificativa(),
                                RDOLoggerType.APPROVAL, request.motivos()));
                repository.save(rdo);

                if (nextItem)
                        return getNextRDO(rdo);
                return null;
        }

        private RDOLogger createLogger(RDOLoggerUser user, String justification, RDOLoggerType action,
                        List<RDORejectionType> type) {
                final String sector = "RH";
                return RDOLogger.builder()
                                .wasAnalyzed(true)
                                .isInfoCorrect(true)
                                .action(action)
                                .justification(justification)
                                .date(LocalDateTime.now())
                                .type(type)
                                .sector(sector)
                                .user(user)
                                .build();
        }

        @NotNull
        private NextRdoDTO getNextRDO(RDOEntity rdo) {
                Optional<RDOEntity> nextOpt = repository
                                .findFirstByPlatformAndStatusDPOrderBySequentialIdAsc(
                                                rdo.getPlatform(),
                                                RDOStatusDP.PENDING);

                return new NextRdoDTO(nextOpt.map(RDOEntity::getId).orElse(null));
        }

        public NextRdoDTO decline(RDOFlowRequest request, Boolean nextItem, UUID user) {
                var rdo = repository.findById(request.rdo())
                                .orElseThrow(() -> new ModuleNotFoundFailure("RDO não encontrada no sistema."));
                var userEntity = userService.getUserById(user.toString())
                                .orElseThrow(() -> new ModuleNotFoundFailure("Usuário não encontrado."));

                rdo.setStatusOP(RDOStatusOP.CORRECTION);
                rdo.setStatusDP(RDOStatusDP.CORRECTION_OP);
                if (rdo.getLoggers() == null || rdo.getLoggers().isEmpty()) {
                    rdo.setLoggers(List.of(createLogger(
                                    userMapper.toLogger(userEntity),
                                    request.justificativa(),
                                    RDOLoggerType.REJECTION,
                                    request.motivos())));

                } else {
                        rdo.getLoggers()
                        .add(createLogger(userMapper.toLogger(userEntity), request.justificativa(), RDOLoggerType.REJECTION, request.motivos()));
                }
                repository.save(rdo);
                if (nextItem)
                        return getNextRDO(rdo);
                return null;
        }

        public RDOPageResponse fetch(UUID userId, Pageable pageable) {
                List<RDOStatusOP> excluded = List.of(RDOStatusOP.PENDING, RDOStatusOP.CORRECTION);
                List<RDOStatusOP> statusOPs = List.of(RDOStatusOP.APPROVED, RDOStatusOP.CORRECTION, RDOStatusOP.BM);

                var perms = permissionChecker.getAllowedRegionaisAndProjects(
                                UUID.fromString(rdoModuleId), userId);

                var pendingApproval = repository.countByStatusDPAndStatusOPNotIn(RDOStatusDP.PENDING, excluded);
                var pendingCorrection = repository.countByStatusDP(RDOStatusDP.CORRECTION_OP);
                var page = repository.findAllByStatusOPInOrdered(statusOPs, pageable, perms.regionais(),
                                perms.projetos());

                return new RDOPageResponse(
                                page,
                                pendingApproval,
                                pendingCorrection);
        }

}
