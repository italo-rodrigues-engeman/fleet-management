package com.indux.modules.ppu.application.services.rdo.operation.bm;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class BMCalculator {
    private static final MathContext CONTEXT = MathContext.DECIMAL128;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);


    public static BigDecimal calcValue(BigDecimal unitValue, Double quantity) {
        return unitValue.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    //classe em português para facilitar o entendimento rápido dos cálculos xC
    public static BigDecimal calcAproveitamentoPorcentagem(BigDecimal valorMedido, BigDecimal previstoDiario, int diaDaMedida, BigDecimal valorUnitario) {
        if (valorMedido == null || previstoDiario == null || valorUnitario == null || 
            previstoDiario.signum() == 0 || valorUnitario.signum() == 0 || diaDaMedida <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal denominador = previstoDiario.multiply(BigDecimal.valueOf(diaDaMedida)).multiply(valorUnitario);
        return valorMedido.divide(denominador, CONTEXT)
                .subtract(BigDecimal.ONE)
                .multiply(HUNDRED)
                .setScale(2, RoundingMode.DOWN);
    }

    public static BigDecimal calcPossibleReach(
            BigDecimal valorReal,
            LocalDate dataAtual,
            LocalDate startCiclo,
            LocalDate endCiclo
    ) {
        if (valorReal == null || startCiclo == null || endCiclo == null || dataAtual == null) {
            return BigDecimal.ZERO;
        }

        // Se já passou do fechamento, retorna o valor real
        if (dataAtual.isAfter(endCiclo)) {
            return valorReal;
        }

        long diasCorridos = ChronoUnit.DAYS.between(startCiclo, dataAtual) + 1;
        long diasTotais = ChronoUnit.DAYS.between(startCiclo, endCiclo) + 1;

        if (diasCorridos <= 0 || diasTotais <= 0) {
            return BigDecimal.ZERO;
        }

        return valorReal
                .divide(BigDecimal.valueOf(diasCorridos), 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(diasTotais));
    }


    public static BigDecimal calcAproveitamentoEmReal(BigDecimal totalPrevisto, BigDecimal totalMedido) {
        if (totalPrevisto == null || totalMedido == null) {
            return BigDecimal.ZERO;
        }
        
        return totalPrevisto.subtract(totalMedido).setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calcReajusteContratual(BigDecimal total, BigDecimal coeficienteReajusteContratual) {
        if (total == null || coeficienteReajusteContratual == null) {
            return BigDecimal.ZERO;
        }
        
        return total.multiply(coeficienteReajusteContratual).setScale(2, RoundingMode.HALF_UP);
    }


}
