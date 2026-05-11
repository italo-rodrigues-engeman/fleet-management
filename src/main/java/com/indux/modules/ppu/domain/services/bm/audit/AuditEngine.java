package com.indux.modules.ppu.domain.services.bm.audit;

import com.indux.modules.ppu.domain.entities.ppu.AuditableConfig;
import com.indux.modules.ppu.domain.entities.rdo.*;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.audit.*;
import com.indux.modules.ppu.domain.strategy.OvertimeProjectionService;
import com.indux.modules.ppu.domain.strategy.time.ProjectionContext;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class AuditEngine {
    protected static final Locale PT_BR = Locale.of("pt", "BR");
    protected static final DecimalFormat DF_PT = (DecimalFormat) DecimalFormat.getInstance(PT_BR);
    private final OvertimeProjectionService overtimeProjectionService;
    private final PlatformAliasResolver platformResolver;

    static {
        DF_PT.setMinimumFractionDigits(2);
        DF_PT.setMaximumFractionDigits(2);
    }

    public AuditEngine(OvertimeProjectionService overtimeProjectionService, PlatformAliasResolver platformResolver) {
        this.overtimeProjectionService = overtimeProjectionService;
        this.platformResolver = platformResolver;
    }

    public List<AuditDivergence> audit(AuditContext ctx) {
        validateConfig(ctx.ppu());

        List<AuditDivergence> divergences = new ArrayList<>(checkHeaderSingle(ctx));
        List<SamcRange> samcRanges = buildSamcRanges(ctx.samcRows(), ctx.ppu().getAuditableConfig());
        Set<String> keysCoveredByRange = coveredSamcRangeKeysForSingleDate(samcRanges, ctx.rdo());

        List<AuditRDORow> rdoRows = mapRdoToAuditRows(ctx);
        if (!keysCoveredByRange.isEmpty()) {
            rdoRows = rdoRows.stream()
                    .filter(row -> !keysCoveredByRange.contains(normalize(row.auditName())))
                    .toList();
        }

        List<SAMCRow> samcRows = ctx.samcRows().stream()
                .filter(row -> !isSamcRangeRow(row))
                .toList();

        Map<String, AggregatedItem> rdoAggregated = aggregateRdoRows(rdoRows);
        Map<String, AggregatedItem> samcAggregated = aggregateSamcRows(samcRows, ctx.ppu().getAuditableConfig());

        divergences.addAll(compareAggregated(rdoAggregated, samcAggregated, ctx.rdo().getId(), ctx.rdo().getDate(),
                ctx.rdo().getPlatform(), buildAuditableLineToNameMap(ctx.ppu())));

        return divergences;
    }

    public List<AuditDivergence> audit(AuditBatchContext ctx, Set<String> allowedStatuses) {
        validateConfig(ctx.ppu());

        List<AuditDivergence> headerDivs = checkHeaderBatch(ctx, allowedStatuses);
        if (!headerDivs.isEmpty())
            return headerDivs;

        List<SAMCRow> samcOk = ctx.samcRows().stream()
                .filter(r -> {
                    String s = Optional.ofNullable(r.status()).orElse("");
                    if (s.isBlank())
                        return false;
                    String up = s.trim().toUpperCase(Locale.ROOT);
                    return allowedStatuses.contains(up);
                })
                .toList();

        AuditBatchContext sliced = ctx.withSlices(
                buildRdoSlices(ctx.rdos()),
                buildSamcSlices(samcOk));

        return performBulkLineItemChecks(sliced);
    }

    private void validateConfig(PPUEntity ppu) {
        if (ppu.getAuditableConfig() == null) {
            throw new IllegalStateException("Configuração de Auditoria (AuditableConfig) não encontrada na PPU.");
        }
    }

    private record AggregatedItem(String key, String displayName, double quantity, String sampleNumber) {
    }

    private record SamcRange(LocalDate start, LocalDate end, String platform, String key, List<SAMCRow> rows) {
    }

    private Map<String, AggregatedItem> aggregateRdoRows(List<AuditRDORow> rows) {
        Map<String, List<AuditRDORow>> grouped = rows.stream()
                .map(r -> new AuditRDORow(
                        r.id(),
                        normalize(r.number()),
                        normalize(r.auditName()),
                        r.name(),
                        r.quantity(),
                        null))
                .filter(r -> !r.auditName().isBlank())
                .collect(Collectors.groupingBy(AuditRDORow::auditName));

        return grouped.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> {
                    double sum = e.getValue().stream().mapToDouble(r -> parseQty(r.quantity())).sum();
                    String sampleNum = e.getValue().stream()
                            .map(AuditRDORow::number)
                            .filter(n -> !n.isBlank())
                            .findFirst().orElse("");
                    String dispName = e.getValue().stream()
                            .map(AuditRDORow::name)
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .findFirst().orElse(e.getKey());
                    return new AggregatedItem(e.getKey(), dispName, sum, sampleNum);
                }));
    }

    private Map<String, AggregatedItem> aggregateSamcRows(List<SAMCRow> rows, AuditableConfig config) {
        Map<String, Double> sums = new HashMap<>();
        Map<String, String> sampleNumbers = new HashMap<>();
        Map<String, String> displayNames = new HashMap<>();

        for (SAMCRow row : rows) {
            String key = resolveSamcKey(row, config);
            if (key == null || key.isBlank())
                continue;

            double qty = parseQty(row.quantidadeExecutada());
            sums.merge(key, qty, Double::sum);

            if (!sampleNumbers.containsKey(key)) {
                String num = normalize(row.numeroDetalhamento());
                if (!num.isBlank())
                    sampleNumbers.put(key, num);
            }

            if (!displayNames.containsKey(key)) {
                String desc = normalize(row.descricaoServico());
                displayNames.put(key, desc.isBlank() ? key : desc);
            }
        }

        return sums.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> new AggregatedItem(
                        e.getKey(),
                        displayNames.getOrDefault(e.getKey(), e.getKey()),
                        e.getValue(),
                        sampleNumbers.getOrDefault(e.getKey(), ""))));
    }

    private String resolveSamcKey(SAMCRow row, AuditableConfig config) {
        if (config.getColumnName() != null) {
            return normalize(row.column(config.getColumnName()));
        }
        if (config.getColumnIndex() != null) {
            return normalize(row.column(config.getColumnIndex()));
        }
        return null;
    }

    private List<AuditDivergence> compareAggregated(
            Map<String, AggregatedItem> rdoMap,
            Map<String, AggregatedItem> samcMap,
            String anchorId, LocalDate date, String platform,
            Map<String, String> ppuNames) {

        List<AuditDivergence> divergences = new ArrayList<>();
        Set<String> allKeys = new HashSet<>(rdoMap.keySet());
        allKeys.addAll(samcMap.keySet());

        for (String key : allKeys) {
            AggregatedItem rdoItem = rdoMap.get(key);
            AggregatedItem samcItem = samcMap.get(key);

            double rdoQty = rdoItem != null ? rdoItem.quantity : 0.0;
            double samcQty = samcItem != null ? samcItem.quantity : 0.0;

            if (Math.abs(rdoQty - samcQty) > 0.001) {
                String msg;
                if (rdoQty > 0 && samcQty == 0)
                    msg = "Linha ausente no SAMC";
                else if (rdoQty == 0 && samcQty > 0)
                    msg = "Linha ausente no RDO";
                else
                    msg = "Diferença em quantidade";

                String displayNum = rdoItem != null && !rdoItem.sampleNumber.isBlank() ? rdoItem.sampleNumber
                        : (samcItem != null ? samcItem.sampleNumber : "");

                String ppuName = ppuNames.get(key);
                String show;
                if (rdoItem != null) {
                    show = rdoItem.displayName;
                } else if (ppuName != null && !ppuName.isBlank()) {
                    show = ppuName;
                } else if (samcItem != null) {
                    show = samcItem.displayName;
                } else {
                    show = key;
                }

                String label = displayNum.isBlank()
                        ? show
                        : displayNum + " - " + show;

                divergences.add(new AuditDivergence(
                        anchorId,
                        label,
                        String.valueOf(rdoQty),
                        String.valueOf(samcQty),
                        date != null ? date.toString() : "-",
                        msg,
                        displayNum,
                        platform));
            }
        }
        return divergences;
    }

    // --- Headers & Batch Logic ---

    private List<AuditDivergence> checkHeaderSingle(AuditContext context) {
        return checkHeader(
                List.of(context.rdo()),
                context.samcRows(),
                context.rdo().getDate(),
                context.rdo().getDate());
    }

    private List<AuditDivergence> checkHeaderBatch(AuditBatchContext ctx, Set<String> allowedStatuses) {
        List<AuditDivergence> div = checkHeader(ctx.rdos(), ctx.samcRows(), ctx.start(), ctx.end());

        Set<String> nonAllowed = ctx.samcRows().stream()
                .map(SAMCRow::status)
                .filter(Objects::nonNull)
                .map(s -> s.trim().toUpperCase(Locale.ROOT))
                .filter(s -> !allowedStatuses.contains(s))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (!nonAllowed.isEmpty()) {
            RDOEntity anchor = ctx.rdos().isEmpty() ? null : ctx.rdos().getFirst();
            div.add(new AuditDivergence(
                    anchor != null ? anchor.getId() : "-",
                    "-",
                    String.join(", ", nonAllowed),
                    String.join(" | ", allowedStatuses),
                    anchor != null ? anchor.getDate().toString() : "-",
                    "Status do SAMC divergente.",
                    "-",
                    "-"));
        }
        return div;
    }

    private List<AuditDivergence> checkHeader(List<RDOEntity> rdos, List<SAMCRow> samcRows, LocalDate start,
                                              LocalDate end) {
        List<AuditDivergence> divergences = new ArrayList<>();
        if (rdos.isEmpty() && samcRows.isEmpty())
            return divergences;

        RDOEntity anchor = rdos.isEmpty() ? null : rdos.getFirst();
        String anchorId = anchor != null ? anchor.getId() : "-";
        String anchorPlatformRaw = anchor != null ? anchor.getPlatform()
                : (samcRows.isEmpty() ? null : samcRows.getFirst().local());
        String anchorPlatform = anchorPlatformRaw != null && !anchorPlatformRaw.isBlank()
                ? platformResolver.toSigla(anchorPlatformRaw)
                : "-";
        String anchorDate = anchor != null ? anchor.getDate().toString() : start.toString();

        String expectedContract = extractSingleValue(samcRows, SAMCRow::contrato);
        if (expectedContract != null && !rdos.isEmpty()) {
            boolean mismatch = rdos.stream().anyMatch(r -> {
                String c = r.getContract() != null ? (String) r.getContract().get("codeSap") : null;
                return c != null && !c.equals(expectedContract);
            });
            if (mismatch) {
                divergences.add(new AuditDivergence(anchorId, "-", "Vários/Desconhecido", expectedContract, anchorDate,
                        "Contrato não confere", "-", anchorPlatform));
            }
        }
        Set<String> samcPlatformsCanonical = collectPlatforms(
                samcRows,
                SAMCRow::local,
                platformResolver::toCanonical);

        Set<String> rdoPlatformsCanonical = collectPlatforms(
                rdos,
                RDOEntity::getPlatform,
                platformResolver::toCanonical);

        if (!samcPlatformsCanonical.equals(rdoPlatformsCanonical)) {
            Set<String> samcPlatformsDisplay = collectPlatforms(
                    samcRows,
                    SAMCRow::local,
                    platformResolver::toSigla);

            Set<String> rdoPlatformsDisplay = collectPlatforms(
                    rdos,
                    RDOEntity::getPlatform,
                    platformResolver::toSigla);

            divergences.add(new AuditDivergence(
                    anchorId,
                    "-",
                    String.join(",", rdoPlatformsDisplay),
                    String.join(",", samcPlatformsDisplay),
                    anchorDate,
                    "Plataformas divergentes",
                    "-",
                    anchorPlatform));
        }
        Set<LocalDate> samcDates = samcRows.stream()
                .flatMap(row -> Stream.of(samcStartDate(row), samcEndDate(row)))
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());

        boolean datesOutOfRange = samcDates.stream().anyMatch(d -> d.isBefore(start) || d.isAfter(end));
        if (datesOutOfRange) {
            divergences.add(new AuditDivergence(anchorId, "-",
                    start + " a " + end,
                    samcDates.toString(),
                    anchorDate, "Datas do SAMC fora do período", "-", anchorPlatform));
        }

        return divergences;
    }

    List<AuditDivergence> performBulkLineItemChecks(AuditBatchContext ctx) {
        List<AuditDivergence> divergences = new ArrayList<>();
        Map<String, String> ppuNames = buildAuditableLineToNameMap(ctx.ppu());
        List<SamcRange> samcRanges = buildSamcRanges(ctx.samcRows(), ctx.ppu().getAuditableConfig());
        Map<LocalDate, Map<String, Set<String>>> coveredKeys = buildCoveredSamcRangeKeys(samcRanges);
        Set<LocalDate> allDates = new TreeSet<>();
        allDates.addAll(ctx.samcByDatePlatform().keySet());
        allDates.addAll(ctx.rdoByDatePlatform().keySet());

        for (LocalDate date : allDates) {
            Map<String, List<SAMCRow>> samcByPlat = ctx.samcByDatePlatform().getOrDefault(date, Map.of());
            Map<String, List<RDOEntity>> rdoByPlat = ctx.rdoByDatePlatform().getOrDefault(date, Map.of());

            Set<String> allPlats = new TreeSet<>();
            allPlats.addAll(samcByPlat.keySet());
            allPlats.addAll(rdoByPlat.keySet());

            for (String platform : allPlats) {
                List<SAMCRow> samcSlice = samcByPlat.getOrDefault(platform, List.of());
                List<RDOEntity> rdoSlice = rdoByPlat.getOrDefault(platform, List.of());

                String anchorId = rdoSlice.isEmpty() ? "-" : rdoSlice.getFirst().getId();

                Set<String> keysCoveredByRange = coveredKeys
                        .getOrDefault(date, Map.of())
                        .getOrDefault(platform, Set.of());

                List<AuditRDORow> rdoRows = mapRdoSliceToAuditRows(rdoSlice, ctx.ppu());
                if (!keysCoveredByRange.isEmpty()) {
                    rdoRows = rdoRows.stream()
                            .filter(row -> !keysCoveredByRange.contains(normalize(row.auditName())))
                            .toList();
                }

                Map<String, AggregatedItem> rdoAgg = aggregateRdoRows(rdoRows);
                Map<String, AggregatedItem> samcAgg = aggregateSamcRows(samcSlice, ctx.ppu().getAuditableConfig());

                String platformDisplay = rdoSlice.isEmpty()
                        ? (samcSlice.isEmpty() ? platform : platformResolver.toSigla(samcSlice.getFirst().local()))
                        : platformResolver.toSigla(rdoSlice.getFirst().getPlatform());

                divergences.addAll(compareAggregated(rdoAgg, samcAgg, anchorId, date, platformDisplay, ppuNames));
            }
        }

        divergences.addAll(compareSamcRanges(samcRanges, ctx, ppuNames));
        return divergences;
    }

    // --- Helpers ---

    protected <T> String extractSingleValue(List<SAMCRow> rows, Function<SAMCRow, String> extractor) {
        Set<String> values = rows.stream()
                .map(extractor)
                .filter(v -> v != null && !v.isBlank())
                .collect(Collectors.toSet());
        return values.size() == 1 ? values.iterator().next() : null;
    }

    protected static String normalize(String s) {
        if (s == null)
            return "";
        String t = s.trim();
        if (t.isEmpty())
            return "";
        t = t.replace('\u00A0', ' ');
        t = t.replaceAll("\\s+", " ");
        return t;
    }

    protected String normalizePlatform(String s) {
        return platformResolver.toSigla(s);
    }

    private double parseQty(String value) {
        if (value == null || value.isBlank())
            return 0.0;
        try {
            return Double.parseDouble(value.replace(",", "."));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    protected Optional<LocalDate> parseDateFlexible(String raw) {
        return getLocalDate(raw);
    }

    private Optional<LocalDate> samcStartDate(SAMCRow row) {
        Optional<LocalDate> start = parseDateFlexible(row.dataInicio());
        return start.isPresent() ? start : samcEndDate(row);
    }

    private Optional<LocalDate> samcEndDate(SAMCRow row) {
        return parseDateFlexible(row.data());
    }

    private boolean isSamcRangeRow(SAMCRow row) {
        Optional<LocalDate> start = samcStartDate(row);
        Optional<LocalDate> end = samcEndDate(row);
        return start.isPresent() && end.isPresent() && start.get().isBefore(end.get());
    }

    @NotNull
    public static Optional<LocalDate> getLocalDate(String raw) {
        if (raw == null || raw.isBlank())
            return Optional.empty();
        String t = raw.trim();

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ISO_LOCAL_DATE);

        for (var fmt : formatters) {
            try {
                return Optional.of(LocalDate.parse(t, fmt));
            } catch (Exception ignored) {
            }
        }
        return Optional.empty();
    }

    private Map<LocalDate, Map<String, List<SAMCRow>>> buildSamcSlices(List<SAMCRow> rows) {
        Map<LocalDate, Map<String, List<SAMCRow>>> out = new TreeMap<>();
        for (SAMCRow row : rows) {
            if (isSamcRangeRow(row))
                continue;
            Optional<LocalDate> dOpt = samcEndDate(row);
            if (dOpt.isEmpty())
                continue;
            LocalDate date = dOpt.get();
            String rawPlatform = row.local();
            if (rawPlatform == null || rawPlatform.isBlank())
                continue;
            String platformCanonical = platformResolver.toCanonical(rawPlatform);
            if (platformCanonical.isBlank())
                continue;
            out.computeIfAbsent(date, x -> new HashMap<>())
                    .computeIfAbsent(platformCanonical, x -> new ArrayList<>())
                    .add(row);
        }
        return out;
    }

    private List<SamcRange> buildSamcRanges(List<SAMCRow> rows, AuditableConfig config) {
        Map<String, List<SAMCRow>> grouped = new LinkedHashMap<>();
        Map<String, LocalDate> starts = new HashMap<>();
        Map<String, LocalDate> ends = new HashMap<>();
        Map<String, String> platforms = new HashMap<>();
        Map<String, String> keys = new HashMap<>();

        for (SAMCRow row : rows) {
            Optional<LocalDate> startOpt = samcStartDate(row);
            Optional<LocalDate> endOpt = samcEndDate(row);
            if (startOpt.isEmpty() || endOpt.isEmpty() || !startOpt.get().isBefore(endOpt.get()))
                continue;

            String platform = platformResolver.toCanonical(row.local());
            if (platform.isBlank())
                continue;

            String key = resolveSamcKey(row, config);
            if (key == null || key.isBlank())
                continue;

            String normalizedKey = normalize(key);
            String groupKey = startOpt.get() + "|" + endOpt.get() + "|" + platform + "|" + normalizedKey;
            grouped.computeIfAbsent(groupKey, ignored -> new ArrayList<>()).add(row);
            starts.putIfAbsent(groupKey, startOpt.get());
            ends.putIfAbsent(groupKey, endOpt.get());
            platforms.putIfAbsent(groupKey, platform);
            keys.putIfAbsent(groupKey, normalizedKey);
        }

        return grouped.entrySet().stream()
                .map(e -> new SamcRange(
                        starts.get(e.getKey()),
                        ends.get(e.getKey()),
                        platforms.get(e.getKey()),
                        keys.get(e.getKey()),
                        e.getValue()))
                .toList();
    }

    private Map<LocalDate, Map<String, Set<String>>> buildCoveredSamcRangeKeys(List<SamcRange> ranges) {
        Map<LocalDate, Map<String, Set<String>>> out = new TreeMap<>();
        for (SamcRange range : ranges) {
            for (LocalDate date = range.start(); !date.isAfter(range.end()); date = date.plusDays(1)) {
                out.computeIfAbsent(date, ignored -> new HashMap<>())
                        .computeIfAbsent(range.platform(), ignored -> new HashSet<>())
                        .add(range.key());
            }
        }
        return out;
    }

    private Set<String> coveredSamcRangeKeysForSingleDate(List<SamcRange> ranges, RDOEntity rdo) {
        if (rdo == null || rdo.getDate() == null)
            return Set.of();

        String platform = platformResolver.toCanonical(rdo.getPlatform());
        if (platform.isBlank())
            return Set.of();

        return ranges.stream()
                .filter(range -> !rdo.getDate().isBefore(range.start()))
                .filter(range -> !rdo.getDate().isAfter(range.end()))
                .filter(range -> Objects.equals(platform, range.platform()))
                .map(SamcRange::key)
                .collect(Collectors.toSet());
    }

    private List<AuditDivergence> compareSamcRanges(
            List<SamcRange> ranges,
            AuditBatchContext ctx,
            Map<String, String> ppuNames) {

        List<AuditDivergence> divergences = new ArrayList<>();

        for (SamcRange range : ranges) {
            List<RDOEntity> rdoSlice = ctx.rdos().stream()
                    .filter(rdo -> rdo.getDate() != null
                            && !rdo.getDate().isBefore(range.start())
                            && !rdo.getDate().isAfter(range.end())
                            && Objects.equals(platformResolver.toCanonical(rdo.getPlatform()), range.platform()))
                    .toList();

            Map<String, AggregatedItem> rdoAgg = filterAggregatedByKey(
                    aggregateRdoRows(mapRdoSliceToAuditRows(rdoSlice, ctx.ppu())),
                    range.key());
            Map<String, AggregatedItem> samcAgg = aggregateSamcRows(range.rows(), ctx.ppu().getAuditableConfig());

            String anchorId = rdoSlice.isEmpty() ? "-" : rdoSlice.getFirst().getId();
            String platformDisplay = rdoSlice.isEmpty()
                    ? platformResolver.toSigla(range.rows().getFirst().local())
                    : platformResolver.toSigla(rdoSlice.getFirst().getPlatform());

            divergences.addAll(compareAggregated(
                    rdoAgg,
                    samcAgg,
                    anchorId,
                    range.start(),
                    platformDisplay,
                    ppuNames));
        }

        return divergences;
    }

    private Map<String, AggregatedItem> filterAggregatedByKey(Map<String, AggregatedItem> source, String key) {
        AggregatedItem item = source.get(key);
        return item == null ? Map.of() : Map.of(key, item);
    }

    private Map<LocalDate, Map<String, List<RDOEntity>>> buildRdoSlices(List<RDOEntity> rdos) {
        Map<LocalDate, Map<String, List<RDOEntity>>> out = new TreeMap<>();
        for (RDOEntity rdo : rdos) {
            LocalDate date = rdo.getDate();
            String rawPlatform = rdo.getPlatform();
            if (date == null || rawPlatform == null || rawPlatform.isBlank())
                continue;
            String platformCanonical = platformResolver.toCanonical(rawPlatform);
            if (platformCanonical.isBlank())
                continue;
            out.computeIfAbsent(date, x -> new HashMap<>())
                    .computeIfAbsent(platformCanonical, x -> new ArrayList<>())
                    .add(rdo);
        }
        return out;
    }

    List<AuditRDORow> mapRdoToAuditRows(AuditContext context) {
        return mapRdoSliceToAuditRows(List.of(context.rdo()), context.ppu());
    }

    private Map<String, String> buildAuditableLineToNameMap(PPUEntity ppu) {
        return Stream.of(
                        Optional.ofNullable(ppu.getServices()).orElse(List.of()),
                        Optional.ofNullable(ppu.getSteelCables()).orElse(List.of()),
                        Optional.ofNullable(ppu.getEquipments()).orElse(List.of()),
                        Optional.ofNullable(ppu.getAccessoryKits()).orElse(List.of()),
                        Optional.ofNullable(ppu.getLines()).orElse(List.of()))
                .flatMap(Collection::stream)
                .filter(l -> l.resolveAuditableLine() != null
                        && !l.resolveAuditableLine().isBlank())
                .collect(Collectors.toMap(
                        l -> normalize(l.resolveAuditableLine()),
                        l -> normalize(l.getName()),
                        (a, b) -> a));
    }

    private List<AuditRDORow> mapRdoSliceToAuditRows(List<RDOEntity> rdoSlice, PPUEntity ppu) {
        Map<String, String> idToAuditableName = Stream.of(
                        Optional.ofNullable(ppu.getServices()).orElse(List.of()),
                        Optional.ofNullable(ppu.getSteelCables()).orElse(List.of()),
                        Optional.ofNullable(ppu.getEquipments()).orElse(List.of()),
                        Optional.ofNullable(ppu.getAccessoryKits()).orElse(List.of()),
                        Optional.ofNullable(ppu.getLines()).orElse(List.of())).flatMap(Collection::stream)
                .collect(Collectors.toMap(
                        line -> normalize(line.getId()),
                        line -> {
                            String platform = rdoSlice.isEmpty() ? null : rdoSlice.getFirst().getPlatform();
                            return normalize(line.resolveAuditableLine(platform));
                        },
                        (a, b) -> a));

        Map<String, ServiceLine> serviceById = Optional.ofNullable(ppu.getServices())
                .orElse(List.of())
                .stream()
                .collect(Collectors.toMap(ServiceLine::getId, Function.identity(), (a, b) -> a));

        List<AuditRDORow> rows = new ArrayList<>();

        rows.addAll(mapServicesFromSlice(rdoSlice, ppu, idToAuditableName, serviceById));
        rows.addAll(mapSimpleItemsFromSlice(rdoSlice, idToAuditableName));
        return rows;
    }

    private List<AuditRDORow> mapServicesFromSlice(List<RDOEntity> rdoSlice, PPUEntity ppu,
                                                   Map<String, String> idToAuditableName, Map<String, ServiceLine> serviceById) {
        List<RDOServiceEntity> baseServices = rdoSlice.stream()
                .flatMap(rdo -> Optional.ofNullable(rdo.getServices()).orElse(List.of()).stream())
                .filter(Objects::nonNull)
                .toList();

        if (baseServices.isEmpty())
            return List.of();

        var projected = overtimeProjectionService.project(
                baseServices,
                Optional.ofNullable(ppu.getServices()).orElse(List.of()),
                ProjectionContext.AUDIT);

        return projected.working().stream()
                .collect(Collectors.groupingBy(RDOServiceEntity::getServiceID))
                .entrySet().stream()
                .map(e -> {
                    String id = e.getKey();
                    List<RDOServiceEntity> list = e.getValue();
                    double total = list.stream().mapToDouble(s -> Optional.ofNullable(s.getValueMeasured()).orElse(0.0))
                            .sum();

                    ServiceLine line = serviceById.get(id);
                    String auditName = idToAuditableName.getOrDefault(id, "");
                    String number = list.getFirst().getServiceNumber();

                    return new AuditRDORow(id, number, auditName, list.getFirst().getServiceName(),
                            String.valueOf(total), false);
                })
                .toList();
    }

    private List<AuditRDORow> mapSimpleItemsFromSlice(List<RDOEntity> rdoSlice, Map<String, String> idToAuditableName) {
        List<AuditRDORow> rows = new ArrayList<>();

        // Steel Cables
        rdoSlice.stream().flatMap(r -> Optional.ofNullable(r.getSteelCable()).orElse(List.of()).stream())
                .forEach(sc -> rows.add(new AuditRDORow(sc.id(), sc.numero(),
                        idToAuditableName.getOrDefault(sc.id(), sc.nome()), sc.nome(),
                        String.valueOf(Optional.ofNullable(sc.quantidade()).orElse(0.0)), false)));

        // Equipments
        rdoSlice.stream().flatMap(r -> Optional.ofNullable(r.getEquipments()).orElse(List.of()).stream())
                .collect(Collectors.groupingBy(eq -> eq.getEquipmentPPUId() + "|" + normalize(eq.getNumber())))
                .forEach((k, eqList) -> {
                    String id = k.split("\\|")[0];
                    long qty = eqList.stream().flatMap(e -> e.getCheckers().stream())
                            .filter(EquipmentChecker::operacional).count();
                    rows.add(new AuditRDORow(id, eqList.getFirst().getNumber(),
                            idToAuditableName.getOrDefault(id, eqList.getFirst().getName()),
                            eqList.getFirst().getName(), String.valueOf(qty), false));
                });

        // Kits
        rdoSlice.stream().flatMap(r -> Optional.ofNullable(r.getAccessoryKits()).orElse(List.of()).stream())
                .forEach(k -> rows.add(new AuditRDORow(k.lineID(), k.numero(),
                        idToAuditableName.getOrDefault(k.lineID(), k.nome()), k.nome(),
                        String.valueOf(Optional.ofNullable(k.quantidade()).orElse(0.0)), false)));

        // Pure Lines
        rdoSlice.stream().flatMap(r -> Optional.ofNullable(r.getLines()).orElse(List.of()).stream())
                .forEach(l -> rows.add(new AuditRDORow(l.getParentId(), l.getParentNumber(),
                        idToAuditableName.getOrDefault(l.getParentId(), l.getParentName()), l.getParentName(),
                        String.valueOf(Optional.ofNullable(l.getValueMeasured()).orElse(0.0)), false)));

        return rows;
    }

    private <T> Set<String> collectPlatforms(
            Collection<T> source,
            Function<T, String> rawExtractor,
            Function<String, String> resolver) {
        return source.stream()
                .map(rawExtractor)
                .filter(Objects::nonNull)
                .map(resolver)
                .filter(s -> s != null && !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
