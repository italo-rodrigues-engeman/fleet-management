package com.indux.modules.ppu.domain.services.bm.audit;

import com.indux.core.domain.model.employee.Platform;
import com.indux.core.domain.repository.employee.PlatformRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PlatformAliasResolverTest {

    @Mock
    private PlatformRepository platformRepository;

    private PlatformAliasResolver resolver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        List<Platform> platforms = Arrays.asList(
                createPlatform(1L, "Plataforma Mexilhão", "PMXL", "Mexilhão"),
                createPlatform(2L, "Plataforma Parati", "PPAT", "Parati"),
                createPlatform(3L, "Plataforma Tupi", "PTUP", null),
                createPlatform(4L, "Plataforma Berbigão", "PBRB", "Berbigão"),
                createPlatform(5L, "Plataforma P-78", "P-78", "P-78"),
                createPlatform(6L, "Plataforma P-78", "P-78", "BUZIOS/PRODUCAO/ATP-BUZ-II/OP-P78"),
                createPlatform(7L, "Plataforma P-77", "P-77", "P-77"),
                createPlatform(8L, "Plataforma P-77", "P-77", "BUZIOS-P-77")
        );
        
        when(platformRepository.findAll()).thenReturn(platforms);
        
        resolver = new PlatformAliasResolver(platformRepository);
        resolver.init();
    }

    @Test
    @DisplayName("Should normalize raw platform by removing PLATAFORMA prefix")
    void shouldNormalizeRawPlatformByRemovingPrefix() {
        assertEquals("PMXL", resolver.normalizeRawPlatform("PLATAFORMA PMXL"));
        assertEquals("PMXL", resolver.normalizeRawPlatform("plataforma pmxl"));
        assertEquals("PMXL", resolver.normalizeRawPlatform("Plataforma PMXL"));
    }

    @Test
    @DisplayName("Should normalize raw platform to uppercase")
    void shouldNormalizeRawPlatformToUppercase() {
        assertEquals("PMXL", resolver.normalizeRawPlatform("pmxl"));
        assertEquals("MEXILHAO", resolver.normalizeRawPlatform("Mexilhão"));
    }

    @Test
    @DisplayName("Should resolve alias to sigla - Mexilhão")
    void shouldResolveAliasToSigla_Mexilhao() {
        assertEquals("PMXL", resolver.toSigla("Mexilhão"));
        assertEquals("PMXL", resolver.toSigla("mexilhão"));
        assertEquals("PMXL", resolver.toSigla("MEXILHÃO"));
    }

    @Test
    @DisplayName("Should resolve alias to sigla - Parati")
    void shouldResolveAliasToSigla_Parati() {
        assertEquals("PPAT", resolver.toSigla("Parati"));
        assertEquals("PPAT", resolver.toSigla("parati"));
    }

    @Test
    @DisplayName("Should resolve sigla to itself")
    void shouldResolveSiglaToItself() {
        assertEquals("PMXL", resolver.toSigla("PMXL"));
        assertEquals("PMXL", resolver.toSigla("pmxl"));
        assertEquals("PPAT", resolver.toSigla("PPAT"));
    }

    @Test
    @DisplayName("Should resolve with PLATAFORMA prefix")
    void shouldResolveWithPlataformaPrefix() {
        assertEquals("PMXL", resolver.toSigla("PLATAFORMA Mexilhão"));
        assertEquals("PMXL", resolver.toSigla("Plataforma PMXL"));
        assertEquals("PPAT", resolver.toSigla("PLATAFORMA Parati"));
    }

    @Test
    @DisplayName("Should return normalized value when no match found")
    void shouldReturnNormalizedValueWhenNoMatchFound() {
        String unknown = resolver.toSigla("Unknown Platform");
        assertEquals("UNKNOWNPLATFORM", unknown);
    }

    @Test
    @DisplayName("Should handle null and blank values")
    void shouldHandleNullAndBlankValues() {
        assertEquals("", resolver.toSigla(null));
        assertEquals("", resolver.toSigla(""));
        assertEquals("", resolver.toSigla("   "));
    }

    @Test
    @DisplayName("Should resolve platform without alias")
    void shouldResolvePlatformWithoutAlias() {
        assertEquals("PTUP", resolver.toSigla("PTUP"));
        assertEquals("PTUP", resolver.toSigla("ptup"));
    }

    @Test
    @DisplayName("Should normalize and resolve in single call")
    void shouldNormalizeAndResolveInSingleCall() {
        assertEquals("PMXL", resolver.toSigla("  plataforma Mexilhão  "));
        assertEquals("PPAT", resolver.toSigla("  PLATAFORMA parati  "));
    }

    @Test
    @DisplayName("Should return canonical value from sigla")
    void shouldReturnCanonicalValueFromSigla() {
        assertEquals("PMXL", resolver.toCanonical("PMXL"));
        assertEquals("PMXL", resolver.toCanonical("pmxl"));
        assertEquals("PPAT", resolver.toCanonical("PPAT"));
    }

    @Test
    @DisplayName("Should return canonical value from alias")
    void shouldReturnCanonicalValueFromAlias() {
        assertEquals("PMXL", resolver.toCanonical("Mexilhão"));
        assertEquals("PMXL", resolver.toCanonical("mexilhão"));
        assertEquals("PPAT", resolver.toCanonical("Parati"));
    }

    @Test
    @DisplayName("Should return canonical value removing hyphens and spaces")
    void shouldReturnCanonicalValueRemovingHyphensAndSpaces() {
        assertEquals("P78", resolver.toCanonical("P-78"));
        assertEquals("P78", resolver.toCanonical("P 78"));
        assertEquals("P78", resolver.toCanonical("Plataforma P-78"));
    }

    @Test
    @DisplayName("Should return canonical value from long SAMC alias")
    void shouldReturnCanonicalValueFromLongSamcAlias() {
        String longAlias = "BUZIOS/PRODUCAO/ATP-BUZ-II/OP-P78";
        String canonical = resolver.toCanonical(longAlias);
        assertEquals("P78", canonical, "Alias longo deve resolver para sigla P-78 e depois normalizar para P78");
    }

    @Test
    @DisplayName("Should handle null and blank values in toCanonical")
    void shouldHandleNullAndBlankValuesInToCanonical() {
        assertEquals("", resolver.toCanonical(null));
        assertEquals("", resolver.toCanonical(""));
        assertEquals("", resolver.toCanonical("   "));
    }

    @Test
    @DisplayName("Should return same canonical for different aliases pointing to same sigla")
    void shouldReturnSameCanonicalForDifferentAliasesPointingToSameSigla() {
        String canonical1 = resolver.toCanonical("P-77");
        String canonical2 = resolver.toCanonical("BUZIOS-P-77");
        assertEquals("P77", canonical1);
        assertEquals("P77", canonical2);
        assertEquals(canonical1, canonical2, "Aliases diferentes para mesma sigla devem retornar mesmo valor canônico");
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
