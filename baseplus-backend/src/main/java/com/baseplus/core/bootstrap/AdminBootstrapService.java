package com.baseplus.core.bootstrap;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baseplus.core.exception.BusinessException;
import com.baseplus.modules.audit.service.AuditLogService;
import com.baseplus.modules.auth.domain.Permission;
import com.baseplus.modules.auth.domain.Role;
import com.baseplus.modules.auth.domain.RoleType;
import com.baseplus.modules.auth.repository.PermissionRepository;
import com.baseplus.modules.auth.repository.RoleRepository;
import com.baseplus.modules.usuario.domain.Usuario;
import com.baseplus.modules.usuario.dto.CreateUsuarioRequest;
import com.baseplus.modules.usuario.dto.UsuarioResponse;
import com.baseplus.modules.usuario.repository.UsuarioRepository;
import com.baseplus.modules.usuario.service.UsuarioAdminService;

@Service
public class AdminBootstrapService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UsuarioRepository usuarioRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UsuarioAdminService usuarioAdminService;
    private final AuditLogService auditLogService;

    public AdminBootstrapService(
            UsuarioRepository usuarioRepository,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            UsuarioAdminService usuarioAdminService,
            AuditLogService auditLogService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.usuarioAdminService = usuarioAdminService;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public UsuarioResponse bootstrap(String name, String email, String password) {
        validateInput(name, email, password);
        ensureNoAdminUser();

        Role adminRole = ensureAdminRole();
        UsuarioResponse response = usuarioAdminService.criar(new CreateUsuarioRequest(
                name.trim(),
                null,
                email.trim().toLowerCase(),
                password,
                null,
                null,
                null,
                null,
                null,
                null,
                true,
                false,
                false
        ));

        Usuario usuario = usuarioRepository.findById(response.id())
                .orElseThrow(() -> new BusinessException("Usuario nao encontrado.", HttpStatus.NOT_FOUND, List.of("Usuario criado nao foi localizado.")));
        usuario.addRole(adminRole);
        Usuario saved = usuarioRepository.save(usuario);
        auditLogService.registrar("SYSTEM", "BOOTSTRAP_ADMIN", "USUARIO", saved.getId());
        return response;
    }

    private void ensureNoAdminUser() {
        if (usuarioRepository.countByRoles_NameIgnoreCase(AdminAccessDefaults.ADMIN_ROLE) > 0) {
            throw new BusinessException(
                    "Bootstrap administrativo recusado.",
                    HttpStatus.CONFLICT,
                    List.of("Ja existe usuario administrador cadastrado.")
            );
        }
    }

    private Role ensureAdminRole() {
        Role adminRole = roleRepository.findByNameIgnoreCase(AdminAccessDefaults.ADMIN_ROLE)
                .orElseGet(() -> new Role(AdminAccessDefaults.ADMIN_ROLE, AdminAccessDefaults.ADMIN_ROLE_DESCRIPTION));
        adminRole.setAtivo(true);
        adminRole.setSistema(true);
        adminRole.setType(RoleType.SYSTEM);

        for (String[] permissionData : AdminAccessDefaults.PERMISSIONS) {
            Permission permission = permissionRepository.findByNameIgnoreCase(permissionData[0])
                    .orElseGet(() -> permissionRepository.save(new Permission(permissionData[0], permissionData[1])));
            adminRole.addPermission(permission);
        }

        return roleRepository.save(adminRole);
    }

    private void validateInput(String name, String email, String password) {
        if (isBlank(name) || isBlank(email) || isBlank(password)) {
            throw new BusinessException(
                    "Dados invalidos.",
                    HttpStatus.BAD_REQUEST,
                    List.of("Nome, email e senha sao obrigatorios para o bootstrap administrativo.")
            );
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new BusinessException(
                    "Senha invalida.",
                    HttpStatus.BAD_REQUEST,
                    List.of("A senha do administrador deve ter no minimo 8 caracteres.")
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
