package com.indux.modules.ppu.domain.services.bm.audit;

import com.indux.core.domain.model.employee.Platform;
import com.indux.core.domain.repository.employee.PlatformRepository;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.ppu.AuditableConfig;
import com.indux.modules.ppu.domain.entities.rdo.audit.AuditContext;
import com.indux.modules.ppu.domain.entities.rdo.audit.AuditDivergence;
import com.indux.modules.ppu.domain.entities.rdo.audit.SAMCRow;
import com.indux.modules.ppu.domain.strategy.OvertimeProjectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AuditEngineHeaderPlatformTest {

    @Mock
    private OvertimeProjectionService overtimeProjectionService;

    @Mock
    private PlatformRepository platformRepository;

    private AuditEngine auditEngine;
    private PlatformAliasResolver platformResolver;
    private AuditableConfig auditableConfig = new AuditableConfig(
            "Número Detalhamento EAC",
            0
    );
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        List<Platform> platforms = Arrays.asList(
                createPlatform(1L, "Plataforma Mexilhão", "PMXL", "Mexilhão"),
                createPlatform(2L, "Plataforma Parati", "PPAT", "Parati"),
                createPlatform(3L, "Plataforma P-78", "P-78", "P-78"),
                createPlatform(4L, "Plataforma P-78", "P-78", "BUZIOS/PRODUCAO/ATP-BUZ-II/OP-P78"),
                createPlatform(5L, "Plataforma P-77", "P-77", "P-77"),
                createPlatform(6L, "Plataforma P-77", "P-77", "BUZIOS-P-77")
        );
        
        when(platformRepository.findAll()).thenReturn(platforms);
        
        platformResolver = new PlatformAliasResolver(platformRepository);
        platformResolver.init();
        
        auditEngine = new AuditEngine(overtimeProjectionService, platformResolver);
    }

    @Test
    @DisplayName("Should not generate platform divergence when SAMC has alias and RDO has sigla")
    void shouldNotGeneratePlatformDivergenceWhenSamcHasAliasAndRdoHasSigla() {
        RDOEntity rdo = createRDO("rdo-1", LocalDate.of(2025, 7, 16), "PMXL");
        PPUEntity ppu = new PPUEntity();
        ppu.setAuditableConfig(auditableConfig);
        
        SAMCRow samcRow = SAMCRow.builder()
                .contrato("400400400")
                .local("Mexilhão")
                .data("16/07/2025")
                .numeroDetalhamento("1.1.1.2.1")
                .descricaoServico("Serviço teste")
                .quantidadeExecutada("1")
                .status("APROVADO")
                .build();
        
        AuditContext ctx = new AuditContext(
                rdo.getDate(),
                rdo.getPlatform(),
                ppu,
                rdo,
                List.of(samcRow)
        );
        
        List<AuditDivergence> divergences = auditEngine.audit(ctx);
        
        boolean hasPlatformDivergence = divergences.stream()
                .anyMatch(d -> d.text().toLowerCase().contains("plataforma"));
        
        assertFalse(hasPlatformDivergence, 
                "Não deveria gerar divergência de plataforma quando SAMC tem apelido 'Mexilhão' e RDO tem sigla 'PMXL'");
    }

    @Test
    @DisplayName("Should not generate platform divergence when SAMC has PLATAFORMA prefix with alias")
    void shouldNotGeneratePlatformDivergenceWhenSamcHasPlataformaPrefixWithAlias() {
        RDOEntity rdo = createRDO("rdo-1", LocalDate.of(2025, 7, 16), "PPAT");
        PPUEntity ppu = new PPUEntity();
        ppu.setAuditableConfig(auditableConfig);
        
        SAMCRow samcRow = SAMCRow.builder()
                .contrato("400400400")
                .local("PLATAFORMA Parati")
                .data("16/07/2025")
                .numeroDetalhamento("1.1.1.2.1")
                .descricaoServico("Serviço teste")
                .quantidadeExecutada("1")
                .status("APROVADO")
                .build();
        
        AuditContext ctx = new AuditContext(
                rdo.getDate(),
                rdo.getPlatform(),
                ppu,
                rdo,
                List.of(samcRow)
        );
        
        List<AuditDivergence> divergences = auditEngine.audit(ctx);
        
        boolean hasPlatformDivergence = divergences.stream()
                .anyMatch(d -> d.text().toLowerCase().contains("plataforma"));
        
        assertFalse(hasPlatformDivergence,
                "Não deveria gerar divergência de plataforma quando SAMC tem 'PLATAFORMA Parati' e RDO tem sigla 'PPAT'");
    }

    @Test
    @DisplayName("Should not generate platform divergence when both use sigla")
    void shouldNotGeneratePlatformDivergenceWhenBothUseSigla() {
        RDOEntity rdo = createRDO("rdo-1", LocalDate.of(2025, 7, 16), "PMXL");
        PPUEntity ppu = new PPUEntity();
        ppu.setAuditableConfig(auditableConfig);
        
        SAMCRow samcRow = SAMCRow.builder()
                .contrato("400400400")
                .local("PMXL")
                .data("16/07/2025")
                .numeroDetalhamento("1.1.1.2.1")
                .descricaoServico("Serviço teste")
                .quantidadeExecutada("1")
                .status("APROVADO")
                .build();
        
        AuditContext ctx = new AuditContext(
                rdo.getDate(),
                rdo.getPlatform(),
                ppu,
                rdo,
                List.of(samcRow)
        );
        
        List<AuditDivergence> divergences = auditEngine.audit(ctx);
        
        boolean hasPlatformDivergence = divergences.stream()
                .anyMatch(d -> d.text().toLowerCase().contains("plataforma"));
        
        assertFalse(hasPlatformDivergence,
                "Não deveria gerar divergência de plataforma quando ambos usam sigla 'PMXL'");
    }

    @Test
    @DisplayName("Should not generate platform divergence when SAMC has long alias and RDO has sigla with hyphen")
    void shouldNotGeneratePlatformDivergenceWhenSamcHasLongAliasAndRdoHasSiglaWithHyphen() {
        RDOEntity rdo = createRDO("rdo-1", LocalDate.of(2025, 7, 16), "P-78");
        PPUEntity ppu = new PPUEntity();
        ppu.setAuditableConfig(auditableConfig);
        
        SAMCRow samcRow = SAMCRow.builder()
                .contrato("400400400")
                .local("BUZIOS/PRODUCAO/ATP-BUZ-II/OP-P78")
                .data("16/07/2025")
                .numeroDetalhamento("1.1.1.2.1")
                .descricaoServico("Serviço teste")
                .quantidadeExecutada("1")
                .status("APROVADO")
                .build();
        
        AuditContext ctx = new AuditContext(
                rdo.getDate(),
                rdo.getPlatform(),
                ppu,
                rdo,
                List.of(samcRow)
        );
        
        List<AuditDivergence> divergences = auditEngine.audit(ctx);
        
        boolean hasPlatformDivergence = divergences.stream()
                .anyMatch(d -> d.text().toLowerCase().contains("plataforma"));
        
        assertFalse(hasPlatformDivergence,
                "Não deveria gerar divergência quando SAMC tem alias longo 'BUZIOS/PRODUCAO/ATP-BUZ-II/OP-P78' e RDO tem 'P-78'");
    }

    @Test
    @DisplayName("Should not generate platform divergence when SAMC has P-77 and RDO has BUZIOS-P-77")
    void shouldNotGeneratePlatformDivergenceWhenSamcHasP77AndRdoHasBuziosP77() {
        RDOEntity rdo = createRDO("rdo-1", LocalDate.of(2025, 7, 16), "BUZIOS-P-77");
        PPUEntity ppu = new PPUEntity();
        ppu.setAuditableConfig(auditableConfig);
        
        SAMCRow samcRow = SAMCRow.builder()
                .contrato("400400400")
                .local("P-77")
                .data("16/07/2025")
                .numeroDetalhamento("1.1.1.2.1")
                .descricaoServico("Serviço teste")
                .quantidadeExecutada("1")
                .status("APROVADO")
                .build();
        
        AuditContext ctx = new AuditContext(
                rdo.getDate(),
                rdo.getPlatform(),
                ppu,
                rdo,
                List.of(samcRow)
        );
        
        List<AuditDivergence> divergences = auditEngine.audit(ctx);
        
        boolean hasPlatformDivergence = divergences.stream()
                .anyMatch(d -> d.text().toLowerCase().contains("plataforma"));
        
        assertFalse(hasPlatformDivergence,
                "Não deveria gerar divergência quando SAMC tem 'P-77' e RDO tem 'BUZIOS-P-77' (ambos apontam para P-77)");
    }

    @Test
    @DisplayName("Should not generate platform divergence for different mask variations")
    void shouldNotGeneratePlatformDivergenceForDifferentMaskVariations() {
        RDOEntity rdo = createRDO("rdo-1", LocalDate.of(2025, 7, 16), "P78");
        PPUEntity ppu = new PPUEntity();
        ppu.setAuditableConfig(auditableConfig);
        
        SAMCRow samcRow = SAMCRow.builder()
                .contrato("400400400")
                .local("Plataforma P-78")
                .data("16/07/2025")
                .numeroDetalhamento("1.1.1.2.1")
                .descricaoServico("Serviço teste")
                .quantidadeExecutada("1")
                .status("APROVADO")
                .build();
        
        AuditContext ctx = new AuditContext(
                rdo.getDate(),
                rdo.getPlatform(),
                ppu,
                rdo,
                List.of(samcRow)
        );
        
        List<AuditDivergence> divergences = auditEngine.audit(ctx);
        
        boolean hasPlatformDivergence = divergences.stream()
                .anyMatch(d -> d.text().toLowerCase().contains("plataforma"));
        
        assertFalse(hasPlatformDivergence,
                "Não deveria gerar divergência para variações de máscara (P78, P-78, Plataforma P-78)");
    }

    private RDOEntity createRDO(String id, LocalDate date, String platform) {
        RDOEntity rdo = RDOEntity.builder()
                .id(id)
                .date(date)
                .platform(platform)
                .contract(Map.of("codeSap", "400400400"))
                .build();
        return rdo;
    }

    private Platform createPlatform(Long id, String nome, String sigla, String apelido) {
        Platform platform = new Platform();
        platform.setId(id);
        platform.setNomePlataforma(nome);
        platform.setSigla(sigla);
        platform.setApelido(apelido);
        return platform;
    }
}
