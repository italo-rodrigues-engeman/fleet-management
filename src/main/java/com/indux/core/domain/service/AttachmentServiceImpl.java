package com.indux.core.domain.service;

import com.indux.core.domain.model.modules.AttachmentEntity;
import com.indux.core.domain.model.modules.form.FileMetadata;
import com.indux.core.infra.filestorage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {
    
    private final StorageService storageService;
    
    @Override
    public List<AttachmentEntity> createAttachmentsFromMultipartFiles(List<MultipartFile> multipartFiles, String basePath) {
        List<AttachmentEntity> result = new ArrayList<>();
        
        if (multipartFiles == null || multipartFiles.isEmpty()) {
            return result;
        }
        
        String timestamp = LocalDateTime.now()
            .truncatedTo(ChronoUnit.SECONDS)
            .toString()
            .replace(":", "-");
        
        for (MultipartFile file : multipartFiles) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            
            AttachmentEntity attachment = new AttachmentEntity();
            attachment.setId(UUID.randomUUID().toString());
            attachment.setNome(file.getOriginalFilename());
            
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
            }
            
            String filename = String.format("%s - %s%s", 
                attachment.getId(),
                timestamp,
                extension
            );
            
            FileMetadata fileMetadata = storeFile(file, basePath, filename, 1);
            attachment.setFile(fileMetadata);
            
            result.add(attachment);
        }
        
        return result;
    }
    
    @Override
    public FileMetadata storeFile(MultipartFile file, String path, String fileName, Integer etapa) {
        String mimeType = file.getContentType();
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.') + 1);
        }

        var store = storageService.store(file, path, fileName);
        String uri = storageService.getRootLocation().relativize(store).toString();
        uri = uri.replace("\\", "/");

        return new FileMetadata(uri, ext, mimeType, etapa);
    }
}
