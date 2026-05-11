package com.indux.modules.ppu.application.services.rdo.operation.audit;

import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.domain.entities.rdo.EquipmentChecker;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.ppu.ServiceType;
import com.indux.modules.ppu.domain.entities.rdo.RDOEquipment;
import com.indux.modules.ppu.domain.entities.rdo.RDOServiceEntity;
import com.indux.modules.ppu.domain.entities.rdo.audit.AuditRDORow;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GenerateAuditExcelService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private final RDORepository rdoRepository;
    private final PPURepository ppuRepository;

    public GenerateAuditExcelService(
            RDORepository rdoRepository,
            PPURepository ppuRepository
    ) {
        this.rdoRepository = rdoRepository;
        this.ppuRepository = ppuRepository;
    }

    /**
     * Gera arquivo Excel de auditoria para um único RDO
     */
    public byte[] generateForSingleRDO(String rdoId) {
        RDOEntity rdo = rdoRepository.findById(rdoId)
                .orElseThrow(() -> new ModuleNotFoundFailure("RDO não encontrado."));
        
        PPUEntity ppu = ppuRepository.findById(rdo.getPpuId())
                .orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada."));

        return generateExcelFile(List.of(rdo), ppu);
    }

    /**
     * Gera arquivo Excel de auditoria para múltiplos RDOs
     */
    public byte[] generateForMultipleRDOs(List<String> rdoIds) {
        List<RDOEntity> rdos = rdoRepository.findAllById(rdoIds);
        
        if (rdos.isEmpty()) {
            throw new ModuleNotFoundFailure("Nenhum RDO encontrado.");
        }

        // Assume que todos os RDOs são da mesma PPU (pegar do primeiro)
        PPUEntity ppu = ppuRepository.findById(rdos.get(0).getPpuId())
                .orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada."));

        return generateExcelFile(rdos, ppu);
    }

    /**
     * Gera arquivo Excel de auditoria para período específico (usado pelo BM)
     */
    public byte[] generateForDateRange(String ppuId, LocalDate start, LocalDate end, List<String> platforms) {
        PPUEntity ppu = ppuRepository.findById(ppuId)
                .orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada."));

        // Se não forneceram plataformas, usa as da PPU
        List<String> targetPlatforms = (platforms == null || platforms.isEmpty()) 
                ? ppu.getPlatforms() 
                : platforms;

        List<RDOEntity> rdos = new ArrayList<>();
        for (String platform : targetPlatforms) {
            rdos.addAll(rdoRepository.findByPlatformAndDateRange(platform, start, end));
        }

        if (rdos.isEmpty()) {
            throw new ModuleNotFoundFailure("Nenhum RDO encontrado para o período especificado.");
        }

        return generateExcelFile(rdos, ppu);
    }

    /**
     * Mapeia um RDO para AuditRDORow (mesma lógica do AuditSAMC)
     */
    private List<AuditRDORow> mapRdoToAuditRows(RDOEntity rdo, PPUEntity ppu) {
        String platform = rdo.getPlatform();
        Map<String, String> idToAuditableName = Stream.of(
                        Optional.ofNullable(ppu.getServices()).orElse(List.of()),
                        Optional.ofNullable(ppu.getSteelCables()).orElse(List.of()),
                        Optional.ofNullable(ppu.getEquipments()).orElse(List.of()),
                        Optional.ofNullable(ppu.getAccessoryKits()).orElse(List.of())
                ).flatMap(Collection::stream)
                .collect(Collectors.toMap(
                        line -> normalize(line.getId()),
                        line -> normalize(line.resolveAuditableLine(platform)),
                        (a, b) -> a
                ));

        Map<String, ServiceLine> serviceById = Optional.ofNullable(ppu.getServices())
                .orElse(List.of())
                .stream()
                .collect(Collectors.toMap(ServiceLine::getId, Function.identity(), (a, b) -> a));

        List<AuditRDORow> rows = new ArrayList<>();
        rows.addAll(mapServices(rdo, idToAuditableName, serviceById));
        rows.addAll(mapSteelCables(rdo, idToAuditableName));
        rows.addAll(mapEquipments(rdo, idToAuditableName));
        rows.addAll(mapAccessoryKits(rdo, idToAuditableName));
        
        return rows;
    }

    // ===== Métodos de Mapeamento (copiados do AuditSAMC) =====

    private List<AuditRDORow> mapServices(RDOEntity rdo, Map<String, String> idToAuditableName, Map<String, ServiceLine> serviceById) {
        return Optional.ofNullable(rdo.getServices()).orElse(List.of())
                .stream()
                .collect(Collectors.groupingBy(RDOServiceEntity::getServiceID, Collectors.counting()))
                .entrySet().stream()
                .map(entry -> {
                    String id = entry.getKey();
                    long qtd = entry.getValue();

                    RDOServiceEntity ref = rdo.getServices().stream()
                            .filter(sv -> sv.getServiceID().equals(id))
                            .findFirst().orElse(null);
                    if (ref == null) return null;

                    ServiceLine line = serviceById.get(id);
                    boolean isFullTime = line != null && ServiceType.VINTEQUATROHORAS.equals(line.getType());
                    double adjustedQtd = isFullTime ? qtd * 0.5 : qtd;

                    return new AuditRDORow(
                            id,
                            normalize(ref.getServiceNumber()),
                            idToAuditableName.get(id),
                            ref.getServiceName(),
                            String.valueOf(adjustedQtd),
                            isFullTime
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }

    private List<AuditRDORow> mapSteelCables(RDOEntity rdo, Map<String, String> idToAuditableName) {
        return Optional.ofNullable(rdo.getSteelCable()).orElse(List.of())
                .stream()
                .map(sc -> {
                    String id = sc.id();
                    String auditable = idToAuditableName.get(sc.lineID());
                    String number = normalize(sc.numero());
                    if ("0".equals(number)) number = null;
                    double qtd = Optional.ofNullable(sc.quantidade()).orElse(0.0);

                    return new AuditRDORow(
                            id, number, auditable, sc.nome(),
                            String.valueOf(qtd), null
                    );
                })
                .toList();
    }

    private List<AuditRDORow> mapEquipments(RDOEntity rdo, Map<String, String> idToAuditableName) {
        return Optional.ofNullable(rdo.getEquipments()).orElse(List.of())
                .stream()
                .collect(Collectors.groupingBy(eq -> eq.getEquipmentPPUId() + "|" + normalize(eq.getNumber())))
                .entrySet().stream()
                .map(entry -> {
                    String[] parts = entry.getKey().split("\\|");
                    String id = parts[0];
                    String num = parts.length > 1 ? parts[1] : null;
                    List<RDOEquipment> eqList = entry.getValue();

                    String auditable = idToAuditableName.getOrDefault(id, normalize(eqList.getFirst().getName()));
                    long qtd = eqList.stream()
                            .flatMap(eq -> Optional.ofNullable(eq.getCheckers()).orElse(List.of()).stream())
                            .filter(EquipmentChecker::operacional)
                            .count();

                    String number = !"0".equals(num) ? num : null;

                    return new AuditRDORow(
                            id, number, auditable,
                            eqList.getFirst().getName(),
                            String.valueOf(qtd), null
                    );
                })
                .toList();
    }

    private List<AuditRDORow> mapAccessoryKits(RDOEntity rdo, Map<String, String> idToAuditableName) {
        return Optional.ofNullable(rdo.getAccessoryKits()).orElse(List.of())
                .stream()
                .map(kit -> {
                    String id = kit.lineID();
                    String auditable = idToAuditableName.getOrDefault(id, normalize(kit.nome()));
                    String number = normalize(kit.numero());
                    if ("0".equals(number)) number = null;
                    var qtd = Optional.ofNullable(kit.quantidade()).orElse(1.0);

                    return new AuditRDORow(
                            id, number, auditable,
                            kit.nome(),
                            String.valueOf(qtd), null
                    );
                })
                .toList();
    }

    // ===== Geração do arquivo Excel =====

    private byte[] generateExcelFile(List<RDOEntity> rdos, PPUEntity ppu) {
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Auditoria RDO");

            // Estilos
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);

            // Headers (seguindo o padrão do SAMC)
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Contrato",
                "Local", 
                "Período Fim",
                "Número detalhamento EAC",
                "Descrição do Serviço",
                "Qntd Executada",
                "Status RO"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Dados
            int rowNum = 1;
            
            String contractCode = rdos.isEmpty() ? "" : 
                    String.valueOf(rdos.get(0).getContract().getOrDefault("codeSap", ""));

            // Processar cada RDO
            for (RDOEntity rdo : rdos) {
                List<AuditRDORow> rows = mapRdoToAuditRows(rdo, ppu);
                LocalDate date = rdo.getDate();
                String platform = rdo.getPlatform();
                
                for (AuditRDORow row : rows) {
                    // Pular linhas sem quantidade ou com quantidade zero
                    double qty = parseQuantity(row.quantity());
                    if (qty <= 0.001) continue;

                    Row dataRow = sheet.createRow(rowNum++);
                    
                    // Contrato
                    createCell(dataRow, 0, contractCode, dataStyle);
                    
                    // Local (Plataforma)
                    createCell(dataRow, 1, platform, dataStyle);
                    
                    // Período Fim (Data)
                    createCell(dataRow, 2, date.format(DATE_FORMATTER), dataStyle);
                    
                    // Número detalhamento EAC
                    String number = row.number();
                    if (number == null || number.isBlank() || "0".equals(number)) {
                        number = ""; // Deixa vazio se não tiver número
                    }
                    createCell(dataRow, 3, number, dataStyle);
                    
                    // Descrição do Serviço (usa auditableLine se disponível, senão usa name)
                    String description = row.auditName() != null && !row.auditName().isBlank() 
                            ? row.auditName() 
                            : row.name();
                    createCell(dataRow, 4, description, dataStyle);
                    
                    // Qntd Executada
                    createCell(dataRow, 5, formatQuantity(qty), dataStyle);
                    
                    // Status RO
                    createCell(dataRow, 6, "APROVADO", dataStyle);
                }
            }

            // Auto-ajustar colunas
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            throw new ModuleFailure("Erro ao gerar arquivo Excel: " + e.getMessage());
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value != null ? value : "");
        cell.setCellStyle(style);
    }

    private double parseQuantity(String quantity) {
        if (quantity == null || quantity.isBlank()) return 0.0;
        try {
            return Double.parseDouble(quantity.replace(",", "."));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String formatQuantity(double quantity) {
        // Formata com vírgula como decimal (padrão PT-BR)
        return String.format(Locale.forLanguageTag("pt-BR"), "%.2f", quantity);
    }

    protected static String normalize(String s) { return s == null ? "" : s.trim(); }
}

