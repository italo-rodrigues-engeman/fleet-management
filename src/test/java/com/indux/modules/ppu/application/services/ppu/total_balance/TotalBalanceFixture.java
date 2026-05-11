package com.indux.modules.ppu.application.services.ppu.total_balance;

import com.indux.modules.ppu.domain.entities.ppu.TotalBalance;

import java.math.BigDecimal;
import java.util.List;

public class TotalBalanceFixture {

    public static TotalBalance createTotalBalanceService() {
        return TotalBalance.builder()
                .id("TESTE")
                .ppuNumber("1.1")
                .genericNumber("1.1.101")
                .name("Nome do Serviço")
                .totalQuantity(10L)
                .totalValue(BigDecimal.ZERO)
                .build();
    }

    public static TotalBalance createTotalBalanceEquipment() {
        return TotalBalance.builder()
                .id("ID-EQUIPAMENTO")
                .ppuNumber("EQUIP-001")
                .genericNumber("EQUIP-001")
                .name("Guindaste Teste")
                .totalQuantity(15L)
                .build();
    }

    public static List<TotalBalance> createTotalBalanceList() {
        return List.of(
                createTotalBalanceService(),
                createTotalBalanceEquipment()
        );
    }
    public static TotalBalance createTotalBalanceWithoutQuantitySteelCable() {
        return TotalBalance.builder()
                .id("ID")
                .ppuNumber("1.2.3")
                .genericNumber("123456789")
                .name("EQUIP-001")
                .build();
    }
    public static TotalBalance createTotalBalanceWithoutQuantityAccessoryKit() {
        return TotalBalance.builder()
                .id("kit-123")
                .ppuNumber("1.0")
                .genericNumber("1.0.0.01")
                .name("Kit de acessórios de teste")
                .build();
    }
}
