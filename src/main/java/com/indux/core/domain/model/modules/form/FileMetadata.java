package com.indux.core.domain.model.modules.form;

import lombok.*;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadata {
    private String path;
    private String extension;
    private String mimeType;
    private Integer etapa;
}
