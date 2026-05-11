package com.indux.modules.ppu.application.services.ppu.total_balance;

import com.indux.modules.ppu.application.services.fixtures.PPUFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TotalBalanceUseCaseTest {
    private TotalBalanceUseCase totalBalanceUseCase;

    @BeforeEach
    void setUp() {
        totalBalanceUseCase = new TotalBalanceUseCase();
    }

    @Test
    @DisplayName("Should return a list of TotalBalance")
    void fetchTotalBalance() {
        var ppu = PPUFixture.fakePPUEntityFunction();
        var result = totalBalanceUseCase.fetchTotalBalance(ppu);
        var expected = List.of(TotalBalanceFixture.createTotalBalanceService(), TotalBalanceFixture.createTotalBalanceEquipment(), TotalBalanceFixture.createTotalBalanceWithoutQuantitySteelCable(), TotalBalanceFixture.createTotalBalanceWithoutQuantityAccessoryKit());
        assertEquals(expected.size(), result.size());
        assertEquals(0, result.get(2).getTotalQuantity());
        assertEquals(10L, result.getFirst().getTotalQuantity());
        assertEquals(BigDecimal.valueOf(100.0), result.getFirst().getTotalValue());
    }

}