package com.healthcare.hms.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Loads optional {@code .env} files into the Spring {@link ConfigurableEnvironment}
 * so local {@code backend/.env} works without exporting shell variables.
 *
 * <p>Precedence: OS / Docker / IDE environment variables always win — dotenv values
 * are registered with the lowest precedence ({@code addLast}).
 *
 * <p>Search order (later files override earlier keys within dotenv only):
 * <ol>
 *   <li>{@code .env} and {@code backend/.env} under {@code user.dir} and parents</li>
 *   <li>optional {@code hms.dotenv.location} system property / env {@code HMS_DOTENV_LOCATION}</li>
 * </ol>
 */
@Order(Ordered.LOWEST_PRECEDENCE)
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(DotenvEnvironmentPostProcessor.class);
    private static final String PROPERTY_SOURCE_NAME = "hmsDotenv";
    private static final int MAX_PARENT_WALK = 6;

    @Override
    public void postProcessEnvironment(
            final ConfigurableEnvironment environment,
            final SpringApplication application
    ) {
        final Path cwd = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        final Map<String, Object> values = new LinkedHashMap<>();

        for (final Path candidate : discoverEnvFiles(cwd)) {
            mergeFile(values, candidate);
        }

        final String explicit = firstNonBlank(
                System.getProperty("hms.dotenv.location"),
                environment.getProperty("hms.dotenv.location"),
                System.getenv("HMS_DOTENV_LOCATION")
        );
        if (explicit != null) {
            mergeFile(values, Path.of(explicit).toAbsolutePath().normalize());
        }

        if (values.isEmpty()) {
            return;
        }

        environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, values));
        log.info("Loaded {} dotenv entries from local .env file(s) (OS env still wins)", values.size());
    }

    private static Set<Path> discoverEnvFiles(final Path start) {
        final Set<Path> files = new LinkedHashSet<>();
        Path current = start;
        for (int i = 0; i <= MAX_PARENT_WALK && current != null; i++) {
            files.add(current.resolve(".env"));
            files.add(current.resolve("backend").resolve(".env"));
            current = current.getParent();
        }
        return files;
    }

    private static void mergeFile(final Map<String, Object> target, final Path file) {
        if (!Files.isRegularFile(file)) {
            return;
        }
        try {
            final List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            int count = 0;
            for (final String raw : lines) {
                final String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                final int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                final String key = line.substring(0, eq).trim();
                if (key.isEmpty() || System.getenv(key) != null) {
                    // Never override a real process environment variable.
                    continue;
                }
                String value = line.substring(eq + 1).trim();
                if ((value.startsWith("\"") && value.endsWith("\""))
                        || (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                target.put(key, value);
                count++;
            }
            if (count > 0) {
                log.debug("Merged {} keys from {}", count, file);
            }
        } catch (final IOException ex) {
            log.warn("Unable to read dotenv file {}: {}", file, ex.getMessage());
        }
    }

    private static String firstNonBlank(final String... values) {
        if (values == null) {
            return null;
        }
        for (final String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
