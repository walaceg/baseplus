package com.baseplus.modules.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
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
import com.baseplus.modules.audit.service.AuditLogService;
import com.baseplus.modules.conta.domain.UserSession;
import com.baseplus.modules.conta.repository.UserSessionRepository;
import com.baseplus.modules.usuario.domain.Usuario;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository repository;
    @Mock
    private UserSessionRepository sessionRepository;
    @Mock
    private AuditLogService auditLogService;

    private RefreshTokenService service;

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(repository, sessionRepository, auditLogService);
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
    void shouldConsumeTokenAndCreateSuccessorWithOriginalFamilyExpiry() {
        RotationFixture fixture = activeTokenFixture("valid-refresh-token");
        when(fixture.usuario().isAtivo()).thenReturn(true);
        when(fixture.usuario().isBloqueado()).thenReturn(false);
        when(repository.findSessionIdByTokenHash(fixture.hash())).thenReturn(Optional.of(77L));
        when(sessionRepository.findByIdForUpdate(77L)).thenReturn(Optional.of(fixture.session()));
        when(repository.findByTokenHashForUpdate(fixture.hash())).thenReturn(Optional.of(fixture.token()));
        when(repository.saveAndFlush(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshTokenService.RotationAttempt attempt = service.rotate("valid-refresh-token");
        RefreshTokenService.Rotation rotation = attempt.rotation();

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository, org.mockito.Mockito.times(2)).saveAndFlush(captor.capture());
        List<RefreshToken> saved = captor.getAllValues();

        assertThat(fixture.token().isConsumed()).isTrue();
        assertThat(attempt.reuseDetected()).isFalse();
        assertThat(rotation.usuario()).isSameAs(fixture.usuario());
        assertThat(rotation.refreshToken()).isNotEqualTo("valid-refresh-token");
        assertThat(rotation.refreshExpiresIn()).isBetween(0L, 604800L);
        assertThat(saved.get(1).getSession()).isSameAs(fixture.session());
        assertThat(saved.get(1).getTokenHash()).isEqualTo(RefreshTokenHasher.sha256Hex(rotation.refreshToken()));
        assertThat(saved.get(1).getExpiresAt()).isEqualTo(fixture.expiration());
    }

    @Test
    void shouldRejectAndAuditConsumedTokenWithoutRevokingFamily() {
        RotationFixture fixture = activeTokenFixture("reused-refresh-token");
        fixture.token().consume(OffsetDateTime.now().minusSeconds(1));
        when(repository.findSessionIdByTokenHash(fixture.hash())).thenReturn(Optional.of(77L));
        when(sessionRepository.findByIdForUpdate(77L)).thenReturn(Optional.of(fixture.session()));
        when(repository.findByTokenHashForUpdate(fixture.hash())).thenReturn(Optional.of(fixture.token()));

        RefreshTokenService.RotationAttempt attempt = service.rotate("reused-refresh-token");

        assertThat(attempt.reuseDetected()).isTrue();
        assertThat(attempt.rotation()).isNull();
        verify(auditLogService).registrar("SECURITY", "REFRESH_TOKEN_REUSE", "USER_SESSION", 77L);
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).saveAndFlush(any(RefreshToken.class));
    }

    @Test
    void shouldRejectExpiredTokenWithoutDeletingItsReuseRecord() {
        RotationFixture fixture = activeTokenFixture("expired-refresh-token");
        RefreshToken expired = new RefreshToken(
                fixture.usuario(), fixture.session(), fixture.hash(), OffsetDateTime.now().minusSeconds(1)
        );
        when(repository.findSessionIdByTokenHash(fixture.hash())).thenReturn(Optional.of(77L));
        when(sessionRepository.findByIdForUpdate(77L)).thenReturn(Optional.of(fixture.session()));
        when(repository.findByTokenHashForUpdate(fixture.hash())).thenReturn(Optional.of(expired));

        assertThrows(BusinessException.class, () -> service.rotate("expired-refresh-token"));
        org.mockito.Mockito.verify(repository, org.mockito.Mockito.never()).delete(expired);
    }

    @Test
    void shouldRejectUnknownToken() {
        when(repository.findSessionIdByTokenHash(RefreshTokenHasher.sha256Hex("unknown")))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.rotate("unknown"));
    }

    private RotationFixture activeTokenFixture(String rawToken) {
        Usuario usuario = org.mockito.Mockito.mock(Usuario.class);
        UserSession session = org.mockito.Mockito.mock(UserSession.class);
        when(session.getId()).thenReturn(77L);
        OffsetDateTime expiration = OffsetDateTime.now().plusDays(2);
        String hash = RefreshTokenHasher.sha256Hex(rawToken);
        return new RotationFixture(usuario, session, new RefreshToken(usuario, session, hash, expiration), hash, expiration);
    }

    private record RotationFixture(
            Usuario usuario,
            UserSession session,
            RefreshToken token,
            String hash,
            OffsetDateTime expiration
    ) {
    }
}
