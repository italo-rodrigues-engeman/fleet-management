package com.indux.modules.ppu.application.services.change_tickets;

import com.indux.core.infra.config.IpResolver;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.modules.ppu.application.dtos.requests.ChangeTicketRequest;
import com.indux.modules.ppu.domain.entities.item.MeasurementForecast;
import com.indux.modules.ppu.domain.entities.mongo.ChangeTicket;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.ppu.ServiceLine;
import com.indux.modules.ppu.domain.repositories.mongo.ChangeTicketRepository;
import com.indux.modules.ppu.domain.repositories.mongo.PPURepository;
import com.indux.modules.ppu.infra.mapper.ticket.ChangeTicketMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CreateChangeTicketUseCase {
    private final ChangeTicketRepository repository;
    private final PPURepository ppuRepository;
    private final ChangeTicketMapper mapper;

    public ChangeTicket execute(ChangeTicketRequest request, HttpServletRequest req, JwtAuthenticationToken token) {
        var ppu = ppuRepository.findById(request.ppuId())
                .orElseThrow(() -> new ModuleNotFoundFailure("PPU não encontrada"));

        var source = ChangeTicket.Source.builder()
                .service("kogni")
                .ip(IpResolver.resolve(req))
                .build();

        var actor = ChangeTicket.Actor.builder()
                .name(token.getToken().getClaimAsString("name"))
                .userId(token.getName())
                .build();

        var version = ppu.getVersion() == null ? 0L : ppu.getVersion();
        var itemsReq = request.items() == null ? List.<ChangeTicketRequest.ChangeTicketItemRequest>of() : request.items();

        var changeItems = new ArrayList<ChangeTicket.ChangeItem>(itemsReq.size());
        var diffs = new ArrayList<ChangeTicket.HistoryLog>(itemsReq.size());

        for (var it : itemsReq) {
            var lt = LineType.of(it.lineType());
            changeItems.add(mapper.toEntity(it));
            var current = currentValue(ppu, lt, it);
            diffs.add(historyLog(lt, it, current));
        }

        var ticket = ChangeTicket.builder()
                .ppuId(request.ppuId())
                .reason(request.reason())
                .fromVersion(version)
                .toVersion(version + 1)
                .item(changeItems)
                .actor(actor)
                .source(source)
                .diff(diffs)
                .contract(ppu.getContract())
                .regionalId(ppu.getRegionalId())
                .regionalName(ppu.getRegionalNome())
                .apelido(ppu.getNickname())
                .status(ChangeTicket.TicketStatus.PENDING)
                .build();

        return repository.save(ticket);
    }

    private Object currentValue(PPUEntity ppu, LineType lt, ChangeTicketRequest.ChangeTicketItemRequest req) {
        return switch (lt) {
            case SERVICE -> {
                var service = findService(ppu, req.itemId());
                var forecast = findForecast(service.getMeasurementForecasts(), req.platform(), "Plataforma não encontrada: ");
                yield forecast.getTotal();
            }
            case EQUIPMENT -> {
                var equipment = ppu.getEquipments().stream()
                        .filter(e -> Objects.equals(e.getId(), req.itemId()))
                        .findFirst()
                        .orElseThrow(() -> new ModuleNotFoundFailure("Equipamento não encontrado: " + req.itemId()));
                if (req.platform() == null) yield 0;
                var forecast = findForecast(equipment.getMeasurementForecasts(), req.platform(), "Plataforma não encontrada: ");
                yield forecast.getTotal();
            }
            case STEEL_CABLE -> ppu.getSteelCables().stream()
                    .filter(sc -> Objects.equals(sc.getId(), req.itemId()))
                    .findFirst()
                    .orElseThrow(() -> new ModuleNotFoundFailure("Cabo de aço não encontrado: " + req.itemId()))
                    .getTotalPlanned();
            case ACCESSORY_KIT -> {
                var kit = ppu.getAccessoryKits().stream()
                        .filter(ak -> Objects.equals(ak.getId(), req.itemId()))
                        .findFirst()
                        .orElseThrow(() -> new ModuleNotFoundFailure("Kit de acessório não encontrado: " + req.itemId()));
                if (req.platform() == null) yield 0;
                var forecast = findForecast(kit.getMeasurementForecasts(), req.platform(), "Plataforma não encontrada: ");
                yield forecast.getTotal();
            }
        };
    }

    private ChangeTicket.HistoryLog historyLog(LineType lt, ChangeTicketRequest.ChangeTicketItemRequest req, Object from) {
        var path = switch (lt) {
            case SERVICE -> "/services/%s/measurementForecasts/%s/total".formatted(req.itemId(), req.platform());
            case EQUIPMENT -> "/equipments/%s/measurementForecasts/%s/total".formatted(req.itemId(), req.platform());
            case STEEL_CABLE -> "/steelCables/%s/totalPlanned".formatted(req.itemId());
            case ACCESSORY_KIT -> "/accessoryKits/%s/measurementForecasts/%s/total".formatted(req.itemId(), req.platform());
        };

        return ChangeTicket.HistoryLog.builder()
                .operation("update")
                .path(path)
                .from(from)
                .to(req.quantity())
                .build();
    }

    private ServiceLine findService(PPUEntity ppu, String serviceId) {
        return ppu.getServices().stream()
                .filter(s -> Objects.equals(s.getId(), serviceId))
                .findFirst()
                .orElseThrow(() -> new ModuleNotFoundFailure("Serviço não encontrado na PPU: " + serviceId));
    }

    private MeasurementForecast findForecast(List<MeasurementForecast> forecasts, String platform, String notFoundPrefix) {
        return forecasts.stream()
                .filter(f -> Objects.equals(f.getPlatform(), platform))
                .findFirst()
                .orElseThrow(() -> new ModuleNotFoundFailure(notFoundPrefix + platform));
    }

    private enum LineType {
        SERVICE, EQUIPMENT, STEEL_CABLE, ACCESSORY_KIT;

        static LineType of(String raw) {
            if (raw == null) throw new IllegalArgumentException("Tipo de linha não informado");
            try {
                return LineType.valueOf(raw.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Tipo de linha não suportado: " + raw);
            }
        }
    }
}
