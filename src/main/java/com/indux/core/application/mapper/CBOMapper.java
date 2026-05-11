package com.indux.core.application.mapper;

import com.indux.core.application.dto.cbo.CBODTO;
import com.indux.core.domain.model.cbo.CBODetails;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CBOMapper {

    CBODTO toDTO(CBODetails entity);
}
