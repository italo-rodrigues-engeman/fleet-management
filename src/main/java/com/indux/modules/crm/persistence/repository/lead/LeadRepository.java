package com.indux.modules.crm.persistence.repository.lead;

import com.indux.modules.crm.domain.entity.Lead;
import com.indux.modules.crm.persistence.model.LeadModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadRepository extends MongoRepository<LeadModel, String>, LeadRepositoryCustom {

    Page<LeadModel> getAllByCompany(Pageable pageable, String companyId);

    void deleteAllByCompany(String companyId);
}
