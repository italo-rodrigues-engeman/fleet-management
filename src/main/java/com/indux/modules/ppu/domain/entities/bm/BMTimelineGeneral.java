package com.indux.modules.ppu.domain.entities.bm;

import com.indux.modules.ppu.application.dtos.response.bm.BMTimeline;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class BMTimelineGeneral{
    private String platform;
    List<BMTimeline> timeline;
}

