package com.baseplus.core.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.baseplus.core.exception.BusinessException;
import com.baseplus.modules.auth.repository.PermissionRepository;
import com.baseplus.modules.auth.repository.RoleRepository;
import com.baseplus.modules.usuario.repository.UsuarioRepository;

@SpringBootTest(properties = "baseplus.bootstrap-admin.enabled=false")
@ActiveProfiles("bootstrap-test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AdminBootstrapServiceTest {

    @Autowired
    private AdminBootstrapService adminBootstrapService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Test
    void shouldCreateFirstAdminWhenNoAdminExists() {
        assertThat(usuarioRepository.countByRoles_NameIgnoreCase(AdminAccessDefaults.ADMIN_ROLE)).isZero();

        adminBootstrapService.bootstrap("Admin Homologacao", "admin.homolog@baseplus.com", "Baseplus@789");

        assertThat(usuarioRepository.countByRoles_NameIgnoreCase(AdminAccessDefaults.ADMIN_ROLE)).isEqualTo(1);
        assertThat(roleRepository.findByNameIgnoreCase(AdminAccessDefaults.ADMIN_ROLE))
                .isPresent()
                .get()
                .satisfies(role -> {
                    assertThat(role.isSistema()).isTrue();
                    assertThat(role.isAtivo()).isTrue();
                    assertThat(role.getPermissions()).hasSize(AdminAccessDefaults.PERMISSIONS.length);
                });
        assertThat(permissionRepository.count()).isGreaterThanOrEqualTo(AdminAccessDefaults.PERMISSIONS.length);
        assertThat(usuarioRepository.findByEmailIgnoreCase("admin.homolog@baseplus.com"))
                .isPresent()
                .get()
                .satisfies(usuario -> {
                    assertThat(usuario.getSenha()).isNotEqualTo("Baseplus@789");
                    assertThat(usuario.isTrocarSenhaPrimeiroAcesso()).isFalse();
                    assertThat(usuario.isBloqueado()).isFalse();
                });
    }

    @Test
    void shouldRefuseBootstrapWhenAdminAlreadyExists() {
        adminBootstrapService.bootstrap("Admin Homologacao", "admin.homolog@baseplus.com", "Baseplus@789");

        assertThatThrownBy(() -> adminBootstrapService.bootstrap("Outro Admin", "outro.admin@baseplus.com", "Baseplus@789"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Bootstrap administrativo recusado.");

        assertThat(usuarioRepository.countByRoles_NameIgnoreCase(AdminAccessDefaults.ADMIN_ROLE)).isEqualTo(1);
    }

    @Test
    void shouldRejectWeakPassword() {
        assertThatThrownBy(() -> adminBootstrapService.bootstrap("Admin Homologacao", "admin.homolog@baseplus.com", "curta"))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Senha invalida.");

        assertThat(usuarioRepository.countByRoles_NameIgnoreCase(AdminAccessDefaults.ADMIN_ROLE)).isZero();
    }
}
