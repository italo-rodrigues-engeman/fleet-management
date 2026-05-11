package com.indux.core.application.mapper;

import com.indux.core.application.dto.cbo.AllFuncao;
import com.indux.core.application.dto.cbo.FuncaoDTO;
import com.indux.core.application.dto.cbo.HistoryFuncao;
import com.indux.core.application.dto.cbo.UpdateTraining;
import com.indux.core.domain.model.cbo.FuncaoHCM;
import com.indux.core.domain.model.modules.AttachmentEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")

public interface FuncaoHCMMapper {
    List<AllFuncao> getAllFuncaoHCM(List<FuncaoHCM> entity);

    @Mapping(source = "attachments", target = "anexo")
    //@Mapping(target = "history", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "contract", ignore = true)
    FuncaoHCM toEntity(FuncaoDTO dto, List<AttachmentEntity> attachments);

    //@Mapping(target = "history", ignore = true)
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "contract", ignore = true)
    FuncaoHCM updateEntity(UpdateTraining updateTraining);

    @Mapping(target = "anexo", ignore = true)
    @Mapping(source = "anexo", target = "anexos")
    FuncaoDTO toDto(FuncaoHCM funcaoHCM);

    @Mapping(source = "anexo", target = "anexos")
    @Mapping(target = "anexo", ignore = true)
    HistoryFuncao toHistorySnapshot(FuncaoHCM funcaoHCM);
}
