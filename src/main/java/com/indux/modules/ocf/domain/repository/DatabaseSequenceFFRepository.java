package com.indux.modules.ocf.domain.repository;

import com.indux.modules.ocf.domain.model.DatabaseSequenceFF;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DatabaseSequenceFFRepository extends JpaRepository<DatabaseSequenceFF, Long> {
    
    @Modifying
    @Transactional
    @Query(value = "UPDATE tb_sequence_ff SET document_id = :documentId WHERE id = :id", nativeQuery = true)
    void updateDocumentId(@Param("id") Long id, @Param("documentId") String documentId);
}
