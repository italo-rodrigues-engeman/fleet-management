package com.indux.modules.ppu.domain.strategy.time.types;

/**
 * Enum que define os tipos de TimeStrategy disponíveis.
 * Cada valor corresponde a uma strategy que calcula distribuição de hora extra/adicional noturno.
 */
public enum TimeStrategyType {
    /**
     * Strategy para linha especial: essa linha não possui hora extra.
     * Todas as horas extras são zeradas.
     */
    SPECIAL_LINE,
    
    /**
     * Strategy para mover hora extra: os valores de hora extra são jogados para outra linha.
     */
    OVERTIME_ANOTHER_LINE,
    
    /**
     * Strategy particional para hora extra: a hora extra dessa linha vale x% para cada colaborador.
     * Conta na linha, não em linha separada.
     */
    OVERTIME_PARTITIONAL,
    
    /**
     * Strategy particional geral: a H.N e a H.E dessa linha valem x% para cada colaborador.
     */
    GENERAL_PARTITIONAL,
    
    /**
     * Strategy para mover colaborador: colaborador que tem hora extra deve ser jogado para outra linha.
     */
    MOVE_OUT,
    
    /**
     * Strategy particional para adicional noturno: o adicional noturno dessa linha vale x% para cada colaborador.
     */
    PREMIUM_NIGHT_PARTITIONAL,
    
    /**
     * Strategy combinada: adicional noturno é incluído no valor medido (MUTATE) 
     * e hora extra vai para outra linha (PRODUCE_VIRTUAL).
     */
    NIGHT_PREMIUM_OVERTIME_TO_LINE,

    /**
     * Strategy foccada para colaboradores que, por contrato, tem 1 H.E e <>NÃO</> são permitidos
     * fazer hora extras além disso.
     */
    HRA;

    /**
     * Retorna o nome da strategy correspondente para uso no registry.
     */
    public String getStrategyName() {
        return name();
    }
}

