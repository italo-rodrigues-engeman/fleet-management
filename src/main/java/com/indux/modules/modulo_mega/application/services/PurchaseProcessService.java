package com.indux.modules.modulo_mega.application.services;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.modulo_mega.application.dto.OrdersFilter;
import com.indux.modules.modulo_mega.application.gateway.PurchaseProcessGateway;
import com.indux.modules.modulo_mega.domain.entities.purchase_process.PurchaseProcess;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PurchaseProcessService {
    private final PurchaseProcessGateway gateway;

    public PurchaseProcessService(PurchaseProcessGateway gateway) {
        this.gateway = gateway;
    }

    public PurchaseProcess get(String id){
     return gateway.findById(id).orElseThrow(() -> new ModuleNotFoundFailure("Pedido não encontrado."));
    }

    public Page<PurchaseProcess> getAll(Pageable pageable, OrdersFilter filter) {
        return gateway.filter(pageable, filter);
    }

    public List<String> getStatus(){
        return gateway.getStatus();
    }

}