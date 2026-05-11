package com.indux.modules.modulo_mega.domain.repository.mongo;

import com.indux.modules.modulo_mega.domain.entities.mongo.ItemSolicitationEntity;
import com.indux.modules.modulo_mega.domain.enums.ItemSolicitationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface ItemSolicitationRepositoryCustom {

    Page<ItemSolicitationEntity> findAllWithFilters(LocalDate dataInicial, LocalDate dataFinal, ItemSolicitationStatus status,
                                                    String createdBy, String mainName, String description, Pageable pageable);
}
