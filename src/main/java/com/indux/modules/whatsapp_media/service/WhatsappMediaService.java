package com.indux.modules.whatsapp_media.service;

import com.indux.core.infra.filestorage.StorageService;
import com.indux.modules.whatsapp_media.domain.entity.WhatsappMediaLog;
import com.indux.modules.whatsapp_media.domain.repository.WhatsappMediaLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@Service
public class WhatsappMediaService {
    private final WhatsappMediaLogRepository whatsappMediaLogRepository;
    private final StorageService storageService;

    public WhatsappMediaService(WhatsappMediaLogRepository whatsappMediaLogRepository, StorageService storageService) {
        this.whatsappMediaLogRepository = whatsappMediaLogRepository;
        this.storageService = storageService;
    }

    public String upload(MultipartFile file) {

        Path store = storageService.store(file, "whatsapp_media/files", file.getOriginalFilename());
        String uri = storageService.getRootLocation().relativize(store).toString();
        whatsappMediaLogRepository.save(new WhatsappMediaLog(null, "system", null, file.getOriginalFilename()));
        return uri.replace("whatsapp_media", "").replace("\\", "/");

    }

}
