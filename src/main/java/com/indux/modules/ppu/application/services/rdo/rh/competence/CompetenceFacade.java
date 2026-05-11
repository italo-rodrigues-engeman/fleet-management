package com.indux.modules.ppu.application.services.rdo.rh.competence;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.exception.user.NotFoundEmployee;
import com.indux.modules.ppu.application.dtos.requests.ClosedCompetenceRequest;
import com.indux.modules.ppu.application.dtos.response.competence.CompetenceResponse;
import com.indux.modules.ppu.application.projection.CompetenceProjection;
import com.indux.modules.ppu.domain.entities.mongo.ClosedCompetenceRDO;
import com.indux.modules.ppu.domain.repositories.mongo.ClosedCompetenceRDORepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.List;

@Component
public class CompetenceFacade {

    private final CloseCompetenceService closeService;
    private final CancelCompetenceService cancelService;
    private final MissingCompetenceService missingService;
    private final ClosedCompetenceRDORepository repository;

    public CompetenceFacade(CloseCompetenceService closeService, CancelCompetenceService cancelService, MissingCompetenceService missingService, ClosedCompetenceRDORepository repository) {
        this.closeService = closeService;
        this.cancelService = cancelService;
        this.missingService = missingService;
        this.repository = repository;
    }

    /**
     * Realiza o fechamento da competência para o período e o projeto informado.
     * @param request DTO contendo os detalhes da competência a ser fechada, incluindo observações e confirmação
     * @param userId  ID do usuário responsável pela aprovação
     * @throws ModuleFailure    se:
     *                          <ul>
     *                             li>Já existir uma competência fechada para o período</li>
     *                              <li>Houver datas sem RDOs registrados</li>
     *                              <li>Existirem RDOs pendentes de aprovação</li>
     *                          </ul>
     * @throws NotFoundEmployee se o usuário informado não for encontrado
     * @return DTO contendo informações da competência.
     */
    public CompetenceResponse close(ClosedCompetenceRequest request, String userId) {
        return closeService.closeCompetence(request, userId);
    }

    /**
     * Cancela uma competência previamente fechada.
     * @param competence mês/ano da competência
     * @param userId ID do usuário RH responsável
     */
    public void cancel(YearMonth competence, Long project, String userId) {
        cancelService.cancelCompetence(competence, project, userId);
    }

    public ClosedCompetenceRDO get(@NotNull YearMonth competence, Long project) {
        return repository.findByCompetenceAndProject(competence.toString(), project).orElseThrow(() -> new ModuleNotFoundFailure("Não foi encontrado nenhuma competência para o período e projeto informado."));
    }

    public List<YearMonth> fetchMissingCompetenceInYear(Long project){ return missingService.execute(project);}

    public Page<ClosedCompetenceRDO> fetchAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<CompetenceProjection> fetchAllProjections(Pageable pageable) {
        return repository.findAllBy(pageable);
    }


}