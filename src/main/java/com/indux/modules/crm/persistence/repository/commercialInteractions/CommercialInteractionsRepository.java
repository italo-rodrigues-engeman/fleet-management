package com.indux.modules.crm.persistence.repository.commercialInteractions;

import com.indux.modules.crm.persistence.model.CommercialInteractionsModel;
import com.indux.modules.crm.persistence.model.UnitModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommercialInteractionsRepository extends MongoRepository<CommercialInteractionsModel, String>, CommercialInteractionsRepositoryCustom {

    Page<CommercialInteractionsModel> findAllByCompany(Pageable pageable, String companyId);
}
