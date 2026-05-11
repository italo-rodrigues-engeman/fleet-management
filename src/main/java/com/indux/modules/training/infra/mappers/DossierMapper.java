package com.indux.modules.training.infra.mappers;

import com.indux.modules.training.application.dto.Dossier;
import com.indux.modules.training.application.dto.DossierClass;
import com.indux.modules.training.domain.entity.ClassEntity;
import com.indux.modules.training.domain.entity.TrainingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface DossierMapper {

    default List<Dossier> toDossierListWithFilter(List<ClassEntity> classEntities, String registrationFilter) {
        return classEntities.stream()
                .collect(Collectors.groupingBy(classEntity -> classEntity.getTraining().getName()))
                .entrySet()
                .stream()
                .map(entry -> {
                    String trainingName = entry.getKey();
                    List<ClassEntity> classes = entry.getValue();
                    
                    List<DossierClass> dossierClasses = classes.stream()
                            .flatMap(classEntity -> classEntity.getCollaborators().stream()
                                    .filter(collaborator -> registrationFilter == null || 
                                            registrationFilter.equals(collaborator.registration()))
                                    .map(collaborator -> new DossierClass(
                                            classEntity.getId(),
                                            convertToLocalDate(classEntity.getDateStrart()),
                                            convertToLocalDate(classEntity.getDateEnd()),
                                            convertToLocalDate(classEntity.getValidity()),
                                            classEntity.getWorkload(),
                                            collaborator.situation(),
                                            collaborator.score(),
                                            classEntity.getType(),
                                            collaborator.attachment(),
                                            classEntity.getAttachment())))
                            .collect(Collectors.toList());

                    
                    return new Dossier(trainingName, dossierClasses);
                })
                .filter(dossier -> !dossier.dossierClasses().isEmpty()) // Filter out dossiers with no classes
                .collect(Collectors.toList());
    }

    default LocalDate convertToLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

}