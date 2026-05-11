package com.indux.modules.request_budgets.application.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.FileMetadata;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.modules.request_budgets.application.dto.AnexoPropostaDTO;
import com.indux.modules.request_budgets.application.dto.BudgetPropostaResponseDTO;
import com.indux.modules.request_budgets.application.dto.CreateBudgetPropostaRequest;
import com.indux.modules.request_budgets.application.mapper.BudgetPropostaMapper;
import com.indux.modules.request_budgets.domain.model.BudgetProposta;
import com.indux.modules.request_budgets.domain.repository.BudgetPropostaRepository;
import com.indux.modules.request_budgets.domain.repository.BudgetVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetPropostaService {

    private final BudgetPropostaRepository budgetPropostaRepository;
    private final BudgetVersionRepository budgetVersionRepository;
    private final BudgetPropostaMapper budgetPropostaMapper;
    private final StorageService storageService;

    public BudgetPropostaResponseDTO createBudgetProposta(String versionId, CreateBudgetPropostaRequest request, String userId) {
        if (!budgetVersionRepository.existsById(versionId)) {
            throw new ModuleNotFoundFailure("Versão do orçamento não encontrada");
        }

        // Verificar se já existe uma proposta para esta versão
        if (budgetPropostaRepository.findByVersionId(versionId).stream().findFirst().isPresent()) {
            throw new IllegalArgumentException("Já existe uma proposta para esta versão");
        }

        BudgetProposta proposta = BudgetProposta.builder()
                .versionId(versionId)
                .dataHoraEntrega(request.getDataHoraEntrega())
                .valorFinalTotal(request.getValorFinalTotal())
                .comprovanteEntrega(request.getComprovanteEntrega())
                .createdBy(UUID.fromString(userId))
                .updatedBy(UUID.fromString(userId))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .anexos(new ArrayList<>())
                .build();

        // Processar lista de anexos
        if (request.getAnexos() != null && !request.getAnexos().isEmpty()) {
            List<BudgetProposta.AnexoProposta> anexosList = new ArrayList<>();
            for (AnexoPropostaDTO anexoDTO : request.getAnexos()) {
                BudgetProposta.AnexoProposta anexo = BudgetProposta.AnexoProposta.builder()
                        .anexoTipo(anexoDTO.getAnexoTipo())
                        .anexoProposta(new ArrayList<>())
                        .build();

                if (anexoDTO.getAnexoProposta() != null && !anexoDTO.getAnexoProposta().isEmpty()) {
                    anexo.setAnexoProposta(anexoDTO.getAnexoProposta());
                }

                anexosList.add(anexo);
            }
            proposta.setAnexos(anexosList);
        }

        BudgetProposta savedProposta = budgetPropostaRepository.save(proposta);
        return budgetPropostaMapper.toResponseDTO(savedProposta);
    }

    public BudgetPropostaResponseDTO getBudgetPropostaById(String id) {
        BudgetProposta proposta = budgetPropostaRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Proposta não encontrada"));
        return budgetPropostaMapper.toResponseDTO(proposta);
    }

    public BudgetPropostaResponseDTO getBudgetPropostaByVersionId(String versionId) {
        BudgetProposta proposta = budgetPropostaRepository.findByVersionId(versionId).stream()
                .findFirst()
                .orElseThrow(() -> new ModuleNotFoundFailure("Proposta não encontrada para esta versão"));
        return budgetPropostaMapper.toResponseDTO(proposta);
    }

    private List<AttachmentEntity> processAttachments(List<MultipartFile> files, String basePath) {
        List<AttachmentEntity> result = new ArrayList<>();
        if (files == null || files.isEmpty()) {
            return result;
        }

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }

            try {
                AttachmentEntity attachment = new AttachmentEntity();
                attachment.setId(UUID.randomUUID().toString());
                attachment.setNome(file.getOriginalFilename());

                String filename = attachment.getId() + " - " + LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
                        .toString().replace(":", "-");

                String mimeType = file.getContentType();
                String original = file.getOriginalFilename();
                String ext = "";
                if (original != null && original.contains(".")) {
                    ext = original.substring(original.lastIndexOf('.') + 1);
                }

                Path store = storageService.store(file, basePath, filename);
                String uri = storageService.getRootLocation().relativize(store).toString();
                uri = uri.replace("\\", "/");

                FileMetadata fileMetadata = new FileMetadata(uri, ext, mimeType, 1);
                attachment.setFile(fileMetadata);

                result.add(attachment);
            } catch (Exception e) {
                throw new RuntimeException("Erro ao processar anexo: " + file.getOriginalFilename(), e);
            }
        }

        return result;
    }
}

