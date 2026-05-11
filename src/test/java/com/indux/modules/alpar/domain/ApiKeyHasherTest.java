package com.indux.modules.alpar.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ApiKeyHasherTest {

    private static final String SECRET = "test-secret";
    private static final String RAW_KEY = "my-api-key";

    @Test
    void shouldProduceSameHashForSameInput() {
        String hash1 = ApiKeyHasher.hash(RAW_KEY, SECRET);
        String hash2 = ApiKeyHasher.hash(RAW_KEY, SECRET);

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void shouldProduceDifferentHashWhenKeyDiffers() {
        String hash1 = ApiKeyHasher.hash(RAW_KEY, SECRET);
        String hash2 = ApiKeyHasher.hash("other-key", SECRET);

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void shouldProduceDifferentHashWhenSecretDiffers() {
        String hash1 = ApiKeyHasher.hash(RAW_KEY, SECRET);
        String hash2 = ApiKeyHasher.hash(RAW_KEY, "other-secret");

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    void shouldProduceHexStringOf64Chars() {
        String hash = ApiKeyHasher.hash(RAW_KEY, SECRET);

        assertThat(hash).hasSize(64).matches("[0-9a-f]+");
    }

    @Test
    void shouldThrowWhenSecretIsEmpty() {
        assertThatThrownBy(() -> ApiKeyHasher.hash(RAW_KEY, ""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Failed to hash API key");
    }
}
