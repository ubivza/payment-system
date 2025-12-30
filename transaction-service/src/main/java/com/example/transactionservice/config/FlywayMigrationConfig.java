package com.example.transactionservice.config;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Setter
@Configuration
@ConfigurationProperties(prefix = "flyway")
public class FlywayMigrationConfig {
    private List<MigrationSource> dataSources;

    @Builder
    @Getter
    @Setter
    public static class MigrationSource {
        private String url;
        private String username;
        private String password;
    }

    @Bean
    public FlywayMigrationInitializer flywayMigrationInitializer(Flyway flyway) {
        return new FlywayMigrationInitializer(flyway, x-> {
            for (MigrationSource migrationSource : dataSources) {
                Flyway.configure()
                        .dataSource(migrationSource.getUrl(), migrationSource.getUsername(), migrationSource.getPassword())
                        .locations("classpath:db/migration")
                        .load().migrate();
            }
        });
    }
}
