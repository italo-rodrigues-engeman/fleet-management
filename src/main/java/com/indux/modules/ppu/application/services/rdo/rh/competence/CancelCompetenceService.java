package com.indux.modules.ppu.application.services.rdo.rh.competence;

import com.indux.core.application.dto.user.SimpleUser;
import com.indux.core.domain.service.user.UserService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusDP;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLogger;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerType;
import com.indux.modules.ppu.domain.entities.rdo.logger.RDOLoggerUser;
import com.indux.modules.ppu.domain.repositories.mongo.ClosedCompetenceRDORepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.infra.mapper.RDOLoggerUserMapper;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Component
public class CancelCompetenceService {

    private final ClosedCompetenceRDORepository closedRepository;
    private final RDORepository rdoRepository;
    private final MongoTemplate mongoTemplate;
    private final RDOLoggerUserMapper userMapper;
    private final UserService userService;

    private static final String RH_SECTOR = "RH";

    public CancelCompetenceService(
            ClosedCompetenceRDORepository closedRepository,
            RDORepository rdoRepository,
            MongoTemplate mongoTemplate,
            RDOLoggerUserMapper userMapper,
            UserService userService
    ) {
        this.closedRepository = closedRepository;
        this.rdoRepository = rdoRepository;
        this.mongoTemplate = mongoTemplate;
        this.userMapper = userMapper;
        this.userService = userService;
    }

    /**
     * Cancela uma competência previamente fechada, revertendo os campos dos RDOs envolvidos.
     *
     * @param competence Competência a ser cancelada
     * @param userId Usuário que está realizando o cancelamento
     * @throws ModuleFailure se a competência não for encontrada
     */
    public void cancelCompetence(YearMonth competence, Long project, String userId) {
        var closed = closedRepository.findByCompetenceAndProject(competence.toString(), project).orElseThrow(() -> new ModuleFailure("Competência não encontrada: " + competence));

        SimpleUser user = userService.getUserById(userId)
                .orElseThrow(() -> new NotFoundEmployee("Usuário que deseja cancelar não foi encontrado na nossa base de dados."));

        List<RDOEntity> rdos = rdoRepository.findAllById(closed.getRdosClosed());

        RDOLoggerUser loggerUser = userMapper.toLogger(user);

        rdos.forEach(rdo -> {
            rdo.setCompetence(null);
            rdo.setStatusDP(RDOStatusDP.APPROVED);
            rdo.getLoggers().add(cancelLogger(loggerUser));
        });

        batchUpdateItems(rdos);

        closedRepository.delete(closed);
    }

    private RDOLogger cancelLogger(RDOLoggerUser user) {
        return RDOLogger.builder()
                .wasAnalyzed(true)
                .isInfoCorrect(true)
                .action(RDOLoggerType.CANCELLATION)
                .justification("Cancelamento de competência")
                .date(LocalDateTime.now())
                .sector(RH_SECTOR)
                .user(user)
                .build();
    }

    private void batchUpdateItems(List<RDOEntity> items) {
        BulkOperations ops = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, RDOEntity.class);

        for (RDOEntity dto : items) {
            Query q = Query.query(Criteria.where("_id").is(dto.getId()));
            Update u = new Update()
                    .set("statusDP", dto.getStatusDP())
                    .unset("competence")
                    .set("loggers", dto.getLoggers());

            ops.updateOne(q, u);
        }

        ops.execute();
    }
}
