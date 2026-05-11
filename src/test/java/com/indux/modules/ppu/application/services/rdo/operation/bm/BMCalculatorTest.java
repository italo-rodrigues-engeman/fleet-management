package com.indux.modules.ppu.application.services.rdo.operation.bm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BMCalculatorTest {

        @Test
        @DisplayName("Should calculate 'Aproveitamento' and 'Alcance Possível' correctly")
        void testAproveitamentoEAlcancePossivel() {
            BigDecimal valorUnitario = new BigDecimal("610.57");
            int quantidadePrevistaDiaria = 7;
            int diasCorridos = 20;
            int diasTotais = 31;
            int quantidadeRealizada = 121;

            BigDecimal valorReal = valorUnitario.multiply(BigDecimal.valueOf(quantidadeRealizada));
            BigDecimal expectedValorReal = new BigDecimal("73878.97");

            LocalDate dataAtual = LocalDate.of(2025, 10, 20);
            LocalDate startCiclo = LocalDate.of(2025, 10, 1);
            LocalDate endCiclo = LocalDate.of(2025, 10, 31);

            BigDecimal aproveitamento = BMCalculator.calcAproveitamentoPorcentagem(
                    valorReal,
                    BigDecimal.valueOf(quantidadePrevistaDiaria),
                    diasCorridos,
                    valorUnitario
            );

            BigDecimal expectedAproveitamento = valorReal
                    .divide(valorUnitario.multiply(BigDecimal.valueOf(quantidadePrevistaDiaria))
                            .multiply(BigDecimal.valueOf(diasCorridos)), 4, RoundingMode.HALF_UP).subtract(BigDecimal.ONE).multiply(BigDecimal.valueOf(100));

            BigDecimal alcancePossivel = BMCalculator.calcPossibleReach(
                    valorReal,
                    dataAtual,
                    startCiclo,
                    endCiclo
            );

            BigDecimal expectedAlcance = valorReal
                    .divide(BigDecimal.valueOf(diasCorridos), 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(diasTotais));

            assertEquals(expectedValorReal.setScale(2, RoundingMode.HALF_UP), valorReal.setScale(2, RoundingMode.HALF_UP));
            assertEquals(expectedAproveitamento.setScale(4, RoundingMode.HALF_UP), aproveitamento.setScale(4, RoundingMode.HALF_UP));
            assertEquals(expectedAlcance.setScale(2, RoundingMode.HALF_UP), alcancePossivel.setScale(2, RoundingMode.HALF_UP));
        }

}
