package com.indux.modules.calibration.aplication.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class ModelCreate {
    @JsonProperty("modelo")
    private String model;
    @JsonProperty("capacidade")
    private String capacity;
    private MultipartFile manual;
    private String link;
    @JsonProperty("foto")
    private List<MultipartFile> picture;
    private String niMega;
}
