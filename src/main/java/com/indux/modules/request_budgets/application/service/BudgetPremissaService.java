package com.indux.modules.request_budgets.application.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.FileMetadata;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.modules.request_budgets.application.dto.BudgetPremissaResponseDTO;
import com.indux.modules.request_budgets.application.dto.CreateBudgetPremissaRequest;
import com.indux.modules.request_budgets.application.mapper.BudgetPremissaMapper;
import com.indux.modules.request_budgets.domain.model.BudgetPremissa;
import com.indux.modules.request_budgets.domain.repository.BudgetPremissaRepository;
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
public class BudgetPremissaService {

    private final BudgetPremissaRepository budgetPremissaRepository;
    private final BudgetVersionRepository budgetVersionRepository;
    private final BudgetPremissaMapper budgetPremissaMapper;
    private final StorageService storageService;

    public BudgetPremissaResponseDTO createBudgetPremissa(String versionId, CreateBudgetPremissaRequest request, String userId) {
        if (!budgetVersionRepository.existsById(versionId)) {
            throw new ModuleNotFoundFailure("Versão do orçamento não encontrada");
        }

        BudgetPremissa premissa = BudgetPremissa.builder()
                .versionId(versionId)
                .anexoTipo(request.getAnexoTipo())
                .createdBy(UUID.fromString(userId))
                .updatedBy(UUID.fromString(userId))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .anexoPremissa(new ArrayList<>())
                .build();

        if (request.getAnexoPremissa() != null && !request.getAnexoPremissa().isEmpty()) {
            premissa.setAnexoPremissa(request.getAnexoPremissa());
        }

        BudgetPremissa savedPremissa = budgetPremissaRepository.save(premissa);
        return budgetPremissaMapper.toResponseDTO(savedPremissa);
    }

    public BudgetPremissaResponseDTO getBudgetPremissaById(String id) {
        BudgetPremissa premissa = budgetPremissaRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Premissa não encontrada"));
        return budgetPremissaMapper.toResponseDTO(premissa);
    }

    public List<BudgetPremissaResponseDTO> getBudgetPremissasByVersionId(String versionId) {
        return budgetPremissaRepository.findByVersionId(versionId).stream()
                .map(budgetPremissaMapper::toResponseDTO)
                .collect(Collectors.toList());
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

