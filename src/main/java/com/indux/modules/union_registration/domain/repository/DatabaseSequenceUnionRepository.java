package com.indux.modules.union_registration.domain.repository;

import com.indux.modules.union_registration.domain.model.DatabaseSequenceUnion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface DatabaseSequenceUnionRepository extends JpaRepository<DatabaseSequenceUnion, Integer> {
    
    @Modifying
    @Transactional
    @Query(value = "UPDATE tb_sindicato_seq SET documento_id = :documentId WHERE id = :id", nativeQuery = true)
    void updateDocumentId(@Param("id") Integer id, @Param("documentId") String documentId);
}
