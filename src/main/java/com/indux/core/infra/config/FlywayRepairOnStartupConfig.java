package com.indux.core.infra.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FlywayRepairOnStartupConfig {

	@Bean
	public FlywayMigrationStrategy flywayMigrationStrategy() {
		return new FlywayMigrationStrategy() {
			@Override
			public void migrate(Flyway flyway) {
				try {
					flyway.repair();
				} catch (Exception e) {
				}
				flyway.migrate();
			}
		};
	}
}


