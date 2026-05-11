package com.indux.modules.ppu.application.services.fixtures;

import com.indux.modules.ppu.domain.entities.mongo.ClosedCompetenceRDO;

import java.time.Month;
import java.time.Year;
import java.util.List;

public class CompetenceFixture {

    public final static ClosedCompetenceRDO fakeJanuaryCompetenceClosed = ClosedCompetenceRDO.builder()
            .month(Month.JANUARY)
            .year(Year.now().getValue())
            .competence("01/2025")
            .checked(true)
            .id("ID-COMPETENCE")
            .build();

    public final static ClosedCompetenceRDO fakeFebruaryCompetenceClosed = ClosedCompetenceRDO.builder()
            .month(Month.FEBRUARY)
            .year(Year.now().getValue())
            .competence("01/2025")
            .checked(true)
            .id("ID-COMPETENCE")
            .build();

    public final static List<ClosedCompetenceRDO> fakeList = List.of(fakeJanuaryCompetenceClosed, fakeFebruaryCompetenceClosed);
}
