package com.healthcare.hms.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.SpringApplication;
import org.springframework.mock.env.MockEnvironment;

class DotenvEnvironmentPostProcessorTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("loads KEY=value from explicit dotenv location without overriding OS env")
    void loadsDotenvFile() throws Exception {
        final Path envFile = tempDir.resolve("custom.env");
        Files.writeString(
                envFile,
                """
                # comment
                HMS_TEST_DOTENV_KEY=from-file
                PATH=should-not-override-os
                """
        );

        final String previous = System.getProperty("hms.dotenv.location");
        System.setProperty("hms.dotenv.location", envFile.toAbsolutePath().toString());
        try {
            final MockEnvironment environment = new MockEnvironment();
            new DotenvEnvironmentPostProcessor()
                    .postProcessEnvironment(environment, new SpringApplication());

            assertThat(environment.getProperty("HMS_TEST_DOTENV_KEY")).isEqualTo("from-file");
            assertThat(environment.getPropertySources().contains("hmsDotenv")).isTrue();
            assertThat(environment.getPropertySources().get("hmsDotenv").containsProperty("PATH"))
                    .isFalse();
        } finally {
            if (previous == null) {
                System.clearProperty("hms.dotenv.location");
            } else {
                System.setProperty("hms.dotenv.location", previous);
            }
        }
    }

    @Test
    @DisplayName("loads backend/.env from a parent of user.dir (Maven module cwd)")
    void loadsBackendEnvWalkingParents() throws Exception {
        final Path repoRoot = tempDir.resolve("repo");
        final Path backendDir = repoRoot.resolve("backend");
        Files.createDirectories(backendDir);
        Files.writeString(
                backendDir.resolve(".env"),
                """
                DATABASE_USERNAME=root
                DATABASE_PASSWORD=
                """
        );

        final String previousDir = System.getProperty("user.dir");
        System.setProperty("user.dir", backendDir.toAbsolutePath().toString());
        try {
            final MockEnvironment environment = new MockEnvironment();
            new DotenvEnvironmentPostProcessor()
                    .postProcessEnvironment(environment, new SpringApplication());

            assertThat(environment.getProperty("DATABASE_USERNAME")).isEqualTo("root");
            assertThat(environment.getProperty("DATABASE_PASSWORD")).isEmpty();
        } finally {
            System.setProperty("user.dir", previousDir);
        }
    }
}
