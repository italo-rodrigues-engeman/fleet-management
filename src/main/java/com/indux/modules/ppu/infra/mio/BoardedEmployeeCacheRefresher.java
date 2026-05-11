package com.indux.modules.ppu.infra.mio;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class BoardedEmployeeCacheRefresher {
    private final EmployeeBoardingETL etl;

    public BoardedEmployeeCacheRefresher(EmployeeBoardingETL etl) {
        this.etl = etl;
    }

    @Scheduled(cron = "0 50 0 * * *")
    @CacheEvict(value = "boardedEmployees", allEntries = true)
    public void evictCacheAtMidnight() throws IOException {
        refresh();
    }

    @Scheduled(cron = "0 0 6 * * *")
    @CacheEvict(value = "boardedEmployees", allEntries = true)
    public void evictCacheAtSix() throws IOException {
        refresh();
    }

    @Scheduled(cron = "0 0 9 * * *")
    @CacheEvict(value = "boardedEmployees", allEntries = true)
    public void evictCacheAtNine() throws IOException {
        refresh();
    }

    @Scheduled(cron = "0 0 19 * * *")
    @CacheEvict(value = "boardedEmployees", allEntries = true)
    public void evictCacheAtNineteen() throws IOException {
        refresh();
    }

    private void refresh() throws IOException {
        String date = EmployeeBoardingETL.getDefaultDate();
        etl.fetch(date, date);
    }

}