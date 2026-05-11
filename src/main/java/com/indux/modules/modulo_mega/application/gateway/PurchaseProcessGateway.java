package com.indux.modules.modulo_mega.application.gateway;

import com.indux.modules.modulo_mega.application.dto.OrdersFilter;
import com.indux.modules.modulo_mega.domain.entities.purchase_process.PurchaseProcess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PurchaseProcessGateway {

    Optional<PurchaseProcess> findById(String id);

    Page<PurchaseProcess> findAll(Pageable pageable);

    Page<PurchaseProcess> filter(Pageable pageable, OrdersFilter filter);

    List<String> getStatus();
}
