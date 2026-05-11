package com.indux.modules.calibration.aplication.dtos;

import java.util.List;

public record CalibrationStandardFilter(
        String search,
        List<String> manufecturerId,
        List<String> equipamentId,
        List<String> niMega,
        List<String> unitId,
        List<String> propertyId,
        List<Integer> time,
        Boolean status
) {
}
