package com.indux.modules.request_budgets.application.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.FileMetadata;
import com.indux.core.infra.exception.module.ModuleNotFoundFailure;
import com.indux.core.infra.filestorage.StorageService;
import com.indux.modules.request_budgets.application.dto.BudgetEsclarecimentoResponseDTO;
import com.indux.modules.request_budgets.application.dto.CreateBudgetEsclarecimentoRequest;
import com.indux.modules.request_budgets.application.mapper.BudgetEsclarecimentoMapper;
import com.indux.modules.request_budgets.domain.model.BudgetEsclarecimento;
import com.indux.modules.request_budgets.domain.repository.BudgetEsclarecimentoRepository;
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
public class BudgetEsclarecimentoService {

    private final BudgetEsclarecimentoRepository budgetEsclarecimentoRepository;
    private final BudgetVersionRepository budgetVersionRepository;
    private final BudgetEsclarecimentoMapper budgetEsclarecimentoMapper;
    private final StorageService storageService;

    public BudgetEsclarecimentoResponseDTO createBudgetEsclarecimento(String versionId, CreateBudgetEsclarecimentoRequest request, String userId) {
        if (!budgetVersionRepository.existsById(versionId)) {
            throw new ModuleNotFoundFailure("Versão do orçamento não encontrada");
        }

        BudgetEsclarecimento esclarecimento = BudgetEsclarecimento.builder()
                .versionId(versionId)
                .perguntas(request.getPerguntas())
                .descricaoEsclarecimento(request.getDescricaoEsclarecimento())
                .dataHoraEsclarecimento(request.getDataHoraEsclarecimento())
                .responsavelEsclarecimento(request.getResponsavelEsclarecimento())
                .createdBy(UUID.fromString(userId))
                .updatedBy(UUID.fromString(userId))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .anexoEsclarecimento(new ArrayList<>())
                .build();

        if (request.getAnexoEsclarecimento() != null && !request.getAnexoEsclarecimento().isEmpty()) {
            esclarecimento.setAnexoEsclarecimento(request.getAnexoEsclarecimento());
        }

        BudgetEsclarecimento savedEsclarecimento = budgetEsclarecimentoRepository.save(esclarecimento);
        return budgetEsclarecimentoMapper.toResponseDTO(savedEsclarecimento);
    }

    public BudgetEsclarecimentoResponseDTO getBudgetEsclarecimentoById(String id) {
        BudgetEsclarecimento esclarecimento = budgetEsclarecimentoRepository.findById(id)
                .orElseThrow(() -> new ModuleNotFoundFailure("Esclarecimento não encontrado"));
        return budgetEsclarecimentoMapper.toResponseDTO(esclarecimento);
    }

    public List<BudgetEsclarecimentoResponseDTO> getBudgetEsclarecimentosByVersionId(String versionId) {
        return budgetEsclarecimentoRepository.findByVersionId(versionId).stream()
                .map(budgetEsclarecimentoMapper::toResponseDTO)
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

