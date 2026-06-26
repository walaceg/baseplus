package com.baseplus.modules.branding.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baseplus.modules.branding.dto.BrandingSettingsResponse;
import com.baseplus.modules.branding.dto.PublicBrandingSettingsResponse;
import com.baseplus.modules.branding.dto.UpdateBrandingSettingsRequest;
import com.baseplus.modules.branding.service.BrandingSettingsService;
import com.baseplus.shared.dto.ApiResponse;

@RestController
@RequestMapping("/branding")
public class BrandingSettingsController {

    private final BrandingSettingsService brandingSettingsService;

    public BrandingSettingsController(BrandingSettingsService brandingSettingsService) {
        this.brandingSettingsService = brandingSettingsService;
    }

    @GetMapping
    @PreAuthorize("@authorizationService.hasPermission('BRANDING_VIEW')")
    public ResponseEntity<ApiResponse<BrandingSettingsResponse>> obter() {
        BrandingSettingsResponse response = brandingSettingsService.obter();
        return ResponseEntity.ok(ApiResponse.success(response, "Branding carregado."));
    }

    @GetMapping("/public")
    public ResponseEntity<ApiResponse<PublicBrandingSettingsResponse>> obterPublico() {
        PublicBrandingSettingsResponse response = brandingSettingsService.obterPublico();
        return ResponseEntity.ok(ApiResponse.success(response, "Branding publico carregado."));
    }

    @PutMapping
    @PreAuthorize("@authorizationService.hasPermission('BRANDING_EDIT')")
    public ResponseEntity<ApiResponse<BrandingSettingsResponse>> atualizar(@RequestBody UpdateBrandingSettingsRequest request) {
        BrandingSettingsResponse response = brandingSettingsService.atualizar(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Branding atualizado com sucesso."));
    }

    @PostMapping(value = "/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@authorizationService.hasPermission('BRANDING_UPLOAD_ASSETS')")
    public ResponseEntity<ApiResponse<BrandingSettingsResponse>> enviarLogo(@RequestParam("file") MultipartFile file) {
        BrandingSettingsResponse response = brandingSettingsService.atualizarLogo(file);
        return ResponseEntity.ok(ApiResponse.success(response, "Logo institucional atualizada com sucesso."));
    }

    @PostMapping(value = "/compact-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@authorizationService.hasPermission('BRANDING_UPLOAD_ASSETS')")
    public ResponseEntity<ApiResponse<BrandingSettingsResponse>> enviarCompactLogo(@RequestParam("file") MultipartFile file) {
        BrandingSettingsResponse response = brandingSettingsService.atualizarCompactLogo(file);
        return ResponseEntity.ok(ApiResponse.success(response, "Logo reduzida atualizada com sucesso."));
    }

    @PostMapping(value = "/favicon", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@authorizationService.hasPermission('BRANDING_UPLOAD_ASSETS')")
    public ResponseEntity<ApiResponse<BrandingSettingsResponse>> enviarFavicon(@RequestParam("file") MultipartFile file) {
        BrandingSettingsResponse response = brandingSettingsService.atualizarFavicon(file);
        return ResponseEntity.ok(ApiResponse.success(response, "Favicon atualizado com sucesso."));
    }

    @PostMapping(value = "/login-logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@authorizationService.hasPermission('BRANDING_UPLOAD_ASSETS')")
    public ResponseEntity<ApiResponse<BrandingSettingsResponse>> enviarLoginLogo(@RequestParam("file") MultipartFile file) {
        BrandingSettingsResponse response = brandingSettingsService.atualizarLoginLogo(file);
        return ResponseEntity.ok(ApiResponse.success(response, "Logo do login atualizada com sucesso."));
    }

    @PostMapping(value = "/login-background", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@authorizationService.hasPermission('BRANDING_UPLOAD_ASSETS')")
    public ResponseEntity<ApiResponse<BrandingSettingsResponse>> enviarLoginBackground(@RequestParam("file") MultipartFile file) {
        BrandingSettingsResponse response = brandingSettingsService.atualizarLoginBackground(file);
        return ResponseEntity.ok(ApiResponse.success(response, "Background institucional atualizado com sucesso."));
    }
}
