package ru.finess.finess;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

  private static final int CONTAINER_PORT = 5432;

  @Bean
  @ServiceConnection
  PostgreSQLContainer<?> postgresContainer() {
    return new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine3.21"))
        .withReuse(true)
        .withUsername("finess")
        .withPassword("finess")
        .withDatabaseName("finess")
        .withExposedPorts(CONTAINER_PORT)
        .withCreateContainerCmdModifier(
            cmd ->
                cmd.withHostConfig(
                    HostConfig.newHostConfig()
                        .withPortBindings(
                            new PortBinding(
                                Ports.Binding.bindPort(CONTAINER_PORT),
                                new ExposedPort(CONTAINER_PORT)))));
  }
}
