package com.indux.core.domain.repository.generic;

import com.indux.core.domain.model.generic.TokenEventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TokenEventTypeRepository extends JpaRepository<TokenEventType, UUID> {
    TokenEventType findByName(String name);
}
