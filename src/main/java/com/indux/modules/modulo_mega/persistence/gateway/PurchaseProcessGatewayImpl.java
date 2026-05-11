package com.indux.modules.modulo_mega.persistence.gateway;

import com.indux.modules.modulo_mega.application.dto.OrdersFilter;
import com.indux.modules.modulo_mega.application.gateway.PurchaseProcessGateway;
import com.indux.modules.modulo_mega.domain.entities.purchase_process.PurchaseProcess;
import com.indux.modules.modulo_mega.persistence.mapper.PurchaseProcessPersistenceMapper;
import com.indux.modules.modulo_mega.persistence.repository.PurchaseProcessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PurchaseProcessGatewayImpl implements PurchaseProcessGateway {

    private final PurchaseProcessRepository repository;
    private final PurchaseProcessPersistenceMapper mapper;

    @Override
    public Optional<PurchaseProcess> findById(String id) {
        return repository.findById(id).map(mapper::toEntity);
    }

    @Override
    public Page<PurchaseProcess> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(mapper::toEntity);
    }

    @Override
    public Page<PurchaseProcess> filter(Pageable pageable, OrdersFilter filter) {
        return repository.filter(pageable, filter).map(mapper::toEntity);
    }

    @Override
    public List<String> getStatus() {
        return repository.getStatus();
    }
}
