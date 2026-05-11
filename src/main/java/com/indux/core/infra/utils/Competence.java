package com.indux.core.infra.utils;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public class Competence {

    private final YearMonth period;

    private Competence(YearMonth period) {
        this.period = period;
    }

    /**
     * Dada qualquer data, retorna a competência correspondente:
     * se dia ≥ 16 → competência do mesmo mês;
     * senão → competência do mês anterior.
     */
    public static Competence ofDate(LocalDate date) {
        Objects.requireNonNull(date, "data não pode ser nulo");
        YearMonth ym = YearMonth.of(date.getYear(), date.getMonth());
        if (date.getDayOfMonth() < 16) {
            ym = ym.minusMonths(1);
        }
        return new Competence(ym);
    }

    /** Início da competência: dia 16 do mês. */
    public LocalDate getStart() {
        return period.atDay(16);
    }

    /** Fim da competência: dia 15 do mês seguinte. */
    public LocalDate getEnd() {
        return period.plusMonths(1).atDay(15);
    }

    /** Para exibir/armazenar: “2025-06” representa 16/06/2025→15/07/2025 */
    public YearMonth getPeriod() {
        return period;
    }
}
