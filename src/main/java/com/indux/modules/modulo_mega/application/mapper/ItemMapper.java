package com.indux.modules.modulo_mega.application.mapper;

import com.indux.modules.modulo_mega.domain.persistence.view.MegaEntityOrder;
import com.indux.modules.modulo_mega.application.dto.ItemDetailedDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ItemMapper {

    @Mapping(source = "idItem", target = "basicInformation.idItem")
    @Mapping(source = "itemName", target = "basicInformation.itemName")
    
    @Mapping(source = "groupCode", target = "basicInformation.groupCode")
    @Mapping(source = "groupName", target = "basicInformation.groupName")
    @Mapping(source = "registerUser", target = "basicInformation.registerUser")
    @Mapping(source = "registerUserEdition", target = "basicInformation.registerUserEdition")
    @Mapping(source = "itemType", target = "basicInformation.itemType")

    @Mapping(source = "creationDate", target = "dates.creationDate")
    
    
    ItemDetailedDTO toDetailedDto(MegaEntityOrder entity);
}