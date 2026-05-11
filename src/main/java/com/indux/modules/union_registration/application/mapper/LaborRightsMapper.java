package com.indux.modules.union_registration.application.mapper;

import com.indux.modules.union_registration.application.dto.labor_rights.LaborRightsDTO;
import com.indux.modules.union_registration.domain.model.LaborContract;
import com.indux.modules.union_registration.domain.model.LaborContractAddendum;
import org.springframework.stereotype.Component;

@Component
public class LaborRightsMapper {
    
    public void mapLaborRightsToEntity(LaborRightsDTO laborRights, LaborContract laborContract) {
        if (laborRights == null) {
            return;
        }
        laborContract.setLaborRights(laborRights);
    }
    
    public LaborRightsDTO mapEntityToLaborRights(LaborContract laborContract) {
        return laborContract.getLaborRights();
    }
    
    public void mapLaborRightsToAddendumEntity(LaborRightsDTO laborRights, LaborContractAddendum addendum) {
        if (laborRights == null) {
            return;
        }
        addendum.setLaborRights(laborRights);
    }
    
    public LaborRightsDTO mapAddendumEntityToLaborRights(LaborContractAddendum addendum) {
        return addendum.getLaborRights();
    }
}
