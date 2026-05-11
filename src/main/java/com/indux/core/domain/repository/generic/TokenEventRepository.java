package com.indux.core.domain.repository.generic;

import com.indux.core.domain.model.generic.TokenEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TokenEventRepository extends JpaRepository<TokenEvent, UUID> {
    Optional<TokenEvent> findByToken(int token);
}
