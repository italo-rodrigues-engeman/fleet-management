package com.indux.modules.ppu.domain.entities.bm;

import com.indux.modules.ppu.application.dtos.response.bm.BMServiceReportItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor @NoArgsConstructor
@Data
public class BMDetailsGeneral{
    private String platform;
    private List<BMServiceReportItem> details;
}
