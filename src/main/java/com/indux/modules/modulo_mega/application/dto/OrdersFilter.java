package com.indux.modules.modulo_mega.application.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class OrdersFilter {

    private List<Integer> numeroPedido;
    private List<Integer> codigoItem;
    private List<Integer> codigoGrupo;
    private List<Integer> codigoFornecedor;

    // Filtros originais do banco
    private List<String> tipoPedido; //pedido livre, etc
    private List<String> comprador;
    private List<String> solicitante;
    private List<String> statusItem;
    private List<String> statusPedido;

    //organograma
    private List<Integer> diretoria;
    private List<Integer> superintendencia;
    private List<Integer> regional;
    private List<Integer> projeto;
    private List<Integer> contrato;
    private List<Integer>  filial;

    // Filtros calculados
    private List<String> situacao;            // Ex: "Regular", "Baixado"
    private List<String> tipoPedidoCalculado; // Ex: "Pedido Padrão", "Pedido Livre", "Lançamento Livre","Indefinido"

    private BigDecimal precoMedMin;
    private BigDecimal precoMedMax;
    private Integer qtdePedidosMin;
    private Integer qtdePedidosMax;
    private BigDecimal qtdeComprasMin;
    private BigDecimal qtdeComprasMax;
    private List<Integer> orderNumbersFilter;
    private List<String> unidadeMedida;
    private Integer solicitacao;
    private List<String> ap;
    private List<Integer> documento;

    private List<String> categoria;

    // ----- FILTROS DE DATA (CADASTRO E PEDIDO) -----

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate cadastroStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate cadastroEndDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate pedidoStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate pedidoEndDate;

    // ----- NOVOS FILTROS DE DATA ADICIONADOS -----

    // Solicitação
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate solicitacaoStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate solicitacaoEndDate;

    // Aprovação Solicitação
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate aprovacaoSolicitacaoStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate aprovacaoSolicitacaoEndDate;

    // Aprovação Mapa
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate aprovacaoMapaStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate aprovacaoMapaEndDate;

    // Aprovação Pedido Compra
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate aprovacaoPedidoCompraStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate aprovacaoPedidoCompraEndDate;

    // Emissão NF
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate emissaoNfStartDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate emissaoNfEndDate;

    public enum TIPO_PEDIDO{
        PEDIDO_LIVRE,
        PEDIDO_PADRAO,
        LANCAMENTO_LIVRE
    }
}


