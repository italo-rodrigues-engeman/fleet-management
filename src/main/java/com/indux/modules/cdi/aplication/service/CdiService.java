package com.indux.modules.cdi.aplication.service;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.cdi.aplication.dtos.*;
import com.indux.modules.cdi.domain.entities.models.Action;
import com.indux.modules.cdi.domain.entities.models.Stage;
import com.indux.modules.cdi.domain.entities.models.Status;
import com.indux.modules.cdi.domain.entities.models.Type;
import com.indux.modules.cdi.domain.entities.mongo.CdiEntity;
import com.indux.modules.cdi.domain.repositories.mongo.CdiRepository;
import com.indux.modules.cdi.infra.mappers.CdiMapper;
import com.indux.modules.cdi.infra.mappers.FilterMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class CdiService {
    private final CdiRepository cdiRepository;
    private final CdiMapper cdiMapper;
    private final FilterMapper filterMapper;
    private final PointsService pointsService;
    private final ActionCDIService actionCDIService;
    private final CounterService  counterService;

    public CdiService(CdiRepository cdiRepository, CdiMapper cdiMapper, FilterMapper filterMapper, PointsService pointsService, ActionCDIService actionCDIService, CounterService counterService) {
        this.cdiRepository = cdiRepository;
        this.cdiMapper = cdiMapper;
        this.filterMapper = filterMapper;
        this.pointsService = pointsService;
        this.actionCDIService = actionCDIService;
        this.counterService = counterService;
    }

    public CdiEntity createCdi(CreateCdiDTO dto){
        var cdi = cdiMapper.toEntity(dto);
        cdi.setStatus(Status.PENDENTE);
        if(cdi.getType().equals(Type.SEGURANÇA)){
            cdi.setStage(Stage.COMITE);
        }else{
            cdi.setStage(Stage.LOCAL);
        }
        Integer totalPoints = getTotalPoints(cdi);
        cdi.setPoints(totalPoints);
        cdi.setCreateAt(new Date());
        cdi.setStatusOrder(1);
        long autoIncrementId = counterService.getNextSequence("cdi_sequence");
        cdi.setAutoIncrementId(autoIncrementId);
        return cdiRepository.save(cdi);
    }

    Integer getTotalPoints(CdiEntity cdi) {
        var points = pointsService.getAllPoints();
        var scope = points.stream().filter(item -> Integer.parseInt(item.id()) == 1).findFirst();
        var complexity = points.stream().filter(item -> Integer.parseInt(item.id()) == 2).findFirst();
        var previstTime = points.stream().filter(item -> Integer.parseInt(item.id()) == 3).findFirst();
        Integer totalPoints = 0;
        totalPoints = getScopePoints(cdi, totalPoints, scope);
        totalPoints = getComplexityPoints(cdi, totalPoints, complexity);
        totalPoints = getPrevistPoints(cdi, totalPoints, previstTime);
        return totalPoints;
    }

    private static Integer getPrevistPoints(CdiEntity cdi, Integer totalPoints, Optional<PointsDTO> previstTime) {
        switch (cdi.getPrevistTime()){
            case CURTO:
                totalPoints += previstTime.get().highPoint();
                break;
            case MEDIO:
                totalPoints += previstTime.get().medioPoint();
                break;
            case LONGO:
                totalPoints += previstTime.get().lowPoint();
                break;
            case DESCONHECIDO:
                totalPoints += 0;
                break;
        }
        return totalPoints;
    }

    private static Integer getComplexityPoints(CdiEntity cdi, Integer totalPoints, Optional<PointsDTO> complexity) {
        switch (cdi.getComplexity()){
            case ALTO:
                totalPoints += complexity.get().highPoint();
                break;
            case MEDIO:
                totalPoints += complexity.get().medioPoint();
                break;
            case BAIXO:
                totalPoints += complexity.get().lowPoint();
                break;
            case DESCONHECIDO:
                totalPoints += 0;
                break;
        }
        return totalPoints;
    }

    private static Integer getScopePoints(CdiEntity cdi, Integer totalPoints, Optional<PointsDTO> scope) {
        switch (cdi.getScope()){
            case NACIONAL:
                totalPoints = scope.get().highPoint();
               break;
            case REGIONAL:
                totalPoints =  scope.get().medioPoint();
                break;
            case CONTRATO:
                totalPoints = scope.get().lowPoint();
                break;
        }
        return totalPoints;
    }

    public Page<GetAllCdiDTO> findAll(
            FilterCdiDTO filter,
            Pageable pageable
    ){
        var newFilter = filterMapper.toEntity(filter);
        var cdi = cdiRepository.getWithFilter(newFilter,pageable);
        var cdiDto = cdiMapper.toDTOs(cdi.getContent());
        return new PageImpl<>(cdiDto, pageable, cdi.getTotalElements());
    }

    public GetIdCdiDTO findById(String id){
        var cdi = cdiRepository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Ideia não emcontrada"));
        return cdiMapper.toDTO(cdi);
    }

    public CdiEntity updateCdi(String id, UpdateCdiDTO avaliation){
        var cdi = cdiRepository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Ideia não emcontrada"));
        AvaliationDTO avaliationDTO;
        avaliationDTO = newAvaliationDTO(avaliation);
        var newPoints = cdi.getPoints()+avaliation.gravidade()+avaliation.urgencia()+avaliation.tendencia()+avaliation.pontos();
        cdi.setPoints(newPoints);
        stageOptions(id, avaliation, cdi, avaliationDTO);
        return cdiRepository.save(cdi);
    }

    private void stageOptions(String id, UpdateCdiDTO avaliation, CdiEntity cdi, AvaliationDTO avaliationDTO) {
        switch (cdi.getStage()){
            case LOCAL:
                cdi.setLocal(avaliationDTO);
                cdi.setStatus(Status.ANALISE);
                cdi.setStatusOrder(2);
                cdi.setStage(Stage.DIRETORIA1);
                break;
            case DIRETORIA1:
                cdi.setDir1(avaliationDTO);
                cdi.setStage(Stage.DIRETORIA2);
                break;
            case DIRETORIA2:
            case COMITE:
                if(avaliation.acao() == null){
                    throw new ModuleNotFoundFailure("Ação invalida");
                }
                if(avaliation.acao().equals(Action.APROVADO)){
                    if(avaliation.realizador() == null || avaliation.prioridade() == null){
                        throw new ModuleNotFoundFailure("Nescessario Realizador ou Prioridade");
                    }
                    cdi.setDir2(avaliationDTO);
                    cdi.setPriortyLevel(avaliation.prioridade());
                    var priortyPoints = getPriortyPoints(avaliation);
                    cdi.setPoints(cdi.getPoints()+priortyPoints);
                    CreateActionDTO action;
                    if (cdi.getType() == Type.DESENVOLVIMENTO){
                        action = new CreateActionDTO(id, avaliation.realizador(), avaliation.prazo(), cdi.getTitle(), true, cdi.getPoints());
                    }else{
                        action = new CreateActionDTO(id, avaliation.realizador(), avaliation.prazo(), cdi.getTitle(),false, cdi.getPoints());
                    }
                    actionCDIService.createActionCdi(action);
                    cdi.setStatus(Status.APROVADO);
                    cdi.setStatusOrder(3);
                    cdi.setStage(Stage.AÇÃO);
                }
                if(avaliation.acao().equals(Action.REJEITADO)){
                    cdi.setStatus(Status.REJEITADO);
                    cdi.setStatusOrder(5);
                    cdi.setStage(Stage.CONCLUIDO);
                }
                break;
            case AÇÃO:
                if(avaliation.acao() == null){
                    throw new ModuleNotFoundFailure("Ação invalida");
                 }
                if(avaliation.acao().equals(Action.CONCLUIDO)){
                    cdi.setStage(Stage.CONCLUIDO);
                }else if (avaliation.acao().equals(Action.INTERROMPIDO)){
                    cdi.setStatus(Status.INTERROMPIDO);
                    cdi.setStatusOrder(4);
                }else if(avaliation.acao().equals(Action.CANCELADO)){
                    cdi.setStatus(Status.REJEITADO);
                    cdi.setStatusOrder(5);
                }
                break;
        }
    }

    Integer getPriortyPoints(UpdateCdiDTO avaliation) {
        var points = pointsService.getAllPoints();
        var complexity = points.stream().filter(item -> Integer.parseInt(item.id()) == 4).findFirst();
        var priortyPoints = 0;
        switch (avaliation.prioridade()){
            case ALTO:
                priortyPoints = complexity.get().highPoint();
                break;
            case MEDIO:
                priortyPoints =  complexity.get().medioPoint();
                break;
            case BAIXO:
                priortyPoints = complexity.get().lowPoint();
                break;
        }
        return priortyPoints;
    }

    static AvaliationDTO newAvaliationDTO(UpdateCdiDTO avaliation) {
        return new AvaliationDTO(
                avaliation.gravidade(),
                avaliation.urgencia(),
                avaliation.tendencia(),
                avaliation.observacao(),
                new Date(),
                avaliation.criterios(),
                avaliation.avaliador());
    }
}
