package com.healthcare.hms.auth.crypto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("TokenHashingService")
class TokenHashingServiceTest {

    private final TokenHashingService tokenHashingService = new TokenHashingService();

    @Test
    @DisplayName("generateRawToken returns 96-character hex string")
    void generateRawToken_returnsHexOfExpectedLength() {
        final String rawToken = tokenHashingService.generateRawToken();

        assertThat(rawToken).hasSize(96).matches("^[0-9a-f]+$");
    }

    @Test
    @DisplayName("hash is deterministic for the same input")
    void hash_isDeterministic() {
        final String raw = "abc123";

        assertThat(tokenHashingService.hash(raw)).isEqualTo(tokenHashingService.hash(raw));
    }

    @Test
    @DisplayName("hash differs for different inputs")
    void hash_differsForDifferentInputs() {
        assertThat(tokenHashingService.hash("token-a")).isNotEqualTo(tokenHashingService.hash("token-b"));
    }

    @Test
    @DisplayName("hash returns 64-character sha-256 hex")
    void hash_returnsSha256HexLength() {
        assertThat(tokenHashingService.hash("sample")).hasSize(64).matches("^[0-9a-f]+$");
    }
}
