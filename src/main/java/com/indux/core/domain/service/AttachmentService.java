package com.indux.core.domain.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.FileMetadata;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {
    List<AttachmentEntity> createAttachmentsFromMultipartFiles(List<MultipartFile> multipartFiles, String basePath);
    FileMetadata storeFile(MultipartFile file, String path, String fileName, Integer etapa);
}
