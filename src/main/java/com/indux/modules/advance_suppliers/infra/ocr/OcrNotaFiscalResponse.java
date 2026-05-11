package com.indux.modules.advance_suppliers.infra.ocr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OcrNotaFiscalResponse {
    
    private Long id;
    
    private String arquivo;
    
    @JsonProperty("texto_extraido")
    private String textoExtraido;
    
    private boolean processado;
    
    private String erro;
    
    private String mensagem;
    
    @JsonProperty("cnpj_emitente")
    private String cnpjEmitente;
    
    @JsonProperty("razao_social")
    private String razaoSocial;
    
    @JsonProperty("data_emissao")
    private String dataEmissao;
    
    @JsonProperty("valor_total")
    private String valorTotal;
    
    @JsonProperty("numero_nota")
    private String numeroNota;
    
    private String endereco;
    
    @JsonProperty("tipo_documento")
    private String tipoDocumento;
    
    private Estabelecimento estabelecimento;
    
    private Map<String, Object> transacao;
    
    private List<Object> itens;
    
    @JsonProperty("informacoes_adicionais")
    private InformacoesAdicionais informacoesAdicionais;
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Estabelecimento {
        private String cnpj;
        
        @JsonProperty("razao_social")
        private String razaoSocial;
        
        private String endereco;
        
        @JsonProperty("chave_acesso")
        private String chaveAcesso;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InformacoesAdicionais {
        private List<Pagina> paginas;
        
        @JsonProperty("total_paginas")
        private Integer totalPaginas;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Pagina {
        private Estabelecimento estabelecimento;
        
        @JsonProperty("tipo_nota")
        private String tipoNota;
        
        @JsonProperty("valor_total")
        private Double valorTotal;
        
        @JsonProperty("data_emissao")
        private String dataEmissao;
        
        @JsonProperty("numero_nota")
        private String numeroNota;
        
        @JsonProperty("chave_acesso")
        private String chaveAcesso;
        
        private List<Object> itens;
        
        @JsonProperty("qr_code_data")
        private QrCodeData qrCodeData;
        
        private Integer pagina;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QrCodeData {
        @JsonProperty("qr_codes")
        private List<QrCode> qrCodes;
    }
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class QrCode {
        private String content;
        private String type;
        private Integer quality;
        
        @JsonProperty("url_data")
        private Map<String, Object> urlData;
    }
} 