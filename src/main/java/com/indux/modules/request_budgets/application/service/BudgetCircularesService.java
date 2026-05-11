package com.indux.modules.request_budgets.application.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.FileMetadata;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.modules.request_budgets.application.dto.BudgetCircularesResponseDTO;
import com.indux.modules.request_budgets.application.dto.CreateBudgetCircularesRequest;
import com.indux.modules.request_budgets.application.mapper.BudgetCircularesMapper;
import com.indux.modules.request_budgets.domain.model.BudgetCirculares;
import com.indux.modules.request_budgets.domain.repository.BudgetCircularesRepository;
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
public class BudgetCircularesService {

    private final BudgetCircularesRepository budgetCircularesRepository;
    private final BudgetVersionRepository budgetVersionRepository;
    private final BudgetCircularesMapper budgetCircularesMapper;
    private final StorageService storageService;

    public BudgetCircularesResponseDTO createBudgetCirculares(String versionId, CreateBudgetCircularesRequest request, String userId) {
        if (!budgetVersionRepository.existsById(versionId)) {
            throw new ModuleNotFoundFailure("Versão do orçamento não encontrada");
        }

        BudgetCirculares circulares = BudgetCirculares.builder()
                .versionId(versionId)
                .identificacaoCirculares(request.getIdentificacaoCirculares())
                .descricaoCirculares(request.getDescricaoCirculares())
                .dataHoraCirculares(request.getDataHoraCirculares())
                .responsavelCirculares(request.getResponsavelCirculares())
                .createdBy(UUID.fromString(userId))
                .updatedBy(UUID.fromString(userId))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .anexoCirculares(new ArrayList<>())
                .build();

        if (request.getAnexoCirculares() != null && !request.getAnexoCirculares().isEmpty()) {
            circulares.setAnexoCirculares(request.getAnexoCirculares());
        }

        BudgetCirculares savedCirculares = budgetCircularesRepository.save(circulares);
        return budgetCircularesMapper.toResponseDTO(savedCirculares);
    }

    public BudgetCircularesResponseDTO getBudgetCircularesById(String id) {
        BudgetCirculares circulares = budgetCircularesRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Circulares não encontrados"));
        return budgetCircularesMapper.toResponseDTO(circulares);
    }

    public List<BudgetCircularesResponseDTO> getBudgetCircularesByVersionId(String versionId) {
        return budgetCircularesRepository.findByVersionId(versionId).stream()
                .map(budgetCircularesMapper::toResponseDTO)
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

