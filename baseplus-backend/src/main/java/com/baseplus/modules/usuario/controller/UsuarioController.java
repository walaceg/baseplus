package com.baseplus.modules.usuario.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baseplus.modules.usuario.dto.CreateUsuarioRequest;
import com.baseplus.modules.usuario.dto.ResetSenhaUsuarioRequest;
import com.baseplus.modules.usuario.dto.UpdateUsuarioRequest;
import com.baseplus.modules.usuario.dto.UsuarioResponse;
import com.baseplus.modules.usuario.service.UsuarioAdminService;
import com.baseplus.shared.dto.ApiResponse;
import com.baseplus.shared.dto.PageResponse;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioAdminService usuarioAdminService;

    public UsuarioController(UsuarioAdminService usuarioAdminService) {
        this.usuarioAdminService = usuarioAdminService;
    }

    @GetMapping
    @PreAuthorize("@authorizationService.hasPermission('USERS_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<UsuarioResponse>>> listar(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(required = false) Boolean bloqueado,
            @RequestParam(required = false) Boolean primeiroAcesso,
            @PageableDefault(size = 10, sort = {"criadoEm", "id"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        PageResponse<UsuarioResponse> response = usuarioAdminService.listar(search, ativo, bloqueado, primeiroAcesso, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Usuarios carregados."));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authorizationService.hasPermission('USERS_VIEW')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> buscar(@PathVariable Long id) {
        UsuarioResponse response = usuarioAdminService.buscar(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Usuario carregado."));
    }

    @PostMapping
    @PreAuthorize("@authorizationService.hasPermission('USERS_CREATE')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> criar(@RequestBody CreateUsuarioRequest request) {
        UsuarioResponse response = usuarioAdminService.criar(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Usuario criado com sucesso."));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@authorizationService.hasPermission('USERS_EDIT')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> atualizar(
            @PathVariable Long id,
            @RequestBody UpdateUsuarioRequest request
    ) {
        UsuarioResponse response = usuarioAdminService.atualizar(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Usuario atualizado com sucesso."));
    }

    @PostMapping("/{id}/resetar-senha")
    @PreAuthorize("@authorizationService.hasPermission('USERS_RESET_PASSWORD')")
    public ResponseEntity<ApiResponse<UsuarioResponse>> resetarSenha(
            @PathVariable Long id,
            @RequestBody ResetSenhaUsuarioRequest request
    ) {
        UsuarioResponse response = usuarioAdminService.resetarSenha(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Senha redefinida com sucesso."));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@authorizationService.hasPermission('USERS_DELETE')")
    public ResponseEntity<ApiResponse<Void>> remover(@PathVariable Long id) {
        usuarioAdminService.remover(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Usuario removido com sucesso."));
    }
}
