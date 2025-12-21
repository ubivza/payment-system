package com.example.transactionservice.config;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

public class PGSQLContainer {

    public static class FixedPortPostgreSQLContainer extends PostgreSQLContainer<FixedPortPostgreSQLContainer> {
        public FixedPortPostgreSQLContainer(String dockerImageName) {
            super(dockerImageName);
        }

        public FixedPortPostgreSQLContainer withFixedExposedPort(int hostPort, int containerPort) {
            super.addFixedExposedPort(hostPort, containerPort);
            return this;
        }
    }
    @Container
    public static FixedPortPostgreSQLContainer postgres0 = new FixedPortPostgreSQLContainer("postgres:latest")
            .withCreateContainerCmdModifier(cmd -> cmd.withName("test-postgresql-container-ps0"))
            .withFixedExposedPort(65431, 5432)
            .withUsername("admin")
            .withPassword("admin")
            .withDatabaseName("test");

    @Container
    public static FixedPortPostgreSQLContainer postgres1 = new FixedPortPostgreSQLContainer("postgres:latest")
            .withCreateContainerCmdModifier(cmd -> cmd.withName("test-postgresql-container-ps1"))
            .withFixedExposedPort(65432, 5432)
            .withUsername("admin")
            .withPassword("admin")
            .withDatabaseName("test");
}
