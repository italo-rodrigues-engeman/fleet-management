package com.indux.modules.ocf.application.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.indux.modules.ocf.domain.entities.mongo.AlodpTempEntity;
import com.indux.modules.ocf.domain.repositories.mongo.AlodpTempRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlodpTempCleanupServiceTest {

    @Mock
    private AlodpTempRepository alodpTempRepository;

    @InjectMocks
    private AlodpTempCleanupService alodpTempCleanupService;

    private String cpfTeste;
    private List<AlodpTempEntity> logsExistentes;

    @BeforeEach
    void setUp() {
        cpfTeste = "11009550705";
        
        logsExistentes = Arrays.asList(
            AlodpTempEntity.builder()
                .id("68bae2e04f9f1dcd106ded18")
                .createdAt(LocalDateTime.now())
                .telefone("5527997979137@s.whatsapp.net")
                .nomeValid(true)
                .cpfValid(true)
                .etapa("em atendimento")
                .validacao(true)
                .status("pendente")
                .nomeCompleto("FABIO SILVA DE JESUS")
                .cpf(cpfTeste)
                .contrato("73")
                .matricula("8834")
                .flowLocked(false)
                .horaUltimaMsg(1757078715128L)
                .responded(false)
                .ultimoTemaEscolhido("3")
                .build()
        );
    }

    @Test
    void cleanupLogsByCpf_DeveRetornarZero_QuandoCpfNulo() {
        // Given
        String cpfNulo = null;

        // When
        long resultado = alodpTempCleanupService.cleanupLogsByCpf(cpfNulo);

        // Then
        assertEquals(0, resultado);
        verify(alodpTempRepository, never()).findByCpf(anyString());
        verify(alodpTempRepository, never()).deleteByCpf(anyString());
    }

    @Test
    void cleanupLogsByCpf_DeveRetornarZero_QuandoCpfVazio() {
        // Given
        String cpfVazio = "";

        // When
        long resultado = alodpTempCleanupService.cleanupLogsByCpf(cpfVazio);

        // Then
        assertEquals(0, resultado);
        verify(alodpTempRepository, never()).findByCpf(anyString());
        verify(alodpTempRepository, never()).deleteByCpf(anyString());
    }

    @Test
    void cleanupLogsByCpf_DeveLimparLogs_QuandoCpfValido() {
        // Given
        when(alodpTempRepository.findByCpf(cpfTeste)).thenReturn(logsExistentes);
        when(alodpTempRepository.deleteByCpf(cpfTeste)).thenReturn(1L);

        // When
        long resultado = alodpTempCleanupService.cleanupLogsByCpf(cpfTeste);

        // Then
        assertEquals(1L, resultado);
        verify(alodpTempRepository).findByCpf(cpfTeste);
        verify(alodpTempRepository).deleteByCpf(cpfTeste);
    }

    @Test
    void cleanupLogsByCpf_DeveRetornarZero_QuandoNenhumLogEncontrado() {
        // Given
        when(alodpTempRepository.findByCpf(cpfTeste)).thenReturn(Arrays.asList());
        when(alodpTempRepository.deleteByCpf(cpfTeste)).thenReturn(0L);

        // When
        long resultado = alodpTempCleanupService.cleanupLogsByCpf(cpfTeste);

        // Then
        assertEquals(0L, resultado);
        verify(alodpTempRepository).findByCpf(cpfTeste);
        verify(alodpTempRepository).deleteByCpf(cpfTeste);
    }

    @Test
    void cleanupLogsByCpf_DeveTratarExcecao_QuandoErroNoRepositorio() {
        // Given - Configurar logger para não mostrar erros durante o teste
        Logger logger = (Logger) LoggerFactory.getLogger(AlodpTempCleanupService.class);
        Level originalLevel = logger.getLevel();
        logger.setLevel(Level.OFF); // Desabilitar logs durante o teste
        
        try {
            when(alodpTempRepository.findByCpf(cpfTeste)).thenThrow(new RuntimeException("Erro de conexão"));

            // When
            long resultado = alodpTempCleanupService.cleanupLogsByCpf(cpfTeste);

            // Then
            assertEquals(0, resultado);
            verify(alodpTempRepository).findByCpf(cpfTeste);
            verify(alodpTempRepository, never()).deleteByCpf(anyString());
        } finally {
            // Restaurar nível de log original
            logger.setLevel(originalLevel != null ? originalLevel : Level.INFO);
        }
    }
}
