package com.indux.modules.training.infra.mappers;

import com.indux.core.application.dto.generic.EmployeeSummaryDTO;
import com.indux.modules.training.application.dto.DueDate;
import com.indux.modules.training.application.dto.DueDetail;
import com.indux.modules.training.application.dto.Dossier;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmployeeTrainingMapper {

    @Named("toDueDate")
    default DueDate toDueDate(EmployeeSummaryDTO employee, int trainingCount, int dossierCount) {
        String status = (dossierCount == trainingCount) ? "Finalizado" : "Atrasado";
        String regional = employee.getRegional() != null ? employee.getRegional() : "";
        String project = employee.getNomeProjeto() != null ? employee.getNomeProjeto() : "";
        
        return new DueDate(
            employee.getId(),
            employee.getMatricula(),
            employee.getNome(),
            employee.getCargoNome() != null ? employee.getCargoNome() : employee.getCargo(),
            regional,
            project,
            trainingCount,
            dossierCount,
            status
        );
    }

    @Named("toDueDetailWithoutTrainings")
    default DueDetail toDueDetailWithoutTrainings(EmployeeSummaryDTO employee) {
        String regional = employee.getRegional() != null ? employee.getRegional() : "";
        String project = employee.getNomeProjeto() != null ? employee.getNomeProjeto() : "";
        
        return new DueDetail(
            employee.getId(),
            employee.getMatricula(),
            employee.getNome(),
            employee.getCargoNome() != null ? employee.getCargoNome() : employee.getCargo(),
            regional,
            project,
            "Sem Treinamentos",
            "SEM_TREINAMENTOS"
        );
    }

    @Named("toDueDetail")
    default DueDetail toDueDetail(
        EmployeeSummaryDTO employee, 
        String trainingName, 
        List<Dossier> dossiers, 
        String status
    ) {
        String regional = employee.getRegional() != null ? employee.getRegional() : "";
        String project = employee.getNomeProjeto() != null ? employee.getNomeProjeto() : "";
        
        return new DueDetail(
            employee.getId(),
            employee.getMatricula(),
            employee.getNome(),
            employee.getCargoNome() != null ? employee.getCargoNome() : employee.getCargo(),
            regional,
            project,
            trainingName,
            status
        );
    }
}