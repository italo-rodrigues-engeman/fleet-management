package com.indux.core.domain.model.modules;

import com.indux.core.domain.model.modules.form.FileMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentEntity {
    private String id;
    private String nome;
    private FileMetadata file;
}
