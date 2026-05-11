package com.indux.modules.calibration.infra.mappers.repository;

import com.indux.modules.calibration.aplication.dtos.CalibrationStandardFilter;
import com.indux.modules.calibration.domain.entities.mongo.CalibrationStandardEntity;
import com.indux.modules.calibration.domain.entities.mongo.EquipamentEntity;
import com.indux.modules.calibration.domain.entities.mongo.RangeEntity;
import com.indux.modules.calibration.domain.repository.mongo.CalibrationStandardCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
public class CalibrationStandardRepositoryImpl implements CalibrationStandardCustom {
    private final MongoTemplate mongoTemplate;

    public CalibrationStandardRepositoryImpl(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Page<CalibrationStandardEntity> findAllFilter(Pageable pageable, CalibrationStandardFilter filter){
        Query query = new Query().with(pageable);
        Criteria criteria = new Criteria();
        List<Criteria> andCriteria = new ArrayList<>();

        if (filter != null) {

            if(filter.search() != null && !filter.search().isEmpty()) {
                Criteria searchCriteria = new Criteria().orOperator(
                    Criteria.where("model.ni_mega_id").regex(filter.search(), "i"),
                    Criteria.where("model.model").regex(filter.search(), "i")
                );
                andCriteria.add(searchCriteria);
            }

            if (filter.manufecturerId() != null && !filter.manufecturerId().isEmpty()) {
                Query equipQuery = Query.query(Criteria.where("manufacturer").in(filter.manufecturerId()));
                List<EquipamentEntity> equipments = mongoTemplate.find(equipQuery, EquipamentEntity.class);
                
                List<String> equipmentIds = equipments.stream()
                    .map(EquipamentEntity::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                
                if (!equipmentIds.isEmpty()) {
                    andCriteria.add(Criteria.where("equipament").in(equipmentIds));
                } else {
                    andCriteria.add(Criteria.where("id").is(""));
                }
            }

            if (filter.equipamentId() != null && !filter.equipamentId().isEmpty()) {
                andCriteria.add(Criteria.where("equipament").in(filter.equipamentId()));
            }

            if (filter.niMega() != null && !filter.niMega().isEmpty()) {
                List<String> niMegaStrings = filter.niMega().stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());
                
                Criteria modelCriteria = Criteria.where("model").elemMatch(Criteria.where("niMega").in(niMegaStrings));
                andCriteria.add(modelCriteria);
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
                    andCriteria.add(Criteria.where("range").in(rangeIds));
                } else {
                    andCriteria.add(Criteria.where("id").is(""));
                }
            }

            if (filter.propertyId() != null && !filter.propertyId().isEmpty()) {
                andCriteria.add(Criteria.where("properties").in(filter.propertyId()));
            }

            if (filter.time() != null && !filter.time().isEmpty()) {
                andCriteria.add(Criteria.where("periodicity.months").in(filter.time()));
            }

            if (filter.status() != null) {
                andCriteria.add(Criteria.where("status").is(filter.status()));
            }

        }


        if (!andCriteria.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(andCriteria.toArray(new Criteria[0])));
        }
        
        query.with(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "autoIncrementId"));

        List<CalibrationStandardEntity> entities = mongoTemplate.find(query, CalibrationStandardEntity.class);

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), entities.size());
        
        List<CalibrationStandardEntity> paginatedEntities = entities.isEmpty() ? 
            new ArrayList<>() : entities.subList(start, end);
            
        return PageableExecutionUtils.getPage(paginatedEntities, pageable, entities::size);
    }
}