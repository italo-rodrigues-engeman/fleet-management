package com.indux.modules.cdi.aplication.service;

import com.indux.modules.cdi.domain.entities.mongo.IdEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
public class CounterService {

    @Autowired
    private MongoTemplate mongoTemplate;

    public long getNextSequence(String sequenceName) {
        IdEntity counter = mongoTemplate.findAndModify(
                query(where("_id").is(sequenceName)),
                new Update().inc("sequence", 1),
                options().returnNew(true).upsert(true),
                IdEntity.class);

        return counter.getSequence();
    }
}
