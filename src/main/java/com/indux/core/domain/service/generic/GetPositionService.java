package com.indux.core.domain.service.generic;
import java.util.List;

public interface GetPositionService<T> {
    List<String> findHcmCodesByCboCode(String cboCode);
    List<String> findHCMCodesByCboCodes(List<String> cboCodes);
    List<T> findByCboCode(String cboCode);
    List<T> findByCboCodes(List<String> cboCodes);
}