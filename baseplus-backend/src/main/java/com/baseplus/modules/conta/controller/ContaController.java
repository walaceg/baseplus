package com.baseplus.modules.conta.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baseplus.modules.conta.dto.AvatarResponse;
import com.baseplus.modules.conta.dto.ChangePasswordRequest;
import com.baseplus.modules.conta.dto.ContaResponse;
import com.baseplus.modules.conta.dto.UpdateContaRequest;
import com.baseplus.modules.conta.dto.UpdateUserPreferencesRequest;
import com.baseplus.modules.conta.dto.UserPreferencesResponse;
import com.baseplus.modules.conta.dto.UserSessionResponse;
import com.baseplus.modules.conta.service.ContaService;
import com.baseplus.shared.dto.ApiResponse;

@RestController
@RequestMapping("/conta")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<ContaResponse>> obterConta() {
        ContaResponse response = contaService.obterConta();
        return ResponseEntity.ok(ApiResponse.success(response, "Conta autenticada."));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<ContaResponse>> atualizarConta(@RequestBody UpdateContaRequest request) {
        ContaResponse response = contaService.atualizarConta(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Conta atualizada com sucesso."));
    }

    @PostMapping("/senha")
    public ResponseEntity<ApiResponse<Void>> alterarSenha(@RequestBody ChangePasswordRequest request) {
        contaService.alterarSenha(request);
        return ResponseEntity.ok(ApiResponse.success(null, "Senha alterada com sucesso."));
    }

    @GetMapping("/preferencias")
    public ResponseEntity<ApiResponse<UserPreferencesResponse>> obterPreferencias() {
        UserPreferencesResponse response = contaService.obterPreferencias();
        return ResponseEntity.ok(ApiResponse.success(response, "Preferencias carregadas."));
    }

    @PutMapping("/preferencias")
    public ResponseEntity<ApiResponse<UserPreferencesResponse>> atualizarPreferencias(
            @RequestBody UpdateUserPreferencesRequest request
    ) {
        UserPreferencesResponse response = contaService.atualizarPreferencias(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Preferencias atualizadas com sucesso."));
    }

    @PostMapping("/foto")
    public ResponseEntity<ApiResponse<AvatarResponse>> salvarAvatar(@RequestParam("file") MultipartFile file) {
        AvatarResponse response = contaService.salvarAvatar(file);
        return ResponseEntity.ok(ApiResponse.success(response, "Avatar atualizado com sucesso."));
    }

    @DeleteMapping("/foto")
    public ResponseEntity<ApiResponse<AvatarResponse>> removerAvatar() {
        AvatarResponse response = contaService.removerAvatar();
        return ResponseEntity.ok(ApiResponse.success(response, "Avatar removido com sucesso."));
    }

    @GetMapping("/sessoes")
    public ResponseEntity<ApiResponse<List<UserSessionResponse>>> listarSessoes() {
        List<UserSessionResponse> response = contaService.listarSessoes();
        return ResponseEntity.ok(ApiResponse.success(response, "Sessoes carregadas."));
    }

    @DeleteMapping("/sessoes/{id}")
    public ResponseEntity<ApiResponse<Void>> removerSessao(@PathVariable Long id) {
        contaService.removerSessao(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Sessao removida com sucesso."));
    }
}
