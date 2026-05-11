package com.indux.modules.whatsapp_media.domain.repository;

import com.indux.modules.whatsapp_media.domain.entity.WhatsappMediaLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WhatsappMediaLogRepository extends MongoRepository<WhatsappMediaLog, String> {


}
