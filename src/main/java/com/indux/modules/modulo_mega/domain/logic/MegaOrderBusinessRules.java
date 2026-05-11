package com.indux.modules.modulo_mega.domain.logic;

import java.util.Set;

public class MegaOrderBusinessRules {

    private static final Set<String> FINAL_STATUS = Set.of("PEDIDO ATENDIDO", "PEDIDO ENCERRADO");

    /**
     * Calcula o tipo do pedido baseado na presença de AP, NF, Pedido e Solicitação.
     */
    public static String calculateOrderType(boolean hasSol, boolean hasOrder, boolean hasNF, boolean hasAP) {
        // Bitmask: AP=8, NF=4, Order=2, Sol=1
        int state = (hasAP ? 8 : 0) | (hasNF ? 4 : 0) | (hasOrder ? 2 : 0) | (hasSol ? 1 : 0);

        return switch (state) {
            case 15 -> "Pedido Padrão";       // 1111
            case 14 -> "Pedido Livre";        // 1110
            case 12 -> "Lançamento Livre"; // 1000, 1100
            case 8 -> "Lançamento Livre"; 
            case 4 -> "Lançamento Livre"; // 1000, 1100
            case 3  -> "Pedido Padrão";       // 0011
            case 2  -> "Pedido Livre";        // 0010
            default -> "Indefinido";
        };
    }

    /**
     * Define se a situação deve ser "Baixado" ou "Regular".
     */
    public static String determineSituation(String nfNum, String orderStatus) {
        if (isDown(nfNum, orderStatus)) {
            return "Baixado";
        }
        return "Regular";
    }

    public static boolean isDown(String nfNum, String orderStatus) {
        boolean noNF = nfNum == null || nfNum.equalsIgnoreCase("SEM NF");
        boolean finalStatus = orderStatus != null && FINAL_STATUS.contains(orderStatus.toUpperCase());
        
        return noNF && finalStatus;
    }
}