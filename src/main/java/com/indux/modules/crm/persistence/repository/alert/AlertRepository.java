package com.indux.modules.crm.persistence.repository.alert;

import com.indux.modules.crm.persistence.model.AlertModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AlertRepository extends MongoRepository<AlertModel, String> {
}
