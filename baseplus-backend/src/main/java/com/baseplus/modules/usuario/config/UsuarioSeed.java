package com.baseplus.modules.usuario.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.baseplus.core.bootstrap.AdminAccessDefaults;
import com.baseplus.modules.auth.domain.Permission;
import com.baseplus.modules.auth.domain.Role;
import com.baseplus.modules.auth.repository.PermissionRepository;
import com.baseplus.modules.auth.repository.RoleRepository;
import com.baseplus.modules.usuario.domain.Usuario;
import com.baseplus.modules.usuario.service.UsuarioService;

@Component
@Profile("dev")
public class UsuarioSeed implements ApplicationRunner {

    public static final String ADMIN_EMAIL = "admin@baseplus.com";
    public static final String ADMIN_PASSWORD = "Baseplus@123";

    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public UsuarioSeed(
            UsuarioService usuarioService,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository,
            PermissionRepository permissionRepository
    ) {
        this.usuarioService = usuarioService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Role adminRole = roleRepository.findByName(AdminAccessDefaults.ADMIN_ROLE)
                .orElseGet(() -> roleRepository.save(new Role(AdminAccessDefaults.ADMIN_ROLE, AdminAccessDefaults.ADMIN_ROLE_DESCRIPTION)));
        adminRole.setAtivo(true);
        adminRole.setSistema(true);
        adminRole.setType(com.baseplus.modules.auth.domain.RoleType.SYSTEM);
        for (String[] permissionData : AdminAccessDefaults.PERMISSIONS) {
            Permission permission = permissionRepository.findByName(permissionData[0])
                    .orElseGet(() -> permissionRepository.save(new Permission(permissionData[0], permissionData[1])));
            adminRole.addPermission(permission);
        }
        adminRole = roleRepository.save(adminRole);

        Usuario admin = usuarioService.buscarPorEmail(ADMIN_EMAIL)
                .orElseGet(() -> new Usuario(
                        "Administrador Base+",
                        ADMIN_EMAIL,
                        passwordEncoder.encode(ADMIN_PASSWORD),
                        true
                ));
        admin.setTrocarSenhaPrimeiroAcesso(false);
        admin.addRole(adminRole);
        usuarioService.salvar(admin);
    }
}
