package com.indux.modules.crm.persistence.repository.company;

import com.indux.modules.crm.persistence.model.CompanyModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends MongoRepository<CompanyModel, String>, CompanyRepositoryCustom {

    Optional<CompanyModel> findByCnpj(String cnpj);
}
