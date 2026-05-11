package com.indux.modules.request_budgets.application.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.FileMetadata;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.modules.request_budgets.application.dto.BudgetOSResponseDTO;
import com.indux.modules.request_budgets.application.dto.CreateBudgetOSRequest;
import com.indux.modules.request_budgets.application.mapper.BudgetOSMapper;
import com.indux.modules.request_budgets.domain.model.BudgetOS;
import com.indux.modules.request_budgets.domain.repository.BudgetRepository;
import com.indux.modules.request_budgets.domain.repository.BudgetOSRepository;
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
public class BudgetOSService {

    private final BudgetOSRepository budgetOSRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetOSMapper budgetOSMapper;
    private final StorageService storageService;

    public BudgetOSResponseDTO createBudgetOS(String budgetId, CreateBudgetOSRequest request, String userId) {
        if (!budgetRepository.existsById(budgetId)) {
            throw new ModuleNotFoundFailure("Orçamento não encontrado");
        }

        BudgetOS os = BudgetOS.builder()
                .budgetId(budgetId)
                .numeroOs(request.getNumeroOs())
                .status(request.getStatus())
                .cnpjDueDiligence(request.getCnpjDueDiligence())
                .inscEstadual(request.getInscEstadual())
                .cpfduediligence(request.getCpfduediligence())
                .createdBy(UUID.fromString(userId))
                .updatedBy(UUID.fromString(userId))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .anexoOs(new ArrayList<>())
                .build();

        if (request.getAnexoOs() != null && !request.getAnexoOs().isEmpty()) {
            os.setAnexoOs(request.getAnexoOs());
        }

        BudgetOS savedOS = budgetOSRepository.save(os);
        return budgetOSMapper.toResponseDTO(savedOS);
    }

    public BudgetOSResponseDTO getBudgetOSById(String id) {
        BudgetOS os = budgetOSRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("OS não encontrada"));
        return budgetOSMapper.toResponseDTO(os);
    }

    public List<BudgetOSResponseDTO> getBudgetOSByBudgetId(String budgetId) {
        return budgetOSRepository.findByBudgetId(budgetId).stream()
                .map(budgetOSMapper::toResponseDTO)
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

