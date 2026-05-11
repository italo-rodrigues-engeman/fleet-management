package com.indux.modules.crm.persistence.repository.unit;

import com.indux.modules.crm.persistence.model.UnitModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitRepositories extends MongoRepository<UnitModel, String>, UnitRepositoryCustom {

    Page<UnitModel> findAllByCompany(Pageable pageable, String companyId);

    void deleteAllByCompany(String companyId);
}
