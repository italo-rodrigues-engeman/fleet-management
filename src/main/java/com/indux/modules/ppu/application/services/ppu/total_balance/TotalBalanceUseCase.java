package com.indux.modules.ppu.application.services.ppu.total_balance;

import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.ppu.LinePPU;
import com.indux.modules.ppu.domain.entities.ppu.TotalBalance;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
@Component
public class TotalBalanceUseCase {

    public List<TotalBalance> fetchTotalBalance(PPUEntity entity) {
        List<TotalBalance> balances = ensureMutable(entity.getTotalBalances());
        entity.setTotalBalances(balances);

        Map<Object, TotalBalance> byId = indexById(balances);

        processLines(byId, balances, entity.getServices());
        processLines(byId, balances, entity.getEquipments());
        processLines(byId, balances, entity.getSteelCables());
        processLines(byId, balances, entity.getAccessoryKits());

        return balances;
    }

    private void processLines(Map<Object, TotalBalance> byId,
                              List<TotalBalance> balances,
                              List<? extends LinePPU> source) {
        if (source == null || source.isEmpty()) return;

        for (LinePPU item : source) {
            if (item == null || item.getId() == null) continue;

            TotalBalance bal = byId.get(item.getId());
            if (bal == null) {
                bal = TotalBalance.builder()
                        .id(item.getId())
                        .ppuNumber(item.getPpuNumber())
                        .genericNumber(item.getGenericNumber())
                        .name(item.getName())
                        .totalQuantity(0L)
                        .totalValue(BigDecimal.ZERO)
                        .build();
                balances.add(bal);
                byId.put(item.getId(), bal);
            }

            long qty = Optional.ofNullable(bal.getTotalQuantity()).orElse(0L);
            BigDecimal unit = toBigDecimal(item.getValue());
            bal.setTotalValue(BigDecimal.valueOf(qty).multiply(unit));
        }
    }

    private Map<Object, TotalBalance> indexById(List<TotalBalance> balances) {
        Map<Object, TotalBalance> map = new LinkedHashMap<>();
        if (balances == null) return map;
        for (TotalBalance b : balances) {
            if (b != null && b.getId() != null) map.putIfAbsent(b.getId(), b);
        }
        return map;
    }

    private static <T> List<T> ensureMutable(List<T> list) {
        if (list == null) return new ArrayList<>();
        return (list instanceof ArrayList<T>) ? list : new ArrayList<>(list);
    }

    private static BigDecimal toBigDecimal(Object val) {
        if (val == null) return BigDecimal.ZERO;
        if (val instanceof BigDecimal bd) return bd;
        if (val instanceof Number n) return new BigDecimal(n.toString()); // sem perda
        try {
            return new BigDecimal(val.toString().trim());
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}

