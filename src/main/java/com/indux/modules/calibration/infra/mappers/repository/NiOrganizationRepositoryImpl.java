package com.indux.modules.calibration.infra.mappers.repository;

import com.indux.modules.calibration.aplication.dtos.CalibrationFilter;
import com.indux.modules.calibration.aplication.dtos.CalibrationStandardFilter;
import com.indux.modules.calibration.domain.entities.mongo.CalibrationEntity;
import com.indux.modules.calibration.domain.entities.mongo.CalibrationStandardEntity;
import com.indux.modules.calibration.domain.entities.mongo.EquipamentEntity;
import com.indux.modules.calibration.domain.entities.mongo.NiOrganizationEntity;
import com.indux.modules.calibration.domain.entities.mongo.RangeEntity;
import com.indux.modules.calibration.domain.repository.mongo.NiOrganizationRepository;
import com.indux.modules.calibration.domain.repository.mongo.NiOrganizationRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class NiOrganizationRepositoryImpl implements NiOrganizationRepositoryCustom {
    private final MongoTemplate mongoTemplate;

    public NiOrganizationRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<NiOrganizationEntity> findAllFilter(Pageable pageable, CalibrationFilter filter){
        Query query = new Query().with(pageable);
        List<Criteria> andCriteria = new ArrayList<>();

        if (filter != null) {
            if (filter.manufecturerId() != null && !filter.manufecturerId().isEmpty()) {
                Query equipmentQuery = Query.query(
                    Criteria.where("manufacturer").in(filter.manufecturerId())
                );
                List<EquipamentEntity> equipments = mongoTemplate.find(
                    equipmentQuery, 
                    EquipamentEntity.class
                );

                List<String> equipmentIds = equipments.stream()
                    .map(EquipamentEntity::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                if (!equipmentIds.isEmpty()) {
                    Query calibrationStandardQuery = Query.query(
                        Criteria.where("equipament").in(equipmentIds)
                    );
                    List<CalibrationStandardEntity> calibrationStandards = mongoTemplate.find(
                        calibrationStandardQuery, 
                        CalibrationStandardEntity.class
                    );

                    List<String> calibrationStandardIds = calibrationStandards.stream()
                        .map(CalibrationStandardEntity::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                    if (!calibrationStandardIds.isEmpty()) {
                        andCriteria.add(Criteria.where("calibrationStandard").in(calibrationStandardIds));
                    } else {
                        andCriteria.add(Criteria.where("id").is(""));
                    }
                } else {
                    andCriteria.add(Criteria.where("id").is(""));
                }
            }

            if (filter.equipamentId() != null && !filter.equipamentId().isEmpty()) {
                Query calibrationStandardQuery = Query.query(
                    Criteria.where("equipament").in(filter.equipamentId())
                );
                List<CalibrationStandardEntity> calibrationStandards = mongoTemplate.find(
                    calibrationStandardQuery, 
                    CalibrationStandardEntity.class
                );

                List<String> calibrationStandardIds = calibrationStandards.stream()
                    .map(CalibrationStandardEntity::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                if (!calibrationStandardIds.isEmpty()) {
                    andCriteria.add(Criteria.where("calibrationStandard").in(calibrationStandardIds));
                } else {
                    andCriteria.add(Criteria.where("id").is(""));
                }
            }

            if (filter.niMega() != null && !filter.niMega().isEmpty()) {
                List<String> niMegaStrings = filter.niMega().stream()
                        .map(String::valueOf)
                        .collect(Collectors.toList());

                Query calibrationStandardQuery = Query.query(
                    Criteria.where("model.ni_mega_id").in(niMegaStrings)
                );
                List<CalibrationStandardEntity> calibrationStandards = mongoTemplate.find(
                    calibrationStandardQuery, 
                    CalibrationStandardEntity.class
                );

                List<String> calibrationStandardIds = calibrationStandards.stream()
                    .map(CalibrationStandardEntity::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                if (!calibrationStandardIds.isEmpty()) {
                    andCriteria.add(Criteria.where("calibrationStandard").in(calibrationStandardIds));
                } else {
                    andCriteria.add(Criteria.where("id").is(""));
                }
            }

            if (filter.unitId() != null && !filter.unitId().isEmpty()) {
                Query rangeQuery = Query.query(
                    Criteria.where("unit").in(filter.unitId())
                );
                List<RangeEntity> ranges = mongoTemplate.find(
                    rangeQuery, 
                    RangeEntity.class
                );

                List<String> rangeIds = ranges.stream()
                    .map(RangeEntity::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                if (!rangeIds.isEmpty()) {
                    Query calibrationStandardQuery = Query.query(
                        Criteria.where("range").in(rangeIds)
                    );
                    List<CalibrationStandardEntity> calibrationStandards = mongoTemplate.find(
                        calibrationStandardQuery, 
                        CalibrationStandardEntity.class
                    );

                    List<String> calibrationStandardIds = calibrationStandards.stream()
                        .map(CalibrationStandardEntity::getId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                    if (!calibrationStandardIds.isEmpty()) {
                        andCriteria.add(Criteria.where("calibrationStandard").in(calibrationStandardIds));
                    } else {
                        andCriteria.add(Criteria.where("id").is(""));
                    }
                } else {
                    andCriteria.add(Criteria.where("id").is(""));
                }
            }

            if (filter.propertyId() != null && !filter.propertyId().isEmpty()) {
                Query calibrationStandardQuery = Query.query(
                    Criteria.where("properties").in(filter.propertyId())
                );
                List<CalibrationStandardEntity> calibrationStandards = mongoTemplate.find(
                    calibrationStandardQuery, 
                    CalibrationStandardEntity.class
                );

                List<String> calibrationStandardIds = calibrationStandards.stream()
                    .map(CalibrationStandardEntity::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                if (!calibrationStandardIds.isEmpty()) {
                    andCriteria.add(Criteria.where("calibrationStandard").in(calibrationStandardIds));
                } else {
                    andCriteria.add(Criteria.where("id").is(""));
                }
            }

            if (filter.time() != null && !filter.time().isEmpty()) {
                // Get all NiOrganization entities that have calibrations
                Query niOrgQuery = Query.query(
                    Criteria.where("calibration").exists(true).ne(null).not().size(0)
                );
                List<NiOrganizationEntity> niOrganizationsWithCalibrations = mongoTemplate.find(
                    niOrgQuery, 
                    NiOrganizationEntity.class
                );

                // Filter organizations based on the periodicity of their last calibration
                List<String> validOrganizationIds = niOrganizationsWithCalibrations.stream()
                    .filter(entity -> {
                        if (entity.getCalibration() == null || entity.getCalibration().isEmpty()) {
                            return false;
                        }
                        
                        // Get the last calibration
                        var lastCalibrationRef = entity.getCalibration().get(entity.getCalibration().size() - 1);
                        CalibrationEntity lastCalibration = mongoTemplate.findById(
                            lastCalibrationRef.getId(), CalibrationEntity.class);

                        if (lastCalibration == null || lastCalibration.getPeriodicity() == null) {
                            return false;
                        }

                        Integer months = lastCalibration.getPeriodicity().getMonths();
                        return months != null && filter.time().contains(months);
                    })
                    .map(NiOrganizationEntity::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

                if (!validOrganizationIds.isEmpty()) {
                    andCriteria.add(Criteria.where("id").in(validOrganizationIds));
                } else {
                    andCriteria.add(Criteria.where("id").is(""));
                }
            }

            if (filter.branchId() != null && !filter.branchId().isEmpty()) {
                andCriteria.add(Criteria.where("branchIds").in(filter.branchId()));
            }

            if (filter.contractId() != null && !filter.contractId().isEmpty()) {
                andCriteria.add(Criteria.where("contractIds").in(filter.contractId()));
            }

            if (filter.projectId() != null && !filter.projectId().isEmpty()) {
                andCriteria.add(Criteria.where("projectIds").in(filter.projectId()));
            }

            if (filter.late() != null) {
                LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(60);
                if(filter.late()){
                    andCriteria.add(Criteria.where("nextCalibration").lt(thirtyDaysFromNow));
                }else{
                    andCriteria.add(Criteria.where("nextCalibration").gt(thirtyDaysFromNow));
                }
            }
        }

        andCriteria.add(Criteria.where("calibration").exists(true).ne(null).not().size(0));
        
        if (!andCriteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(andCriteria.toArray(new Criteria[0])));
        }

        query.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "autoIncrementId"));

        List<NiOrganizationEntity> entities = mongoTemplate.find(query, NiOrganizationEntity.class);

        if (filter != null && (filter.situation() != null || (filter.calibrationStatus() != null && !filter.calibrationStatus().isEmpty()))) {
            entities = entities.stream()
                .filter(entity -> {
                    if (entity.getCalibration() == null || entity.getCalibration().isEmpty()) {
                        return false;
                    }

                    var lastCalibrationRef = entity.getCalibration().get(entity.getCalibration().size() - 1);
                    
                    CalibrationEntity lastCalibration = mongoTemplate.findById(
                        lastCalibrationRef.getId(), CalibrationEntity.class);

                    if (lastCalibration == null) {
                        return false;
                    }

                    if (filter.situation() != null &&
                        (lastCalibration.getSituation() == null || 
                         !lastCalibration.getSituation().equals(filter.situation()))) {
                        return false;
                    }

                    if (filter.calibrationStatus() != null && !filter.calibrationStatus().isEmpty()) {
                        if (lastCalibration.getStatus() == null ||
                            !filter.calibrationStatus().contains(lastCalibration.getStatus())) {
                            return false;
                        }
                    }

                    return true;
                })
                .collect(Collectors.toList());
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), entities.size());

        List<NiOrganizationEntity> paginatedEntities = entities.isEmpty() ?
                new ArrayList<>() : entities.subList(start, end);

        return PageableExecutionUtils.getPage(paginatedEntities, pageable, entities::size);
    }
    
    @Override
    public Page<NiOrganizationEntity> findBySearchTermAndCalibrationIdCustom(String propriedadeId, String searchTerm, Pageable pageable) {
        Query query = new Query().with(pageable);
        List<Criteria> andCriteria = new ArrayList<>();

        if (propriedadeId != null) {
            Query calibrationStandardQuery = Query.query(
                    Criteria.where("properties").in(propriedadeId)
            );
            List<CalibrationStandardEntity> calibrationStandards = mongoTemplate.find(
                    calibrationStandardQuery,
                    CalibrationStandardEntity.class
            );

            List<String> calibrationStandardIds = calibrationStandards.stream()
                    .map(CalibrationStandardEntity::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!calibrationStandardIds.isEmpty()) {
                andCriteria.add(Criteria.where("calibrationStandard").in(calibrationStandardIds));
            } else {
                andCriteria.add(Criteria.where("id").is(""));
            }
        }

        if(searchTerm != null && !searchTerm.isEmpty()) {
            Criteria heritageCriteria = Criteria.where("heritage").regex(searchTerm, "i");
            Criteria observationCriteria = Criteria.where("observation").regex(searchTerm, "i");
            andCriteria.add(new Criteria().orOperator(heritageCriteria, observationCriteria));
        }

        if (!andCriteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(andCriteria.toArray(new Criteria[0])));
        }
        
        List<NiOrganizationEntity> entities = mongoTemplate.find(query, NiOrganizationEntity.class);
        
        long total = mongoTemplate.count(query, NiOrganizationEntity.class);
        
        return new PageImpl<>(entities, pageable, total);
    }
}