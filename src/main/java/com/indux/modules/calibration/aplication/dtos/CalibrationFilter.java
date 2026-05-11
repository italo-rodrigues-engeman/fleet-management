package com.indux.modules.calibration.aplication.dtos;

import lombok.Builder;

import java.util.List;

@Builder
public record CalibrationFilter(
        List<String> manufecturerId,
        List<String> equipamentId,
        List<String> niMega,
        List<String> unitId,
        List<String> propertyId,
        List<Integer> time,
        List<Long> branchId,
        List<Long> contractId,
        List<Long> projectId,
        List<String> calibrationStatus,
        Boolean situation,
        Boolean late
) {
}