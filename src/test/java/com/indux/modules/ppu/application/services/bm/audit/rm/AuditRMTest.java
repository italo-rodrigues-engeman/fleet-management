package com.indux.modules.ppu.application.services.bm.audit.rm;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.service.AttachmentService;
import com.indux.core.infra.exception.module.ModuleFailure;
import com.indux.modules.ppu.application.dtos.response.RMLogResponse;
import com.indux.modules.ppu.application.dtos.response.bm.BMPlatformReport;
import com.indux.modules.ppu.application.services.rdo.operation.bm.BMPlatformHandler;
import com.indux.modules.ppu.domain.entities.bm.RMLog;
import com.indux.modules.ppu.domain.entities.mongo.BMEntity;
import com.indux.modules.ppu.domain.entities.mongo.PPUEntity;
import com.indux.modules.ppu.domain.repositories.mongo.BMRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuditRMTest {

    @Mock
    private BMRepository repository;

    @Mock
    private AttachmentService attachmentService;

    @InjectMocks
    private AuditRM auditRM;

    @Mock
    private JwtAuthenticationToken token;

    private MockedStatic<RMPDFReader> mockedPDFReader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        setupMockJwtToken();
        mockedPDFReader = mockStatic(RMPDFReader.class);
    }

    @AfterEach
    void tearDown() {
        if (mockedPDFReader != null) {
            mockedPDFReader.close();
        }
    }

    private void setupMockJwtToken() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("name", "Test User");
        claims.put("email", "user@test.com");
        claims.put("registration", "12345");
        
        Jwt jwt = Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claims(c -> c.putAll(claims))
                .build();
        
        when(token.getToken()).thenReturn(jwt);
        when(token.getName()).thenReturn(UUID.randomUUID().toString());
    }

    @Test
    @DisplayName("Should execute audit successfully with valid files")
    void execute_ShouldReturnRMLogResponse_WhenFilesAreValid() throws Exception {
        String bmID = "bm123";
        BMEntity bm = createMockBM(bmID, "5000.00");
        List<MultipartFile> files = List.of(createMockPDFFile("4600677524", "1000.00"));
        List<AttachmentEntity> attachments = List.of(createMockAttachment());

        RMPDFReader.Resumo mockResumo = new RMPDFReader.Resumo(
                new BigDecimal("1000.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "4600677524"
        );

        mockedPDFReader.when(() -> RMPDFReader.parse(any(MultipartFile.class)))
                .thenReturn(mockResumo);

        when(repository.findById(bmID)).thenReturn(Optional.of(bm));
        when(attachmentService.createAttachmentsFromMultipartFiles(files, "bm/" + bmID))
                .thenReturn(attachments);


        RMLogResponse response = auditRM.execute(bmID, files, token);

        assertNotNull(response);
        assertEquals(new BigDecimal("5000.00"), response.totalBM());
        assertEquals(new BigDecimal("1000.00"), response.totalRM());
        assertNotNull(response.porcentagem());
        assertEquals(20.0, response.porcentagem(), 0.01);
        assertEquals(1, response.rms().size());

        verify(repository).findById(bmID);
        verify(attachmentService).createAttachmentsFromMultipartFiles(files, "bm/" + bmID);
        verify(repository).save(bm);
    }

    @Test
    @DisplayName("Should sum existing and new RM values correctly")
    void execute_ShouldSumExistingAndNewRMValues() throws Exception {
        String bmID = "bm123";
        BMEntity bm = createMockBMWithExistingRMs(bmID, new BigDecimal("2000.00"), "2000.00");
        List<MultipartFile> files = List.of(createMockPDFFile("4600677524", "1000.00"));
        List<AttachmentEntity> attachments = List.of(createMockAttachment());

        RMPDFReader.Resumo mockResumo = new RMPDFReader.Resumo(
                new BigDecimal("1000.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "4600677524"
        );

        mockedPDFReader.when(() -> RMPDFReader.parse(any(MultipartFile.class)))
                .thenReturn(mockResumo);

        when(repository.findById(bmID)).thenReturn(Optional.of(bm));
        when(attachmentService.createAttachmentsFromMultipartFiles(files, "bm/" + bmID))
                .thenReturn(attachments);


        RMLogResponse response = auditRM.execute(bmID, files, token);

        assertNotNull(response);
        assertEquals(new BigDecimal("3000.00"), response.totalRM());
        assertEquals(150.0, response.porcentagem(), 0.01);
    }

    @Test
    @DisplayName("Should throw ModuleFailure when BM not found")
    void execute_ShouldThrowModuleFailure_WhenBMNotFound() {
        String bmID = "invalid";
        List<MultipartFile> files = List.of(createMockPDFFile("4600677524", "1000.00"));

        when(repository.findById(bmID)).thenReturn(Optional.empty());

        ModuleFailure exception = assertThrows(ModuleFailure.class,
                () -> auditRM.execute(bmID, files, token));

        assertEquals("BM não encontrada pelo ID informado.", exception.getMessage());
        verify(repository).findById(bmID);
        verifyNoInteractions(attachmentService);
    }

    @Test
    @DisplayName("Should throw ModuleFailure when total BM value not found")
    void execute_ShouldThrowModuleFailure_WhenTotalNotFound() {
        String bmID = "bm123";
        BMEntity bm = createMockBM(bmID, "0");
        List<MultipartFile> files = List.of(createMockPDFFile("4600677524", "1000.00"));
        RMPDFReader.Resumo mockResumo = new RMPDFReader.Resumo(
                new BigDecimal("1000.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "9999999999"
        );

        when(repository.findById(bmID)).thenReturn(Optional.of(bm));
        mockedPDFReader.when(() -> RMPDFReader.parse(any(MultipartFile.class)))
                .thenReturn(mockResumo);
        ModuleFailure exception = assertThrows(ModuleFailure.class,
                () -> auditRM.execute(bmID, files, token));

        verify(repository).findById(bmID);
        verifyNoInteractions(attachmentService);
    }

    @Test
    @DisplayName("Should throw ModuleFailure when contract code diverges")
    void execute_ShouldThrowModuleFailure_WhenContractCodeDiverges() {
        String bmID = "bm123";
        BMEntity bm = createMockBM(bmID, "5000.00");
        List<MultipartFile> files = List.of(createMockPDFFile("9999999999", "1000.00"));

        RMPDFReader.Resumo mockResumo = new RMPDFReader.Resumo(
                new BigDecimal("1000.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "9999999999"
        );

        mockedPDFReader.when(() -> RMPDFReader.parse(any(MultipartFile.class)))
                .thenReturn(mockResumo);

        when(repository.findById(bmID)).thenReturn(Optional.of(bm));

        ModuleFailure exception = assertThrows(ModuleFailure.class,
                () -> auditRM.execute(bmID, files, token));

        assertTrue(exception.getMessage().contains("Código SAP do contrato no arquivo"));
        assertTrue(exception.getMessage().contains("diverge"));
        assertTrue(exception.getMessage().contains("Esperado: 4600677524"));
        assertTrue(exception.getMessage().contains("Encontrado: 9999999999"));

        verify(repository).findById(bmID);
        verify(attachmentService, never()).createAttachmentsFromMultipartFiles(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should validate all files before saving any")
    void execute_ShouldValidateAllFilesBeforeSaving() {
        String bmID = "bm123";
        BMEntity bm = createMockBM(bmID, "5000.00");
        List<MultipartFile> files = List.of(
                createMockPDFFile("4600677524", "1000.00"),
                createMockPDFFile("9999999999", "2000.00")
        );

        mockedPDFReader.when(() -> RMPDFReader.parse(argThat(f -> Objects.requireNonNull(f.getOriginalFilename()).contains("test"))))
                .thenReturn(
                        new RMPDFReader.Resumo(new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "4600677524"),
                        new RMPDFReader.Resumo(new BigDecimal("2000.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "9999999999")
                );

        when(repository.findById(bmID)).thenReturn(Optional.of(bm));

        assertThrows(ModuleFailure.class, () -> auditRM.execute(bmID, files, token));

        verify(attachmentService, never()).createAttachmentsFromMultipartFiles(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should process multiple valid files correctly")
    void execute_ShouldProcessMultipleFiles() throws Exception {
        String bmID = "bm123";
        BMEntity bm = createMockBM(bmID, "5000.00");
        List<MultipartFile> files = List.of(
                createMockPDFFile("4600677524", "1000.00"),
                createMockPDFFile("4600677524", "1500.00")
        );
        List<AttachmentEntity> attachments = List.of(
                createMockAttachment(),
                createMockAttachment()
        );

        mockedPDFReader.when(() -> RMPDFReader.parse(any(MultipartFile.class)))
                .thenReturn(
                        new RMPDFReader.Resumo(new BigDecimal("1000.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "4600677524"),
                        new RMPDFReader.Resumo(new BigDecimal("1500.00"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, "4600677524")
                );

        when(repository.findById(bmID)).thenReturn(Optional.of(bm));
        when(attachmentService.createAttachmentsFromMultipartFiles(files, "bm/" + bmID))
                .thenReturn(attachments);


        RMLogResponse response = auditRM.execute(bmID, files, token);

        assertNotNull(response);
        assertEquals(new BigDecimal("2500.00"), response.totalRM());
        assertEquals(2, response.rms().size());
        assertEquals(50.0, response.porcentagem(), 0.01);
    }

    @Test
    @DisplayName("Should filter null values when calculating existing RM total")
    void execute_ShouldFilterNullValuesInExistingRMs() throws Exception {
        String bmID = "bm123";
        BMEntity bm = createMockBMWithNullRMValues(bmID);
        List<MultipartFile> files = List.of(createMockPDFFile("4600677524", "1000.00"));
        List<AttachmentEntity> attachments = List.of(createMockAttachment());

        RMPDFReader.Resumo mockResumo = new RMPDFReader.Resumo(
                new BigDecimal("1000.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "4600677524"
        );

        mockedPDFReader.when(() -> RMPDFReader.parse(any(MultipartFile.class)))
                .thenReturn(mockResumo);

        when(repository.findById(bmID)).thenReturn(Optional.of(bm));
        when(attachmentService.createAttachmentsFromMultipartFiles(files, "bm/" + bmID))
                .thenReturn(attachments);


        RMLogResponse response = auditRM.execute(bmID, files, token);

        assertNotNull(response);
        assertEquals(new BigDecimal("2000.00"), response.totalRM());
    }

    @Test
    @DisplayName("Should calculate percentage correctly")
    void execute_ShouldCalculatePercentageCorrectly() throws Exception {
        String bmID = "bm123";
        BMEntity bm = createMockBM(bmID, "10000.00");
        List<MultipartFile> files = List.of(createMockPDFFile("4600677524", "2500.00"));
        List<AttachmentEntity> attachments = List.of(createMockAttachment());

        RMPDFReader.Resumo mockResumo = new RMPDFReader.Resumo(
                new BigDecimal("2500.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "4600677524"
        );

        mockedPDFReader.when(() -> RMPDFReader.parse(any(MultipartFile.class)))
                .thenReturn(mockResumo);

        when(repository.findById(bmID)).thenReturn(Optional.of(bm));
        when(attachmentService.createAttachmentsFromMultipartFiles(files, "bm/" + bmID))
                .thenReturn(attachments);


        RMLogResponse response = auditRM.execute(bmID, files, token);

        assertEquals(25.0, response.porcentagem(), 0.01);
    }

    @Test
    @DisplayName("Should set auditRMChecked to true after successful audit")
    void execute_ShouldSetAuditRMCheckedToTrue() throws Exception {
        String bmID = "bm123";
        BMEntity bm = createMockBM(bmID, "5000.00");
        List<MultipartFile> files = List.of(createMockPDFFile("4600677524", "1000.00"));
        List<AttachmentEntity> attachments = List.of(createMockAttachment());

        RMPDFReader.Resumo mockResumo = new RMPDFReader.Resumo(
                new BigDecimal("1000.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "4600677524"
        );

        mockedPDFReader.when(() -> RMPDFReader.parse(any(MultipartFile.class)))
                .thenReturn(mockResumo);

        when(repository.findById(bmID)).thenReturn(Optional.of(bm));
        when(attachmentService.createAttachmentsFromMultipartFiles(files, "bm/" + bmID))
                .thenReturn(attachments);


        auditRM.execute(bmID, files, token);

        assertTrue(bm.getAuditRMChecked());
        verify(repository).save(bm);
    }

    @Test
    @DisplayName("Should remove RMLog successfully")
    void removeRMLog_ShouldRemoveRMLog_WhenBMNotFinished() {
        String bmID = "bm123";
        String rmLogId = "rm456";
        BMEntity bm = createMockBMWithRMLog(bmID, rmLogId);

        when(repository.findById(bmID)).thenReturn(Optional.of(bm));

        assertDoesNotThrow(() -> auditRM.removeRMLog(bmID, rmLogId));

        assertFalse(bm.getAuditRMChecked());
        assertTrue(bm.getAuditRMLog().stream().noneMatch(log -> log.getId().equals(rmLogId)));
        verify(repository).save(bm);
    }

    @Test
    @DisplayName("Should throw ModuleFailure when removing RMLog from finished BM")
    void removeRMLog_ShouldThrowModuleFailure_WhenBMIsFinished() {
        String bmID = "bm123";
        String rmLogId = "rm456";
        BMEntity bm = createMockFinishedBM(bmID);

        when(repository.findById(bmID)).thenReturn(Optional.of(bm));

        ModuleFailure exception = assertThrows(ModuleFailure.class,
                () -> auditRM.removeRMLog(bmID, rmLogId));

        assertEquals("Não é possível remover RM de uma BM já aprovada.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ModuleFailure when RMLog not found")
    void removeRMLog_ShouldThrowModuleFailure_WhenRMLogNotFound() {
        String bmID = "bm123";
        String rmLogId = "invalid";
        BMEntity bm = createMockBM(bmID, "0");

        when(repository.findById(bmID)).thenReturn(Optional.of(bm));

        ModuleFailure exception = assertThrows(ModuleFailure.class,
                () -> auditRM.removeRMLog(bmID, rmLogId));

        assertTrue(exception.getMessage().contains("RMLog não encontrado com ID"));
        verify(repository, never()).save(any());
    }

    private BMEntity createMockBM(String id, String totalValue) {
        BMEntity bm = new BMEntity();
        bm.setId(id);
        bm.setAuditRMLog(new ArrayList<>());
        bm.setAuditRMChecked(false);
        bm.setValueClosed(new BigDecimal(totalValue));

        PPUEntity ppu = new PPUEntity();
        Map<String, Object> contract = new HashMap<>();
        contract.put("codeSap", "4600677524");
        ppu.setContract(contract);
        bm.setPpu(ppu);

        return bm;
    }

    private BMEntity createMockBM(String id) {
        BMEntity bm = new BMEntity();
        bm.setId(id);
        bm.setAuditRMLog(new ArrayList<>());
        bm.setAuditRMChecked(false);

        PPUEntity ppu = new PPUEntity();
        Map<String, Object> contract = new HashMap<>();
        contract.put("codeSap", "4600677524");
        ppu.setContract(contract);
        bm.setPpu(ppu);

        return bm;
    }


    private BMEntity createMockBMWithExistingRMs(String id, BigDecimal existingValue, String valueClosed) {
        BMEntity bm = createMockBM(id, valueClosed);
        RMLog existingLog = RMLog.builder()
                .id(UUID.randomUUID().toString())
                .value(existingValue)
                .build();
        bm.getAuditRMLog().add(existingLog);
        return bm;
    }

    private BMEntity createMockBMWithNullRMValues(String id) {
        BMEntity bm = createMockBM(id);
        RMLog log1 = RMLog.builder()
                .id(UUID.randomUUID().toString())
                .value(new BigDecimal("1000.00"))
                .build();
        RMLog log2 = RMLog.builder()
                .id(UUID.randomUUID().toString())
                .value(null)
                .build();
        bm.getAuditRMLog().add(log1);
        bm.getAuditRMLog().add(log2);
        return bm;
    }

    private BMEntity createMockBMWithRMLog(String id, String rmLogId) {
        BMEntity bm = createMockBM(id);
        RMLog log = RMLog.builder()
                .id(rmLogId)
                .value(new BigDecimal("1000.00"))
                .build();
        bm.getAuditRMLog().add(log);
        bm.setAuditRMChecked(true);
        return bm;
    }

    private BMEntity createMockFinishedBM(String id) {
        BMEntity bm = createMockBM(id);
        bm.setStatus(com.indux.core.domain.model.modules.form.DocumentStatus.APROVADO);
        return bm;
    }

    private MockMultipartFile createMockPDFFile(String contractCode, String value) {
        String pdfContent = String.format(
                "CONTRATO R/3 : %s\nVALOR BRUTO: R$ %s",
                contractCode, value
        );
        return new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                pdfContent.getBytes()
        );
    }

    private AttachmentEntity createMockAttachment() {
        AttachmentEntity attachment = new AttachmentEntity();
        attachment.setId(UUID.randomUUID().toString());
        attachment.setNome("test.pdf");
        return attachment;
    }
}
