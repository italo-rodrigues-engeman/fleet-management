package com.indux.modules.ppu.application.services.rdo.scripts;

import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.entities.rdo.*;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.ppu.LinePPU;
import com.indux.modules.ppu.domain.entities.ppu.SteelCableLine;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.domain.repositories.mongo.RDORepository;
import com.indux.modules.ppu.domain.strategy.LineStrategy;
import com.indux.modules.ppu.domain.strategy.LineStrategyRegistry;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ScriptsVersion {
    private final RDORepository rdoRepository;
    private final PPURepository ppuRepository;
    private final LineStrategyRegistry lineStrategyRegistry;

    public ScriptsVersion(RDORepository rdoRepository, PPURepository ppuRepository,
            LineStrategyRegistry lineStrategyRegistry) {
        this.rdoRepository = rdoRepository;
        this.ppuRepository = ppuRepository;
        this.lineStrategyRegistry = lineStrategyRegistry;
    }

    public void fixEquipments() {
        var allRdos = rdoRepository.findAll();
        var ppuById = ppuRepository.findAll().stream()
                .collect(Collectors.toMap(PPUEntity::getId, Function.identity()));

        for (var rdo : allRdos) {
            var ppu = ppuById.get(rdo.getPpuId());
            if (ppu == null)
                continue;

            var newEquipments = new ArrayList<RDOEquipment>();
            var desentupidoraPPU = findPpuEquipment(ppu, "desentupidora");
            var lavaJatoPPU = findPpuEquipment(ppu, "lava jato");

            // Agrupa os equipamentos existentes em 2 grupos
            var desentupidoras = new ArrayList<EquipmentChecker>();
            var lavaJatos = new ArrayList<EquipmentChecker>();

            Optional.ofNullable(rdo.getEquipments()).orElse(List.of()).forEach(eq -> {
                String name = eq.getName().toLowerCase();
                var checkers = Optional.ofNullable(eq.getCheckers()).orElse(List.of());
                if (name.contains("desentupidora")) {
                    desentupidoras.addAll(checkers);
                } else if (name.contains("lava jato")) {
                    lavaJatos.addAll(checkers);
                }
            });

            if (!desentupidoras.isEmpty() && desentupidoraPPU != null) {
                newEquipments.add(buildEquipmentFromPPU(desentupidoraPPU, desentupidoras));
            }
            if (!lavaJatos.isEmpty() && lavaJatoPPU != null) {
                newEquipments.add(buildEquipmentFromPPU(lavaJatoPPU, lavaJatos));
            }

            rdo.setEquipments(newEquipments);
            rdoRepository.save(rdo);

            System.out.printf("[RDO FIX] %s → %d equipamentos reconstruídos%n",
                    rdo.getId(), newEquipments.size());
        }
    }

    private LinePPU findPpuEquipment(PPUEntity ppu, String keyword) {
        return Optional.ofNullable(ppu.getEquipments()).orElse(List.of()).stream()
                .filter(eq -> eq.getName().toLowerCase().contains(keyword))
                .findFirst()
                .orElse(null);
    }

    private RDOEquipment buildEquipmentFromPPU(LinePPU ppuLine, List<EquipmentChecker> checkers) {
        var newId = UUID.randomUUID().toString();
        var fixedCheckers = checkers.stream()
                .map(chk -> new EquipmentChecker(
                        UUID.randomUUID().toString(),
                        chk.numeroPatrimonio(),
                        chk.fabricante(),
                        chk.descricaoModelo(),
                        chk.operacional(),
                        chk.observacao()))
                .toList();

        var newEquipment = new RDOEquipment();
        newEquipment.setId(newId);
        newEquipment.setEquipmentPPUId(ppuLine.getId());
        newEquipment.setNumber(ppuLine.getGenericNumber());
        newEquipment.setName(ppuLine.getName());
        newEquipment.setCheckers(fixedCheckers);

        return newEquipment;
    }

    public void fixLinesAddingPPUID() {
        var allRdos = rdoRepository.findAll();
        var allPpus = ppuRepository.findAll();

        // Mapa PPUId -> PPUEntity
        Map<String, PPUEntity> ppuById = allPpus.stream()
                .collect(Collectors.toMap(PPUEntity::getId, Function.identity()));

        for (var rdo : allRdos) {
            var myPPU = ppuById.get(rdo.getPpuId());
            if (myPPU == null)
                continue;

            String platform = rdo.getPlatform();
            if (platform == null)
                continue;

            // --- Atualizar Equipamentos ---
            Optional.ofNullable(rdo.getEquipments()).orElse(List.of()).forEach(equip -> {
                var match = myPPU.getEquipments().stream()
                        .filter(ppuEq -> ppuEq.getName().equalsIgnoreCase(equip.getName()))
                        .findFirst();

                match.ifPresent(ppuEq -> equip.setEquipmentPPUId(ppuEq.getId()));
            });

            // --- Atualizar Cabos de Aço ---
            List<SteelCableChecker> updatedCables = Optional.ofNullable(rdo.getSteelCable())
                    .orElse(List.of())
                    .stream()
                    .map(cable -> {
                        var match = myPPU.getSteelCables().stream()
                                .filter(ppuCable -> ppuCable.getPlatforms() != null
                                        && ppuCable.getPlatforms().contains(platform))
                                .filter(ppuCable -> ppuCable.getName().equalsIgnoreCase(cable.nome()))
                                .findFirst();
                        if (match.isPresent()) {
                            return cable.copyWithLineID(match.get().getId());
                        }
                        return cable;
                    })
                    .toList();
            rdo.setSteelCable(updatedCables);

            // --- Atualizar Kits de Acessórios ---
            List<AccessoryKitDTO> updatedKits = Optional.ofNullable(rdo.getAccessoryKits())
                    .orElse(List.of())
                    .stream()
                    .map(kit -> {
                        var match = myPPU.getAccessoryKits().stream()
                                .filter(ppuKit -> ppuKit.getName().equalsIgnoreCase(kit.nome()))
                                .findFirst();
                        if (match.isPresent()) {
                            return kit.copyWithID(match.get().getId());
                        }
                        return kit;
                    })
                    .toList();
            rdo.setAccessoryKits(updatedKits);

            rdoRepository.save(rdo);
        }
    }

    public void fixAllPpus() {
        var ppus = ppuRepository.findAll();
        for (PPUEntity ppu : ppus) {
            fixSinglePpu(ppu);
        }
    }

    public void fixSinglePpu(String ppuId) {
        var ppu = ppuRepository.findById(ppuId)
                .orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada: " + ppuId));
        fixSinglePpu(ppu);
    }

    private void fixSinglePpu(PPUEntity ppu) {
        var steelCables = Optional.ofNullable(ppu.getSteelCables()).orElse(List.of());
        if (steelCables.isEmpty())
            return;

        Map<String, String> genericToId = steelCables.stream()
                .filter(l -> l.getGenericNumber() != null)
                .collect(Collectors.toMap(
                        l -> normalize(l.getGenericNumber()),
                        SteelCableLine::getId,
                        (a, b) -> a));

        if (genericToId.isEmpty())
            return;

        List<RDOEntity> rdos = rdoRepository.findByPpuId(ppu.getId());
        for (RDOEntity rdo : rdos) {
            var steel = rdo.getSteelCable();
            if (steel == null || steel.isEmpty())
                continue;

            List<SteelCableChecker> fixed = steel.stream()
                    .map(sc -> fixSingleCable(sc, genericToId))
                    .toList();

            if (!fixed.equals(steel)) {
                rdo.setSteelCable(fixed);
                rdoRepository.save(rdo);
            }
        }
    }

    private SteelCableChecker fixSingleCable(SteelCableChecker sc, Map<String, String> genericToId) {
        String numero = normalize(sc.numero());
        if (numero.isEmpty())
            return sc;

        String expectedLineId = genericToId.get(numero);
        if (expectedLineId == null)
            return sc;

        if (Objects.equals(expectedLineId, sc.lineID()))
            return sc;

        return sc.copyWithLineID(expectedLineId);
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim();
    }

    public void backfillValueMeasuredByLineStrategies() {
        var allRdos = Optional.ofNullable(rdoRepository.findAll()).orElseGet(List::of);

        var ppuById = Optional.ofNullable(ppuRepository.findAll()).orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .filter(p -> trimToNull(p.getId()) != null)
                .collect(Collectors.toMap(PPUEntity::getId, Function.identity(), (a, b) -> a));

        int updated = 0;
        int skipped = 0;
        int noPpu = 0;
        int noServices = 0;
        int unresolved = 0;

        for (var rdo : allRdos) {
            if (rdo == null)
                continue;

            var services = Optional.ofNullable(rdo.getServices()).orElseGet(List::of);
            if (services.isEmpty()) {
                noServices++;
                continue;
            }

            var ppu = ppuById.get(trimToNull(rdo.getPpuId()));
            if (ppu == null) {
                noPpu++;
                continue;
            }

            var indexes = buildServiceIndexes(ppu.getServices());

            boolean changed = false;

            for (var svc : services) {
                if (svc == null)
                    continue;

                var serviceLine = resolveServiceLine(svc, indexes);
                if (serviceLine == null) {
                    unresolved++;
                    continue;
                }

                var before = svc.getValueMeasured();

                LineStrategy strategy = lineStrategyRegistry.getStrategy(serviceLine);
                strategy.calculate(svc, rdo.getServices(), indexes.byId());

                var after = svc.getValueMeasured();
                if (!Objects.equals(before, after)) {
                    changed = true;
                }
            }

            if (changed) {
                rdoRepository.save(rdo);
                updated++;
                System.out.printf("[RDO BACKFILL valueMeasured] %s updated%n", rdo.getId());
            } else {
                skipped++;
                System.out.printf("[RDO BACKFILL valueMeasured] %s skipped (no changes)%n", rdo.getId());
            }
        }

        System.out.printf(
                "[RDO BACKFILL valueMeasured] finished. updated=%d skipped=%d noPpu=%d noServices=%d unresolved=%d total=%d%n",
                updated, skipped, noPpu, noServices, unresolved, allRdos.size());
    }

    private ServiceIndexes buildServiceIndexes(List<ServiceLine> services) {
        var safe = Optional.ofNullable(services).orElseGet(List::of).stream()
                .filter(Objects::nonNull)
                .toList();

        Map<String, ServiceLine> byId = safe.stream()
                .filter(s -> trimToNull(s.getId()) != null)
                .collect(Collectors.toMap(s -> s.getId().trim(), Function.identity(), (a, b) -> a));

        Map<String, ServiceLine> byNumber = safe.stream()
                .filter(s -> trimToNull(s.getGenericNumber()) != null)
                .collect(Collectors.toMap(s -> s.getGenericNumber().trim(), Function.identity(), (a, b) -> a));

        return new ServiceIndexes(byId, byNumber);
    }

    private ServiceLine resolveServiceLine(RDOServiceEntity svc, ServiceIndexes indexes) {
        var sid = trimToNull(svc.getServiceID());
        if (sid != null) {
            var byId = indexes.byId().get(sid);
            if (byId != null)
                return byId;
        }

        var num = trimToNull(svc.getServiceNumber());
        if (num != null) {
            return indexes.byNumber().get(num);
        }

        return null;
    }

    private String trimToNull(String s) {
        if (s == null)
            return null;
        var t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private record ServiceIndexes(Map<String, ServiceLine> byId, Map<String, ServiceLine> byNumber) {
    }

}