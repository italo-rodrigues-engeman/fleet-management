package com.indux.modules.ppu.domain.entities.ppu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AvailableType {
    private String id = UUID.randomUUID().toString();
    private String name;
    private Double factor;
    private int quantityDays;
}
