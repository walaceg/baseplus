package com.baseplus.modules.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baseplus.core.exception.BusinessException;
import com.baseplus.modules.auth.domain.RefreshToken;
import com.baseplus.modules.auth.repository.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repository;

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(repository);
    }

    @Test
    void shouldPersistOnlySha256HashAndReturnHighEntropyToken() {
        when(repository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String rawToken = service.criar(null, null);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(captor.capture());
        String persistedHash = captor.getValue().getTokenHash();

        assertThat(Base64.getUrlDecoder().decode(rawToken)).hasSize(48);
        assertThat(persistedHash).matches("[0-9a-f]{64}");
        assertThat(persistedHash).isEqualTo(RefreshTokenHasher.sha256Hex(rawToken));
        assertThat(persistedHash).isNotEqualTo(rawToken);
        assertThat(service.getExpirationSeconds()).isEqualTo(604800L);
    }

    @Test
    void shouldGenerateUniqueRefreshTokensAndHashes() {
        when(repository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String first = service.criar(null, null);
        String second = service.criar(null, null);

        assertThat(first).isNotEqualTo(second);
        assertThat(RefreshTokenHasher.sha256Hex(first)).isNotEqualTo(RefreshTokenHasher.sha256Hex(second));
    }

    @Test
    void shouldCreateDeterministicSha256HexHash() {
        assertThat(RefreshTokenHasher.sha256Hex("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void shouldLookUpRefreshTokenByDeterministicHash() {
        String rawToken = "refresh-token-original";
        String expectedHash = RefreshTokenHasher.sha256Hex(rawToken);
        RefreshToken stored = new RefreshToken(null, null, expectedHash, OffsetDateTime.now().plusDays(1));
        when(repository.findByTokenHash(expectedHash)).thenReturn(Optional.of(stored));

        assertThat(service.validar(rawToken)).isSameAs(stored);
    }

    @Test
    void shouldRejectUnknownRefreshToken() {
        when(repository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.validar("unknown-refresh-token"));
    }

    @Test
    void shouldRejectExpiredRefreshToken() {
        String rawToken = "expired-refresh-token";
        String hash = RefreshTokenHasher.sha256Hex(rawToken);
        RefreshToken expired = new RefreshToken(null, null, hash, OffsetDateTime.now().minusSeconds(1));
        when(repository.findByTokenHash(hash)).thenReturn(Optional.of(expired));

        assertThrows(BusinessException.class, () -> service.validar(rawToken));
        verify(repository).delete(expired);
    }
}
