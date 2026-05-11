package com.indux.modules.modulo_mega.persistence.repository;

import com.indux.modules.modulo_mega.application.dto.OrdersFilter;
import com.indux.modules.modulo_mega.persistence.model.PurchaseProcessDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PurchaseProcessFilterRepository {
    Page<PurchaseProcessDocument> filter(Pageable pageable, OrdersFilter filter);

    List<String> getStatus();
}
