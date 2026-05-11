package com.indux.modules.ppu.domain.repositories.mongo;

import com.indux.modules.ppu.application.dtos.requests.RDOFilter;
import com.indux.modules.ppu.application.projection.RDOGridProjection;
import com.indux.modules.ppu.domain.entities.mongo.RDOEntity;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusDP;
import com.indux.modules.ppu.domain.entities.rdo.RDOStatusOP;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface RDORepositoryCustom {
        Page<RDOGridProjection> findAllOrdered(Pageable pageable, Set<Integer> regionais, Set<Integer> projetos);

        Page<RDOGridProjection> filter(RDOFilter filter, Pageable pageable, Boolean isFromRH);

        Page<RDOEntity> findEntitiesByFilter(RDOFilter filter, Pageable pageable, Boolean isFromRH);

        Integer countByFiltersOP(RDOStatusOP status, String platform, LocalDate start, LocalDate end, RDOFilter filter);

        Integer countByFiltersRH(RDOStatusDP statusDP, List<RDOStatusOP> excludedStatusOP, String platform,
                        LocalDate start,
                        LocalDate end, RDOFilter filter);

        List<RDOEntity> findAllByPlatformInAndStatusOPAndDateBetween(
                        List<String> platforms,
                        RDOStatusOP status,
                        LocalDate start,
                        LocalDate end);

        List<RDOEntity> findAllByPlatformAndStatusOPAndDateBetween(String platform, RDOStatusOP opStatus,
                        LocalDate start,
                        LocalDate end);

        Page<RDOGridProjection> findAllByStatusOPInOrdered(List<RDOStatusOP> statusOPs, Pageable pageable,
                        Set<Integer> regionais, Set<Integer> projetos);

        List<RDOEntity> findByPlatformAndDateRange(String platform, LocalDate startDate, LocalDate endDate);

        List<RDOEntity> findAllByDateAndPpuID(LocalDate start, LocalDate end, String ppuId);

        List<RDOEntity> findAllByPlatformAndDateAndPpuID(String platform, LocalDate start, LocalDate end, String ppuId);
}
