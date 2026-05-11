package com.indux.core.application.service.cbo;

import com.indux.core.application.dto.cbo.*;
import com.indux.core.application.mapper.CBOMapper;
import com.indux.core.domain.model.cbo.CBODetails;
import com.indux.core.domain.model.cbo.FuncaoHCM;
import com.indux.core.domain.model.employee.Cargo;
import com.indux.core.domain.repository.cbo.CBODetailsRepository;
import com.indux.core.domain.repository.cbo.FuncaoHCMRepository;
import com.indux.core.domain.repository.generic.CargoRepository;
import com.indux.core.domain.repository.generic.EmployeeRepository;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.organization_chart.application.services.SubordinateService;
import com.indux.modules.organization_chart.application.services.FilialService;
import com.indux.modules.organization_chart.domain.entities.jpa.FilialHcmEntity;
import com.indux.modules.training.application.dto.TrainingResponse;
import com.indux.modules.training.application.service.TrainingService;
import com.indux.modules.training.infra.mappers.TrainingMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CBODetailsService {
    private final CBODetailsRepository cboDetailsRepository;
    private final CargoRepository cargoRepository;
    private final CBOMapper cboMapper;
    private final TrainingService trainingService;
    private final FuncaoHCMRepository funcaoHCMRepository;
    private final TrainingMapper trainingMapper;
    private final SubordinateService subordinateService;
    private final FilialService filialService;
    private final EmployeeRepository employeeRepository;

    public CBODetailsService(CBODetailsRepository cboDetailsRepository, CargoRepository cargoRepository,
            CBOMapper cboMapper, TrainingService trainingService, FuncaoHCMRepository funcaoHCMRepository,
            TrainingMapper trainingMapper, SubordinateService subordinateService, FilialService filialService,
            EmployeeRepository employeeRepository) {
        this.cboDetailsRepository = cboDetailsRepository;
        this.cargoRepository = cargoRepository;
        this.cboMapper = cboMapper;
        this.trainingService = trainingService;
        this.funcaoHCMRepository = funcaoHCMRepository;
        this.trainingMapper = trainingMapper;
        this.subordinateService = subordinateService;
        this.filialService = filialService;
        this.employeeRepository = employeeRepository;
    }

    public Page<GetAllCBO> getAll(FilterCBO filter, Pageable pageable) {
        if (filter != null && (
                   isNotEmpty(filter.getDiretoriaId())
                || isNotEmpty(filter.getSuperintendenciaId())
                || isNotEmpty(filter.getRegionalId())
                || isNotEmpty(filter.getSetorId())
                || isNotEmpty(filter.getContratoId())
                || isNotEmpty(filter.getProjetoId())

        )) {
            var filiaisEntities = subordinateService.getFiliaisHcmByFilters(filter.getDiretoriaId(), filter.getSuperintendenciaId(), filter.getRegionalId(),
                    filter.getSetorId(), filter.getContratoId(), filter.getProjetoId());
            List<Integer> filialIds = filiaisEntities.stream()
                    .map(FilialHcmEntity::getFilialId)
                    .collect(Collectors.toList());
            if (filialIds.isEmpty()) {
                filter.setFilialIdHcm(new ArrayList<>(Arrays.asList(0)));
            }else {
                filter.setFilialIdHcm(filialIds);
            }
        }
        Page<Cargo> cargosRaw = cargoRepository.findFilterCargo(filter, pageable);

        List<GetAllCBO> content = cargosRaw.getContent().stream()
                .map(this::mapCargoToGetAllCBO)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, cargosRaw.getTotalElements());
    }

    public boolean isNotEmpty(List<?> value){
        return value != null && !value.isEmpty();
    }

    private GetAllCBO mapCargoToGetAllCBO(Cargo cargo) {
        String nameCBO = cargo.getNameTitleCbo();

        List<Integer> filialIds = cargo.getFilial() != null
                ? cargo.getFilial().stream()
                        .map(FilialHcmEntity::getFilialId)
                        .collect(Collectors.toList())
                : new ArrayList<>();

        List<String> regional = new ArrayList<>();
        List<String> contrato = new ArrayList<>();

        if (!filialIds.isEmpty()) {
            List<Map<String, Object>> organizationData = filialService.filialOrganization(filialIds);
            for (Map<String, Object> org : organizationData) {
                if (org.get("nomeRegional") != null) {
                    String regionalValue = String.valueOf(org.get("nomeRegional"));
                    if (!regional.contains(regionalValue)) {
                        regional.add(regionalValue);
                    }
                }
                if (org.get("nomeContrato") != null) {
                    String contratoValue = String.valueOf(org.get("nomeContrato"));
                    if (!contrato.contains(contratoValue)) {
                        contrato.add(contratoValue);
                    }
                }
            }
        }

        List<String> nomeHCM = new ArrayList<>();
        if (cargo.getNameTitle() != null) {
            nomeHCM.add(cargo.getNameTitle());
        }

        List<TrainingResponse> trainings = getTrainingsForCargo(cargo.getIdHcm());
        List<RequiredTrainingStraring> requiredTrainings = getRequiredTrainingsForCargo(cargo.getIdHcm());

        Long activo = 0L;
        Long totalPerson = 0L;
        if (cargo.getIdHcm() != null && !filialIds.isEmpty()) {
            activo = employeeRepository.countActiveEmployeesByCargoAndFiliais(cargo.getIdHcm(), filialIds);
            totalPerson = employeeRepository.countAllEmployeesByCargoAndFiliais(cargo.getIdHcm(), filialIds);
        }

        return GetAllCBO.builder()
                .idCargo(cargo.getId().toString())
                .idCBO(null)
                .codCBO(cargo.getCodeCbo())
                .nameCBO(nameCBO)
                .branch(regional)
                .contract(contrato)
                .nameHCM(nomeHCM)
                .idHCM(cargo.getIdHcm())
                .totalPerson(totalPerson)
                .os(null)
                .activo(activo)
                .filial(filialIds)
                .trainings(trainings)
                .requiredTrainings(requiredTrainings)
                .build();
    }

    private List<TrainingResponse> getTrainingsForCargo(String idHCM) {
        try {
            if (idHCM != null) {
                var funcaoOptional = funcaoHCMRepository.findByHcmId(idHCM);
                if (funcaoOptional.isPresent()) {
                    FuncaoHCM funcao = funcaoOptional.get();
                    return trainingMapper.toDtos(funcao.getTrainings());
                }
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<RequiredTrainingStraring> getRequiredTrainingsForCargo(String idHCM) {
        try {
            if (idHCM != null) {
                var funcaoOptional = funcaoHCMRepository.findByHcmId(idHCM);
                if (funcaoOptional.isPresent()) {
                    FuncaoHCM funcao = funcaoOptional.get();
                    return funcao.getRequiredTrainings();
                }
            }
            return new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public CBODTO findById(String id) {
        var cbo = cboDetailsRepository.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("CBO não encontrado"));
        return cboMapper.toDTO(cbo);
    }

    public Page<RelatedPosition> findAllCBO(Pageable pageable) {
        List<CBODetails> allCboDetails = cboDetailsRepository.findAll();

        List<RelatedPosition> allRelatedPositions = allCboDetails.stream()
                .filter(cbo -> cbo.getRelatedPosition() != null)
                .flatMap(cbo -> cbo.getRelatedPosition().stream())
                .collect(Collectors.toList());

        int total = allRelatedPositions.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);

        List<RelatedPosition> pageContent = (start >= total)
                ? Collections.emptyList()
                : allRelatedPositions.subList(start, end);

        return new PageImpl<>(pageContent, pageable, total);
    }

    public CBODTO findByIdCBO(Integer idCBO) {
        List<CBODetails> allCboDetails = cboDetailsRepository.findAll();

        CBODetails cbo = allCboDetails.stream()
                .filter(cboDetail -> cboDetail.getRelatedPosition() != null)
                .filter(cboDetail -> cboDetail.getRelatedPosition().stream()
                        .anyMatch(relPos -> relPos.codCBO() != null && relPos.codCBO().equals(idCBO)))
                .findFirst()
                .orElseThrow(() -> new ModuleNotFoundFailure("CBO não encontrado"));

        return cboMapper.toDTO(cbo);
    }

    public void postMatriz(Integer filialId, List<String> cargoIds) {
        FilialHcmEntity filial = filialService.getFilialById(filialId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Filial não encontrada com ID: " + filialId));

        cargoIds.forEach(cargoId -> {
            Cargo cargo = cargoRepository.findById(cargoId)
                    .orElseThrow(() -> new ModuleNotFoundFailure("Cargo não encontrado com ID: " + cargoId));

            if (cargo.getFilial() == null) {
                cargo.setFilial(new ArrayList<>());
            }

            if (!cargo.getFilial().contains(filial)) {
                cargo.getFilial().add(filial);
                cargoRepository.save(cargo);
            }
        });
    }

    public void deleteMatriz(Integer filialId, List<String> cargoIds) {
        FilialHcmEntity filial = filialService.getFilialById(filialId)
                .orElseThrow(() -> new ModuleNotFoundFailure("Filial não encontrada com ID: " + filialId));

        cargoIds.forEach(cargoId -> {
            Cargo cargo = cargoRepository.findById(cargoId)
                    .orElseThrow(() -> new ModuleNotFoundFailure("Cargo não encontrado com ID: " + cargoId));

            if (cargo.getFilial() != null && cargo.getFilial().contains(filial)) {
                cargo.getFilial().remove(filial);
                cargoRepository.save(cargo);
            }
        });
    }

}
