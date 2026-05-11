package com.indux.modules.modulo_mega.service;

import com.indux.modules.modulo_mega.application.dto.AbcCurveGroupDTO;
import com.indux.modules.modulo_mega.application.dto.AbcItemDetailDTO;
import com.indux.modules.modulo_mega.application.dto.ItemHistoryDetailedDTO;
import com.indux.modules.modulo_mega.domain.entities.mongo.ItemSolicitationEntity;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelGeneratorService {

    // =========================================================================
    // 1. GERAÇÃO DE EXCEL DA CURVA ABC (TODOS OS ITENS JUNTOS)
    // =========================================================================
    public byte[] generateAbcCurveExcel(List<AbcCurveGroupDTO> abcGroups) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Curva ABC");

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            //CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle integerStyle = createIntegerStyle(workbook);
            
            int rowIdx = 0;


            // Cabeçalho único
            Row headerRow = sheet.createRow(rowIdx++);
            String[] columns = {
                "Código", "Item", "Grupo", "Status","Tipo",
                "Pedidos", "Qtd", "Preço Médio", "Total","Curva"
            };

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Todos os itens, sem separação por curva
            for (AbcCurveGroupDTO group : abcGroups) {
                for (AbcItemDetailDTO item : group.getItems()) {

                    Row row = sheet.createRow(rowIdx++);

                    row.createCell(0).setCellValue(item.getIdItem());
                    row.createCell(1).setCellValue(item.getItemName());
                    row.createCell(2).setCellValue(item.getGroupName());

                    
                        // Status
                    row.createCell(3).setCellValue(
                        item.getStatus() != null ? item.getStatus() : "-"
                    );
                    //Tipo
                    row.createCell(4).setCellValue(
                        item.getItemType() != null ? item.getItemType(): "-");

                    // Pedidos
                    createCell(row, 5, item.getTotalOrders(), integerStyle);

                    // Quantidade
                    createCell(row, 6, item.getQtdItensTotal(), integerStyle);

                    // Preço Médio
                    createCell(row, 7, item.getAveragePrice(), currencyStyle);

                    // Total
                    createCell(row, 8, item.getTotalValue(), currencyStyle);

                    
                      // Curva
                    row.createCell(9).setCellValue(group.getCurveClass());
                }
            }

            // Auto size
            for (int i = 0; i < 9; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar Excel ABC: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // 2. GERAÇÃO DE EXCEL DO HISTÓRICO DETALHADO
    // =========================================================================
    public byte[] generateItemHistoryExcel(List<ItemHistoryDetailedDTO> historyData) {

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Histórico Detalhado");

            // Estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle dateTimeStyle = createDateTimeStyle(workbook); // NOVO
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            //CellStyle numberStyle = createNumberStyle(workbook);
            CellStyle integerStyle = createIntegerStyle(workbook);

            String[] headers = {
                "Cód. Item", "Descrição", "Status", "Cód. Grupo", "Nome Grupo", "Projeto",
                "Nº Pedido", "Nº Nota","Nº da Solicitacão","AP","Código do Projeto", "Tipo Pedido", "Data Solicitação", "Data Pedido", "Data Entrega",
                "Cód. Fornecedor", "Fornecedor", "CNPJ", "Qtd", "Preço Unit.", "Valor Total",
                "Cód. Regional", "Regional", "Cód. Filial", "Filial",
                "Diretoria", "Superintendência",
                "Contrato", "Solicitante", "Comprador","Status do Item",
                "Unidade de Medida","Tipo",
                "Usuário de Inclusão","Usuário de Alteração",
                "Data Criação"
            };

            int rowIdx = 0;
            Row headerRow = sheet.createRow(rowIdx++);


            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (ItemHistoryDetailedDTO item : historyData) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;

                createCell(row, col++, item.getIdItem(), null);
                row.createCell(col++).setCellValue(item.getItemName());
                row.createCell(col++).setCellValue(item.getOrderStatus());
                createCell(row, col++, item.getGroupCode(), null);
                row.createCell(col++).setCellValue(item.getGroupName());
                row.createCell(col++).setCellValue(item.getProjectName());

                // CORREÇÃO DE ORDEM AQUI (Nota vs Tipo vs Pedido)
                row.createCell(col++).setCellValue(item.getOrderNumber() != null ? item.getOrderNumber().toString() : "");
                row.createCell(col++).setCellValue(item.getNoteNumber() != null ? item.getNoteNumber().toString() : "");
                row.createCell(col++).setCellValue(item.getSolicitation() != null ? item.getSolicitation().toString() : "");
                row.createCell(col++).setCellValue(item.getApprove() != null ? item.getApprove().toString() : "");
                row.createCell(col++).setCellValue(item.getProjectCode() != null ? item.getProjectCode().toString() : "");
                row.createCell(col++).setCellValue(item.getOrderType());   // Tipo Pedido

                createCell(row, col++, item.getSolDate(), dateStyle);
                createCell(row, col++, item.getOrderDate(), dateStyle);
                createCell(row, col++, item.getDeliveryDate(), dateStyle);

                createCell(row, col++, item.getSupplierCode(), null);
                row.createCell(col++).setCellValue(item.getSupplierName());
                row.createCell(col++).setCellValue(item.getCnpj());
                
                createCell(row, col++, item.getQtdItensTotal(), integerStyle);
                createCell(row, col++, item.getAveragePriceFromSupplier(), currencyStyle);
                createCell(row, col++, item.getTotalItemValue(), currencyStyle);

                createCell(row, col++, item.getRegionalCode(), null);
                row.createCell(col++).setCellValue(item.getRegionalName());
                createCell(row, col++, item.getBranchId(), null);
                row.createCell(col++).setCellValue(item.getBranchName());
                
                row.createCell(col++).setCellValue(item.getDirectoryName());
                row.createCell(col++).setCellValue(item.getSuperName());

                row.createCell(col++).setCellValue(item.getContractName());
                row.createCell(col++).setCellValue(item.getRequesterName());
                row.createCell(col++).setCellValue(item.getBuyerName());
                row.createCell(col++).setCellValue(item.getItemStatus());
                
                row.createCell(col++).setCellValue(item.getUnitOfMeasure());
                row.createCell(col++).setCellValue(item.getItemType());
                row.createCell(col++).setCellValue(item.getRegisterUser());
                row.createCell(col++).setCellValue(item.getRegisterUserEdition());
                // MELHORIA: Data com Hora para criação
                createCell(row, col++, item.getCreationDate(), dateTimeStyle); 
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar Excel Histórico: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // 3. GERAÇÃO DE EXCEL DE ITENS CADASTRADOS (SOLICITAÇÃO DE ITEM)
    // =========================================================================
    public byte[] generateRegisteredItemsExcel(List<ItemSolicitationEntity> items) {

        try (Workbook workbook = new org.apache.poi.hssf.usermodel.HSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Importação de itens");

            // Estilos
            CellStyle headerTopStyle = createHeaderTopStyle(workbook);
            CellStyle headerTypeStyle = createHeaderTypeStyle(workbook);
            CellStyle headerFieldStyle = createHeaderFieldStyle(workbook);
            CellStyle headerTitleStyle = createHeaderTitleStyle(workbook);
            CellStyle integerStyle = createIntegerStyle(workbook);
            CellStyle textStyle = createTextStyle(workbook);
            CellStyle yesNoStyle = createTextStyle(workbook);
            CellStyle decimalStyle = createDecimalStyle(workbook);

            // Definição de todas as colunas
            String[][] columnHeaders = {
                // Linha 1: Tipo e tamanho
                {"(número - tamanho 6)", "(texto - tamanho 70)", "(texto - tamanho 2)", "Sim / Não", "(texto - tamanho 8)",
                 "(texto - tamanho 2)", "Comprado / Fabricado / Montado / Configurado / Virtual / Desmontado", "(texto e número - tamanho 8)", "Estoque Mínimo / Ponto de renovação / Não gera / Fórmula", "Estoque máximo / L.E.C. / Não calcula / Fórmula",
                 "(texto - tamanho 50)", "(texto - tamanho 40)", "(texto - tamanho 120)", "(texto - tamanho 500)", "Sim / Não", "(número - tamanho 10, 4)",
                 "(número - tamanho 10, 4)", "(texto - tamanho 15)", "Movimento de Estoque / Recebimento de Materiais", "(número - tamanho 6)", "(número - tamanho 3)", "(número - tamanho 15)",
                 "(número - tamanho 4)", "(número - tamanho 3)", "(texto - tamanho 1)", "(número - tamanho 6)", "NCM / Item", "(número - tamanho 3)",
                 "Sim / Não", "(texto - tamanho 4)", "Sim / Não", "Sim / Não", "(texto - tamanho 2)", "Sim / Não",
                 "(número - tamanho 3)", "Similar / Genérico / Ético ou de marca", "Sim / Não", "(texto e número - tamanho 7)", "(texto - tamanho 8)", "(número - tamanho 6)",
                 "(número - tamanho 6)", "PEPS / UEPS / Validade / Manual", "Sim / Não", "(número - tamanho 6)", "(número - tamanho 3)", "Sim / Não",
                 "(número - tamanho 15, 3)", "(número - tamanho 6, 2)", "(número - tamanho 3)", "(data DD/MM/YYYY)", "(número - tamanho 18, 2)", "Sim / Não",
                 "Sim / Não", "Sim / Não", "Sim / Não", "(número - tamanho 7)", "(número - tamanho 1)", "(texto e número - tamanho 4)",
                 "(número - tamanho 7)", "Sim / Não", "(número - tamanho 6)", "(texto - tamanho 10)", "(texto - tamanho 20)", "(número - tamanho 6)",
                 "(número - tamanho 6)", "Nível I / Nível II / Nível III / S1 / S2 / S3 / S4", "Normal / Severa / Atenuada", "Simples / Duplo / Múltiplo", "(texto - tamanho 8)", "(texto - tamanho 4)",
                 "(número - tamanho 7)", "(número - tamanho 6)", "(número - tamanho 6)", "(texto - tamanho 3)", "(número - tamanho 6)", "(número - tamanho 6)",
                 "(texto - tamanho 3)", "Dinâmico / MPS / MRP / Não Planejado / Reposição", "Discreta / Período fixo / Repetitiva / Fantasma / Quantidade fixa / Cativa", "Sim / Não", "Sim / Não", "Automática / Granel - Não / Requisitado / Manual / Ordem",
                 "(número - tamanho 6, 2)", "(número - tamanho 22)", "Sim / Não", "Dia / Mês / Ano", "(número - tamanho 22)", "Sim / Não",
                 "Sim / Não", "(número - tamanho 7)", "(número - tamanho 22)", "(texto e número - tamanho 8)", "Sim / Não", "Diário / Semanal / Mensal / Anual / Único",
                 "Sim / Não", "(número - tamanho 22)", "(número - tamanho 22)", "(número - tamanho 22)", "(número - tamanho 6)", "Inspeção / Qualificação / Ambos / Não sujeito",
                 "Sim / Não", "(texto e número - tamanho 1)", "(texto e número - tamanho 1)", "Sim / Não", "(número - tamanho 9, 3)", "(número - tamanho 6, 2)",
                 "(número - tamanho 5)", "Sim / Não", "(número - tamanho 20)", "(texto - tamanho 4000)", "(número - tamanho 10, 4)", "(número - tamanho 10, 4)",
                 "(número - tamanho 10, 4)"},
                // Linha 2: Nome técnico do campo
                {"GRU_IN_CODIGO", "PRO_ST_DESCRICAO", "PRO_ST_DEFITEM", "PRO_BO_GENERICO", "UNIP_ST_UNIDADE",
                 "PRO_CH_DEFFISCALITEM", "PRO_ST_ORIGEM", "UNI_ST_UNIDADE", "PRO_IN_GERASOLICITACAO", "PRO_IN_QTDECOMPRAR",
                 "PRO_ST_ALTERNATIVO", "PRO_ST_DESCRICAOPDV", "PRO_ST_DESCRICAONFE", "PRO_ST_NARRATIVA", "PRO_BO_TOTALIZADOC", "PRO_RE_PELIQUIDO",
                 "PRO_RE_PEBRUTO", "PRO_ST_UTILIZACAO", "PRO_CH_REALIZADOORC", "TRI_GRU_IN_CODIGO", "COS_IN_CODIGO", "NCM_IN_CODIGO",
                 "CEST_IN_CODIGO", "APL_IN_CODIGO", "PCD_ST_CODIGO", "TPCO_IN_CODIGO", "PRO_CH_DEFICMS", "CTR_IN_CODIGO",
                 "PRO_BO_CAT95", "PRO_ST_CAT95CODSEFAZ", "PRO_BO_SCANC", "PRO_BO_PRODEPE", "PRO_ST_CODIGOAPU", "PRO_BO_APUPISCOFINSCUM",
                 "APL_IN_CODIGO_MATTERC", "PRO_CH_TPPROD", "PRO_BO_SELO", "PRO_ST_CODIGOSELO", "PRO_ST_CODIGOINFORMACAOADD", "CON_GRU_IN_CODIGO",
                 "EST_GRU_IN_CODIGO", "PRO_IN_CRITERIOBUSCA", "PRO_ST_CESTOQUE", "PRO_IN_PESQDEM", "PRO_IN_MESESABC", "PRO_CH_ESTATISTICA",
                 "PRO_RE_QMINCOMPRA", "PRO_RE_MARGEMLUCRO", "IND_IN_CODIGO", "PRO_DT_VLOBJETIVO", "PRO_RE_VLOBJETIVO", "PRO_BO_TODOSALMOX",
                 "PRO_BO_CONTROLALOTES", "PRO_BO_CONTROLAVALIDADE", "PRO_BO_CONTROLADTENTRADA", "FOR_IN_CODIGO", "RFC_IN_CODIGO", "RAT_IN_CODIGODTFAB",
                 "RAT_IN_CODIGOAGREGACAO", "PRO_BO_FILTRAREFCODBARRA", "CLA_GRU_IN_CODIGO", "PRO_ST_ESPECIE", "PRO_ST_MARCA", "PRO_IN_VOLUME",
                 "QUA_GRU_IN_CODIGO", "PRO_IN_NIVELINSPECAO", "PRO_IN_CARACINSPECAO", "PRO_IN_PLANOINSPECAO", "UNQ_ST_UNIDADE", "FMTQ_ST_CODIGO",
                 "SKL_IN_CODIGO", "ALM_IN_CODIGOINSP", "LOC_IN_CODIGOINSP", "NAT_ST_CODIGOINSP", "ALM_IN_CODIGOREPR", "LOC_IN_CODIGOREPR",
                 "NAT_ST_CODIGOREPR", "PRO_ST_TIPODEMANDAMRP", "PRO_ST_POLITICAORDEM", "PRO_CH_ORIGEMFIXA", "PRO_CH_ORIGEMFIXACONSESTOQ", "PRO_ST_POLITICAREPOSICAO",
                 "PRO_RE_PERDA", "PRO_RE_FATOREXPLOSAO", "PRO_CH_UTILIZAMINMRP", "PRO_ST_CALCVALTPPER", "PRO_RE_CALCVALQTDREF", "PRO_CH_COMPOSICAOPLN",
                 "PRO_CH_ATRIBUTOPLNCOMPRADO", "RAT_IN_CODIGOCPSPLN", "PRO_RE_PESOESPECIFICO", "UNI_ST_UNIDADEPESOESPEC", "PRO_BO_UTILIZACALCPREVISAO", "PRO_CH_INTERVALOESPALHA",
                 "PRO_BO_UTILIZALEPESPALHA", "PRO_RE_LOTEPADRAOESPALHA", "PVM_IN_SEQUENCIA", "PRO_IN_TOLERANCIA", "MAN_GRU_IN_CODIGO", "PRO_ST_SINSPECAO",
                 "PRO_BO_TRANSFINSPESTOQ", "PRO_ST_TIPODADOS", "PRO_ST_TIPOTRIBUTA", "PRO_ST_COMISSIONADO", "PRO_RE_PESPECIFICO", "PRO_RE_ALIQIRRF",
                 "CLS_IN_REDUZIDO", "PRO_BO_VINCULACONTRATOS", "PRO_IN_NQA", "PRO_ST_ATRIBUTOPLNCOMPRADO", "PRO_RE_ALTURA", "PRO_RE_LARGURA",
                 "PRO_RE_COMPRIMENTO"},
                // Linha 3: Título amigável
                {"Código do grupo de produto *", "Descrição do item *", "Definição do item *", "Item genérico *", "Unidade de processo",
                 "Definição fiscal do produto (SPED fiscal) *", "Origem *", "Unidade de estoque *", "Gera solicitação *", "Qtde a comprar *",
                 "Código alternativo do item", "Descrição abreviada", "Descrição NF-e", "Narrativa sobre o item", "Totaliza documentos", "Peso líquido",
                 "Peso bruto", "Utilização do item", "Controle de orçamento de obra", "Grupo base - Tributação", "Código do serviço", "Código NCM",
                 "Código CEST", "Código da aplicação", "Código da situação tributária da tabela A", "Código da regra de PIS/COFINS", "ICMS definido por", "Código do tratamento ICMS",
                 "Arquivo magnético CAT95", "Código SEFAZ da CAT95", "Arquivo magnético SCANC", "Possui incentivo PRODEPE?", "Código de apuração SEF", "Incidência no regime cumulativo na forma de apuração do PIS/COFINS",
                 "Código da aplicação padrão utilizada em material de terceiro", "Tipo produto", "Controla Selo IPI?", "Código do selo de controle IPI", "Código da informação adicional", "Grupo base - Contábil",
                 "Grupo base - Estoque", "Tipo de busca de estoque", "Controla estoque", "Nº de meses da demanda", "Nº de meses da curva ABC", "Estatística",
                 "Quantidade mínima de compra", "Margem de lucro", "Índice do valor objetivo", "Data do valor objetivo", "Valor objetivo do produto", "Todos os almoxarifados/localizações",
                 "Controle lote de fornecimento?", "Controla data de validade?", "Controla data de entrada?", "Código da fórmula", "Característica do produto", "Código do atributo de data de fabricação",
                 "Código do atributo do Código de agregação", "Filtra característica do código de barra?", "Grupo base - Produto/Classe", "Espécie da embalagem", "Marca da embalagem", "Volume da embalagem",
                 "Grupo base - Qualidade", "Níveis de inspeção", "Características de inspeção", "Plano de amostragem", "Unidade de inspeção", "Formato de inspeção",
                 "Código Skip Lote", "Código almoxarifado de inspeção", "Código localização de inspeção", "Código natureza de inspeção", "Código almoxarifado de reprovação", "Código localização de reprovação",
                 "Código da natureza de reprovação", "Tipo de demanda MPS/MRP", "Política de ordem", "Origem fixa?", "Origem fixa considera estoque?", "Política de requisição",
                 "Percentual padrão de perda", "Fator de explosão", "Utiliza mínimo MPS/MRP", "Cálculo de validade (Tipo)", "Cálculo de validade (Quantidade)", "Desmembra produto por composição para planejamento",
                 "Desmembra produto por atributos para planejamento", "Código do atributo destinado para composição no estoque", "Peso específico do item", "Unidade de medida do peso específico do item", "Utiliza consumo histórico para cálculo de previsão", "Intervalo de espalhamento",
                 "Utiliza lotes econômicos para espalhamento", "Quantidade lote padrão para espalhamento", "Sequência do plano de variáveis do MPS", "Tolerância de recebimento da Ordem", "Grupo base - Manufatura", "Controle de Qualidade",
                 "Inspeção com transferência", "Nível Tipo Dados", "Nível Tipo Tributação", "Comissionado", "Peso Específico", "% IRRF",
                 "Cód. Critério Inspeção", "Vincular com Contratos", "Nível de qualidade aceitável (NQA)", "Atributos para planejamento", "Altura (Metros)", "Largura (Metros)",
                 "Comprimento (Metros)"}
            };

            // Validar que todos os arrays têm o mesmo tamanho e usar o tamanho real
            int totalColumns = Math.min(Math.min(columnHeaders[0].length, columnHeaders[1].length), columnHeaders[2].length);

            int rowIdx = 0;

            // Título da planilha
            // Título da planilha
            Row titleRow = sheet.createRow(rowIdx++);
            titleRow.setHeightInPoints(50); // Ajuste de altura da linha 1
            
            // Cria todas as células da primeira linha para aplicar o estilo, mas texto apenas na primeira
            for (int i = 0; i < totalColumns; i++) {
                Cell cell = titleRow.createCell(i);
                if (i == 0) {
                    cell.setCellValue("Importação de itens");
                }
                cell.setCellStyle(createTitleStyle(workbook));
            }
            // sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, totalColumns - 1)); // REMOVIDO

            // Linha em branco REMOVIDA
            // rowIdx++;

            // Primeira linha de cabeçalho (OBRIGATÓRIO) - apenas para campos obrigatórios
            Row headerRow1 = sheet.createRow(rowIdx++);
            for (int i = 0; i < totalColumns; i++) {
                Cell cell = headerRow1.createCell(i);
                // Campos obrigatórios são os primeiros 11
                if (i < 11) {
                    cell.setCellValue("OBRIGATÓRIO");
                } else {
                    cell.setCellValue("");
                }
                cell.setCellStyle(headerTopStyle);
            }

            // Segunda linha de cabeçalho (tipo e tamanho)
            Row headerRow2 = sheet.createRow(rowIdx++);
            for (int i = 0; i < totalColumns && i < columnHeaders[0].length; i++) {
                Cell cell = headerRow2.createCell(i);
                cell.setCellValue(columnHeaders[0][i]);
                cell.setCellStyle(headerTypeStyle);
            }

            // Terceira linha de cabeçalho (nome do campo técnico)
            Row headerRow3 = sheet.createRow(rowIdx++);
            for (int i = 0; i < totalColumns && i < columnHeaders[1].length; i++) {
                Cell cell = headerRow3.createCell(i);
                cell.setCellValue(columnHeaders[1][i]);
                cell.setCellStyle(headerFieldStyle);
            }

            // Quarta linha de cabeçalho (título amigável)
            Row headerRow4 = sheet.createRow(rowIdx++);
            for (int i = 0; i < totalColumns && i < columnHeaders[2].length; i++) {
                Cell cell = headerRow4.createCell(i);
                cell.setCellValue(columnHeaders[2][i]);
                // Destacar alguns campos em amarelo (conforme imagens)
                if (i == 13 || i == 14 || i == 20 || i == 21) { // Narrativa, Totaliza documentos, Código do serviço, Código NCM
                    cell.setCellStyle(headerTitleStyle);
                } else {
                    cell.setCellStyle(headerTitleStyle);
                }
            }

            // Dados
            for (ItemSolicitationEntity item : items) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;
                
                // Salvar o valor do grupo de produto para usar em outras colunas
                Integer itemGroupValue = item.getItemGroup();
                
                // 1. Código do grupo de produto
                if (itemGroupValue != null) {
                    createCell(row, col++, itemGroupValue, integerStyle);
                } else {
                    Cell blankCell = row.createCell(col++);
                    blankCell.setBlank();
                    blankCell.setCellStyle(integerStyle);
                }
                
                // 2. Descrição do item (vem do campo nome principal)
                String itemDescription = item.getMainName() != null ? item.getMainName() : "";
                row.createCell(col++).setCellValue(itemDescription);
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 3. Definição do item (tipo) - mapeia produto para "MA" e serviço para "SE"
                String itemType = "";
                if (item.getTipo() != null) {
                    String tipoLower = item.getTipo().toLowerCase().trim();
                    if (tipoLower.contains("produto")) {
                        itemType = "MA";
                    } else if (tipoLower.contains("serviço") || tipoLower.contains("servico")) {
                        itemType = "SE";
                    } else {
                        itemType = item.getTipo();
                    }
                }
                row.createCell(col++).setCellValue(itemType);
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 4. Item genérico (Sim/Não) - padrão NÃO
                row.createCell(col++).setCellValue("NÃO");
                row.getCell(col - 1).setCellStyle(yesNoStyle);
                
                // 5. Unidade de processo
                String unit = item.getUnidadeMedida() != null ? item.getUnidadeMedida() : "";
                row.createCell(col++).setCellValue(unit);
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 6. Definição fiscal do produto (SPED fiscal) - preenchida por definicaoFiscal
                String definicaoFiscal = item.getDefinicaoFiscal() != null ? item.getDefinicaoFiscal() : "";
                row.createCell(col++).setCellValue(definicaoFiscal);
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 7. Origem - sempre "COMPRADO"
                row.createCell(col++).setCellValue("COMPRADO");
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 8. Unidade de estoque - recebe o mesmo valor da Unidade de processo
                row.createCell(col++).setCellValue(unit);
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 9. Gera solicitação - sempre "NÃO GERA"
                row.createCell(col++).setCellValue("NÃO GERA");
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 10. Qtde a comprar - sempre "NÃO CALCULA"
                row.createCell(col++).setCellValue("NÃO CALCULA");
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 11. Código alternativo do item - vazio
                row.createCell(col++).setBlank();
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 12. Descrição abreviada - vazio
                row.createCell(col++).setBlank();
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 13. Descrição NF-e - vazio
                row.createCell(col++).setBlank();
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 14. Narrativa sobre o item - preenchida com o campo description
                String narrative = item.getDescription() != null ? item.getDescription() : "";
                row.createCell(col++).setCellValue(narrative);
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 15. Totaliza documentos - sempre "SIM"
                row.createCell(col++).setCellValue("SIM");
                row.getCell(col - 1).setCellStyle(yesNoStyle);
                
                // Preencher campos intermediários até chegar na coluna "Código do serviço" (posição 20)
                // 16-18: Campos intermediários (vazios)
                while (col < 19) {
                    row.createCell(col++).setBlank();
                    row.getCell(col - 1).setCellStyle(textStyle);
                }
                
                // 19. Grupo base - Tributação
                 // 19. Grupo base - Tributação
                 Cell blankCellTributacao = row.createCell(col++);
                 blankCellTributacao.setBlank();
                 blankCellTributacao.setCellStyle(integerStyle);
                
                // 20. Código do serviço - preenchido com o campo servico
                String servico = item.getServico() != null ? item.getServico() : "";
                row.createCell(col++).setCellValue(servico);
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 21. Código NCM - preenchido com o campo ncm
                String ncm = item.getNcm() != null ? item.getNcm().replaceAll("\\D", "") : "";
                row.createCell(col++).setCellValue(ncm);
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 22. Código CEST - vazio
                row.createCell(col++).setBlank();
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 23. Código da aplicação - preenchido com o campo aplicacao
                String aplicacao = item.getAplicacao() != null ? item.getAplicacao() : "";
                row.createCell(col++).setCellValue(aplicacao);
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 24. Código da situação tributária da tabela A - preenchido com codigoSituacaoTributaria
                String codigoSituacaoTributaria = item.getCodigoSituacaoTributaria() != null ? item.getCodigoSituacaoTributaria() : "";
                row.createCell(col++).setCellValue(codigoSituacaoTributaria);
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 25. Código da regra de PIS/COFINS - preenchido com codigoRegraPisCofins
                String codigoRegraPisCofins = item.getCodigoRegraPisCofins() != null ? item.getCodigoRegraPisCofins() : "";
                row.createCell(col++).setCellValue(codigoRegraPisCofins);
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 26. ICMS definido por - padrão "ITEM"
                row.createCell(col++).setCellValue("ITEM");
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // 27. Código do tratamento ICMS - preenchido com codigoTratamentoIcms
                String codigoTratamentoIcms = item.getCodigoTratamentoIcms() != null ? item.getCodigoTratamentoIcms() : "";
                row.createCell(col++).setCellValue(codigoTratamentoIcms);
                row.getCell(col - 1).setCellStyle(textStyle);
                
                // Preencher campos intermediários até chegar na coluna "Controla estoque" (posição 42)
                // 28-38: Campos intermediários (vazios)
                while (col < 39) {
                    row.createCell(col++).setBlank();
                    row.getCell(col - 1).setCellStyle(textStyle);
                }
                
                // 39. Grupo base - Contábil
                if (itemGroupValue != null) {
                    createCell(row, col++, itemGroupValue, integerStyle);
                } else {
                    Cell blankCell = row.createCell(col++);
                    blankCell.setBlank();
                    blankCell.setCellStyle(integerStyle);
                }

                // 40. Grupo base - Estoque
                if (itemGroupValue != null) {
                    createCell(row, col++, itemGroupValue, integerStyle);
                } else {
                    Cell blankCell = row.createCell(col++);
                    blankCell.setBlank();
                    blankCell.setCellStyle(integerStyle);
                }
                
                // 41: Campos intermediários (vazios) - Até 42
                while (col < 42) {
                    row.createCell(col++).setBlank();
                    row.getCell(col - 1).setCellStyle(textStyle);
                }
                
                // 42. Controla estoque - "SIM" se produto, "NÃO" se serviço
                String controlaEstoque = "NÃO";
                if (item.getTipo() != null) {
                    String tipoLower = item.getTipo().toLowerCase().trim();
                    if (tipoLower.contains("produto")) {
                        controlaEstoque = "SIM";
                    }
                }
                row.createCell(col++).setCellValue(controlaEstoque);
                row.getCell(col - 1).setCellStyle(yesNoStyle);
                
                // Preencher campos intermediários até chegar na coluna "Grupo base - Produto/Classe" (posição 60)
                // 43-59: Campos intermediários (vazios)
                while (col < 60) {
                    row.createCell(col++).setBlank();
                    row.getCell(col - 1).setCellStyle(textStyle);
                }
                
                // 60. Grupo base - Produto/Classe - recebe o mesmo valor do Código do grupo de produto
                if (itemGroupValue != null) {
                    createCell(row, col++, itemGroupValue, integerStyle);
                } else {
                    Cell blankCell = row.createCell(col++);
                    blankCell.setBlank();
                    blankCell.setCellStyle(integerStyle);
                }
                
                // 61-98. Campos adicionais (vazios por padrão, pois não existem na entidade)
                for (int i = col; i < totalColumns; i++) {
                    row.createCell(i).setBlank();
                    row.getCell(i).setCellStyle(textStyle);
                }
            }

            // Auto size das colunas
            for (int i = 0; i < totalColumns; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar Excel de Itens Cadastrados: " + e.getMessage(), e);
        }
    }

    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================
    
    private void createCell(Row row, int colIndex, Object value, CellStyle style) {
    Cell cell = row.createCell(colIndex);

    if (value == null) {
        cell.setBlank();
        return;
    }

    if (value instanceof Number) {
        cell.setCellValue(((Number) value).doubleValue());
    }
    else if (value instanceof LocalDate) {
        cell.setCellValue(java.sql.Date.valueOf((LocalDate) value));
    }
    else if (value instanceof LocalDateTime) {
        cell.setCellValue(java.sql.Timestamp.valueOf((LocalDateTime) value));
    }
    else {
        cell.setCellValue(value.toString());
    }

    if (style != null) {
        cell.setCellStyle(style);
    }
}

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        // #,##0.00 garante separador de milhar e 2 casas decimais
        style.setDataFormat(format.getFormat("R$ #,##0.00")); 
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));
        return style;
    }
    private CellStyle createIntegerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("0"));  // Apenas número inteiro
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("dd/MM/yyyy"));
        return style;
    }
    
    // NOVO MÉTODO PARA DATA E HORA
    private CellStyle createDateTimeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("dd/MM/yyyy HH:mm:ss"));
        return style;
    }

    // Estilos específicos para o Excel de itens cadastrados
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.TEAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderTopStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createHeaderTypeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createHeaderFieldStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        font.setItalic(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createHeaderTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createTextStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDecimalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.0000"));
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }
}