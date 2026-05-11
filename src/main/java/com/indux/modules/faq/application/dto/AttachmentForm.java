package com.indux.modules.faq.application.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class AttachmentForm {
    private String nome;
    private MultipartFile file;

    public AttachmentForm() {}

    public AttachmentForm(String nome, MultipartFile file) {
        this.nome = nome;
        this.file = file;
    }
}

