package com.indux.modules.ppu.domain.repositories.mongo;

import com.indux.modules.ppu.domain.entities.mongo.ChangeTicket;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicketGridProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChangeTicketRepository extends MongoRepository<ChangeTicket, String> {
    Page<ChangeTicketGridProjection> findAllBy(Pageable pageable);
    Page<ChangeTicketGridProjection> findByPpuId(String ppuId, Pageable pageable);
}
