package com.baseplus.modules.branding.service;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.baseplus.core.exception.BusinessException;
import com.baseplus.core.storage.FileStorageService;
import com.baseplus.core.storage.StoredFile;
import com.baseplus.modules.branding.domain.BrandingSettings;
import com.baseplus.modules.branding.domain.LoginBackgroundMode;
import com.baseplus.modules.branding.dto.BrandingSettingsResponse;
import com.baseplus.modules.branding.dto.PublicBrandingSettingsResponse;
import com.baseplus.modules.branding.dto.UpdateBrandingSettingsRequest;
import com.baseplus.modules.branding.repository.BrandingSettingsRepository;

@Service
public class BrandingSettingsService {

    private static final String DEFAULT_NOME = "Base+";
    private static final String DEFAULT_SUBTITULO = "Plataforma Base+";
    private static final String DEFAULT_TEMA = "light";
    private static final String DEFAULT_COR_PRIMARIA = "#2563eb";
    private static final String DEFAULT_COR_SECUNDARIA = "#1e40af";
    private static final String DEFAULT_DENSIDADE = "regular";
    private static final LoginBackgroundMode DEFAULT_LOGIN_BACKGROUND_MODE = LoginBackgroundMode.DEFAULT;
    private static final long MAX_LOGO_SIZE_BYTES = 5L * 1024 * 1024;
    private static final long MAX_FAVICON_SIZE_BYTES = 1L * 1024 * 1024;
    private static final long MAX_COMPACT_LOGO_SIZE_BYTES = 2L * 1024 * 1024;
    private static final long MAX_LOGIN_LOGO_SIZE_BYTES = 5L * 1024 * 1024;
    private static final long MAX_LOGIN_BACKGROUND_SIZE_BYTES = 5L * 1024 * 1024;
    private static final String LOGO_UPLOAD_DIR = "branding/logo";
    private static final String COMPACT_LOGO_UPLOAD_DIR = "branding/compact-logo";
    private static final String FAVICON_UPLOAD_DIR = "branding/favicon";
    private static final String LOGIN_LOGO_UPLOAD_DIR = "branding/login-logo";
    private static final String LOGIN_BACKGROUND_UPLOAD_DIR = "branding/login-background";

    private final BrandingSettingsRepository brandingSettingsRepository;
    private final FileStorageService fileStorageService;

    public BrandingSettingsService(BrandingSettingsRepository brandingSettingsRepository, FileStorageService fileStorageService) {
        this.brandingSettingsRepository = brandingSettingsRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public BrandingSettingsResponse obter() {
        return toResponse(getOrCreateDefault());
    }

    @Transactional
    public PublicBrandingSettingsResponse obterPublico() {
        return toPublicResponse(getOrCreateDefault());
    }

    @Transactional
    public BrandingSettingsResponse atualizar(UpdateBrandingSettingsRequest request) {
        if (request == null) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Ao menos um campo deve ser informado."));
        }

        BrandingSettings settings = getOrCreateDefault();
        settings.setNomePlataforma(resolveOptionalText(request.getNomePlataforma(), settings.getNomePlataforma()));
        settings.setSubtituloInstitucional(resolveOptionalText(request.getSubtituloInstitucional(), settings.getSubtituloInstitucional()));
        settings.setTema(resolveOptionalTema(request.getTema(), settings.getTema()));
        settings.setCorPrimaria(resolveOptionalColor(request.getCorPrimaria(), settings.getCorPrimaria()));
        settings.setCorSecundaria(resolveOptionalColor(request.getCorSecundaria(), settings.getCorSecundaria()));
        settings.setDensidadeVisual(resolveOptionalDensidade(request.getDensidadeVisual(), settings.getDensidadeVisual()));
        settings.setLoginBackgroundMode(resolveOptionalLoginBackgroundMode(request.getLoginBackgroundMode(), settings.getLoginBackgroundMode()));
        settings.setLogoUrl(resolveOptionalAssetUrl(request.getLogoUrl(), settings.getLogoUrl(), "/branding/logo"));
        settings.setCompactLogoUrl(resolveOptionalAssetUrl(request.getCompactLogoUrl(), settings.getCompactLogoUrl(), "/branding/compact-logo"));
        settings.setFaviconUrl(resolveOptionalAssetUrl(request.getFaviconUrl(), settings.getFaviconUrl(), "/branding/favicon"));
        settings.setLoginLogoUrl(resolveOptionalAssetUrl(request.getLoginLogoUrl(), settings.getLoginLogoUrl(), "/branding/login-logo"));
        settings.setLoginBackgroundUrl(resolveOptionalAssetUrl(request.getLoginBackgroundUrl(), settings.getLoginBackgroundUrl(), "/branding/login-background"));
        settings.setWhiteLabelEnabled(resolveOptionalBoolean(request.getWhiteLabelEnabled(), settings.isWhiteLabelEnabled()));
        settings.setWhiteLabelName(resolveOptionalNullableText(request.getWhiteLabelName(), settings.getWhiteLabelName()));
        settings.setWhiteLabelSubtitle(resolveOptionalNullableText(request.getWhiteLabelSubtitle(), settings.getWhiteLabelSubtitle()));

        return toResponse(brandingSettingsRepository.save(settings));
    }

    @Transactional
    public BrandingSettingsResponse atualizarLogo(MultipartFile file) {
        BrandingSettings settings = getOrCreateDefault();
        String previousLogoUrl = settings.getLogoUrl();

        StoredFile storedFile = fileStorageService.saveImage(file, LOGO_UPLOAD_DIR, MAX_LOGO_SIZE_BYTES);
        settings.setLogoUrl(storedFile.url());

        try {
            BrandingSettings saved = brandingSettingsRepository.save(settings);
            if (previousLogoUrl != null && !previousLogoUrl.equals(storedFile.url())) {
                fileStorageService.deleteByUrl(previousLogoUrl);
            }
            return toResponse(saved);
        } catch (RuntimeException exception) {
            fileStorageService.deleteByUrl(storedFile.url());
            throw exception;
        }
    }

    @Transactional
    public BrandingSettingsResponse atualizarCompactLogo(MultipartFile file) {
        BrandingSettings settings = getOrCreateDefault();
        String previousCompactLogoUrl = settings.getCompactLogoUrl();

        StoredFile storedFile = fileStorageService.saveImage(file, COMPACT_LOGO_UPLOAD_DIR, MAX_COMPACT_LOGO_SIZE_BYTES);
        settings.setCompactLogoUrl(storedFile.url());

        try {
            BrandingSettings saved = brandingSettingsRepository.save(settings);
            if (previousCompactLogoUrl != null && !previousCompactLogoUrl.equals(storedFile.url())) {
                fileStorageService.deleteByUrl(previousCompactLogoUrl);
            }
            return toResponse(saved);
        } catch (RuntimeException exception) {
            fileStorageService.deleteByUrl(storedFile.url());
            throw exception;
        }
    }

    @Transactional
    public BrandingSettingsResponse atualizarFavicon(MultipartFile file) {
        BrandingSettings settings = getOrCreateDefault();
        String previousFaviconUrl = settings.getFaviconUrl();

        StoredFile storedFile = fileStorageService.saveImage(file, FAVICON_UPLOAD_DIR, MAX_FAVICON_SIZE_BYTES);
        settings.setFaviconUrl(storedFile.url());

        try {
            BrandingSettings saved = brandingSettingsRepository.save(settings);
            if (previousFaviconUrl != null && !previousFaviconUrl.equals(storedFile.url())) {
                fileStorageService.deleteByUrl(previousFaviconUrl);
            }
            return toResponse(saved);
        } catch (RuntimeException exception) {
            fileStorageService.deleteByUrl(storedFile.url());
            throw exception;
        }
    }

    @Transactional
    public BrandingSettingsResponse atualizarLoginLogo(MultipartFile file) {
        validateLoginLogoFile(file);

        BrandingSettings settings = getOrCreateDefault();
        String previousLoginLogoUrl = settings.getLoginLogoUrl();

        StoredFile storedFile = fileStorageService.saveImage(file, LOGIN_LOGO_UPLOAD_DIR, MAX_LOGIN_LOGO_SIZE_BYTES);
        settings.setLoginLogoUrl(storedFile.url());

        try {
            BrandingSettings saved = brandingSettingsRepository.save(settings);
            if (previousLoginLogoUrl != null && !previousLoginLogoUrl.equals(storedFile.url())) {
                fileStorageService.deleteByUrl(previousLoginLogoUrl);
            }
            return toResponse(saved);
        } catch (RuntimeException exception) {
            fileStorageService.deleteByUrl(storedFile.url());
            throw exception;
        }
    }

    @Transactional
    public BrandingSettingsResponse atualizarLoginBackground(MultipartFile file) {
        validateLoginBackgroundFile(file);

        BrandingSettings settings = getOrCreateDefault();
        String previousLoginBackgroundUrl = settings.getLoginBackgroundUrl();

        StoredFile storedFile = fileStorageService.saveImage(file, LOGIN_BACKGROUND_UPLOAD_DIR, MAX_LOGIN_BACKGROUND_SIZE_BYTES);
        settings.setLoginBackgroundUrl(storedFile.url());

        try {
            BrandingSettings saved = brandingSettingsRepository.save(settings);
            if (previousLoginBackgroundUrl != null && !previousLoginBackgroundUrl.equals(storedFile.url())) {
                fileStorageService.deleteByUrl(previousLoginBackgroundUrl);
            }
            return toResponse(saved);
        } catch (RuntimeException exception) {
            fileStorageService.deleteByUrl(storedFile.url());
            throw exception;
        }
    }

    private BrandingSettings getOrCreateDefault() {
        List<BrandingSettings> registros = brandingSettingsRepository.findAllByOrderByIdAsc();
        if (registros.isEmpty()) {
            return brandingSettingsRepository.saveAndFlush(criarPadrao());
        }

        BrandingSettings principal = registros.get(0);
        if (registros.size() > 1) {
            brandingSettingsRepository.deleteAllExcept(principal.getId());
            brandingSettingsRepository.flush();
        }

        if (principal.getLoginBackgroundMode() == null) {
            principal.setLoginBackgroundMode(DEFAULT_LOGIN_BACKGROUND_MODE);
            principal = brandingSettingsRepository.save(principal);
        }

        boolean migrated = normalizeStoredAssetUrls(principal);
        if (migrated) {
            principal = brandingSettingsRepository.save(principal);
        }

        return principal;
    }

    private BrandingSettings criarPadrao() {
        return new BrandingSettings(
                DEFAULT_NOME,
                DEFAULT_SUBTITULO,
                DEFAULT_TEMA,
                DEFAULT_COR_PRIMARIA,
                DEFAULT_COR_SECUNDARIA,
                DEFAULT_DENSIDADE,
                DEFAULT_LOGIN_BACKGROUND_MODE,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null
        );
    }

    private BrandingSettingsResponse toResponse(BrandingSettings settings) {
        return new BrandingSettingsResponse(
                settings.getNomePlataforma(),
                settings.getSubtituloInstitucional(),
                settings.getTema(),
                settings.getCorPrimaria(),
                settings.getCorSecundaria(),
                settings.getDensidadeVisual(),
                settings.getLoginBackgroundMode(),
                settings.getLogoUrl(),
                settings.getCompactLogoUrl(),
                settings.getFaviconUrl(),
                settings.getLoginLogoUrl(),
                settings.getLoginBackgroundUrl(),
                settings.isWhiteLabelEnabled(),
                settings.getWhiteLabelName(),
                settings.getWhiteLabelSubtitle()
        );
    }

    private PublicBrandingSettingsResponse toPublicResponse(BrandingSettings settings) {
        return new PublicBrandingSettingsResponse(
                settings.getNomePlataforma(),
                settings.getSubtituloInstitucional(),
                settings.getTema(),
                settings.getCorPrimaria(),
                settings.getCorSecundaria(),
                settings.getDensidadeVisual(),
                settings.getLoginBackgroundMode(),
                settings.getLogoUrl(),
                settings.getFaviconUrl(),
                settings.getLoginLogoUrl(),
                settings.getLoginBackgroundUrl(),
                settings.isWhiteLabelEnabled(),
                settings.getWhiteLabelName(),
                settings.getWhiteLabelSubtitle()
        );
    }

    private String resolveOptionalText(String value, String currentValue) {
        if (value == null || value.trim().isEmpty()) {
            return currentValue;
        }

        return value.trim();
    }

    private String resolveOptionalNullableText(String value, String currentValue) {
        if (value == null) {
            return currentValue;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private boolean resolveOptionalBoolean(Boolean value, boolean currentValue) {
        return value == null ? currentValue : value;
    }

    private String resolveOptionalTema(String value, String currentValue) {
        if (value == null || value.trim().isEmpty()) {
            return currentValue;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!"light".equals(normalized) && !"dark".equals(normalized)) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Tema invalido."));
        }

        return normalized;
    }

    private String resolveOptionalDensidade(String value, String currentValue) {
        if (value == null || value.trim().isEmpty()) {
            return currentValue;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        if (!"compact".equals(normalized) && !"regular".equals(normalized)) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Densidade visual invalida."));
        }

        return normalized;
    }

    private String resolveOptionalColor(String value, String currentValue) {
        if (value == null || value.trim().isEmpty()) {
            return currentValue;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("#([0-9A-F]{3}|[0-9A-F]{6})")) {
            throw new BusinessException("Dados invalidos.", HttpStatus.BAD_REQUEST, List.of("Cor invalida."));
        }

        return normalized;
    }

    private void validateLoginBackgroundFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo invalido.", HttpStatus.BAD_REQUEST, List.of("O arquivo e obrigatorio."));
        }

        if (file.getSize() > MAX_LOGIN_BACKGROUND_SIZE_BYTES) {
            throw new BusinessException("Arquivo invalido.", HttpStatus.BAD_REQUEST, List.of("O arquivo excede o tamanho maximo permitido."));
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().trim().toLowerCase(Locale.ROOT);
        if (!"image/png".equals(contentType) && !"image/jpeg".equals(contentType) && !"image/jpg".equals(contentType)) {
            throw new BusinessException("Arquivo invalido.", HttpStatus.BAD_REQUEST, List.of("O arquivo deve ser PNG, JPG ou JPEG."));
        }
    }

    private void validateLoginLogoFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo invalido.", HttpStatus.BAD_REQUEST, List.of("O arquivo e obrigatorio."));
        }

        if (file.getSize() > MAX_LOGIN_LOGO_SIZE_BYTES) {
            throw new BusinessException("Arquivo invalido.", HttpStatus.BAD_REQUEST, List.of("O arquivo excede o tamanho maximo permitido."));
        }

        String contentType = file.getContentType() == null ? "" : file.getContentType().trim().toLowerCase(Locale.ROOT);
        if (!"image/png".equals(contentType)
                && !"image/jpeg".equals(contentType)
                && !"image/jpg".equals(contentType)) {
            throw new BusinessException("Arquivo invalido.", HttpStatus.BAD_REQUEST, List.of("O arquivo deve ser PNG, JPG ou JPEG."));
        }
    }

    private LoginBackgroundMode resolveOptionalLoginBackgroundMode(LoginBackgroundMode value, LoginBackgroundMode currentValue) {
        if (value == null) {
            return currentValue == null ? DEFAULT_LOGIN_BACKGROUND_MODE : currentValue;
        }

        return value;
    }

    private String resolveOptionalAssetUrl(String value, String currentValue, String legacyPathPrefix) {
        if (value == null || value.trim().isEmpty()) {
            return currentValue;
        }

        return normalizeAssetUrl(value, legacyPathPrefix);
    }

    private boolean normalizeStoredAssetUrls(BrandingSettings settings) {
        boolean changed = false;

        String logoUrl = normalizeAssetUrl(settings.getLogoUrl(), "/branding/logo");
        if (!equalsNullable(logoUrl, settings.getLogoUrl())) {
            settings.setLogoUrl(logoUrl);
            changed = true;
        }

        String compactLogoUrl = normalizeAssetUrl(settings.getCompactLogoUrl(), "/branding/compact-logo");
        if (!equalsNullable(compactLogoUrl, settings.getCompactLogoUrl())) {
            settings.setCompactLogoUrl(compactLogoUrl);
            changed = true;
        }

        String faviconUrl = normalizeAssetUrl(settings.getFaviconUrl(), "/branding/favicon");
        if (!equalsNullable(faviconUrl, settings.getFaviconUrl())) {
            settings.setFaviconUrl(faviconUrl);
            changed = true;
        }

        String loginLogoUrl = normalizeAssetUrl(settings.getLoginLogoUrl(), "/branding/login-logo");
        if (!equalsNullable(loginLogoUrl, settings.getLoginLogoUrl())) {
            settings.setLoginLogoUrl(loginLogoUrl);
            changed = true;
        }

        String loginBackgroundUrl = normalizeAssetUrl(settings.getLoginBackgroundUrl(), "/branding/login-background");
        if (!equalsNullable(loginBackgroundUrl, settings.getLoginBackgroundUrl())) {
            settings.setLoginBackgroundUrl(loginBackgroundUrl);
            changed = true;
        }

        return changed;
    }

    private String normalizeAssetUrl(String value, String legacyPathPrefix) {
        String normalized = resolveOptionalText(value, null);
        if (normalized == null) {
            return null;
        }

        if (normalized.startsWith("/uploads/")) {
            return normalized;
        }

        if (normalized.equals(legacyPathPrefix) || normalized.equals(legacyPathPrefix + "/")) {
            return null;
        }

        if (normalized.startsWith(legacyPathPrefix + "/")) {
            return "/uploads" + normalized;
        }

        if (normalized.startsWith(legacyPathPrefix.substring(1) + "/")) {
            return "/uploads/" + normalized;
        }

        return normalized;
    }

    private boolean equalsNullable(String left, String right) {
        if (left == null) {
            return right == null;
        }

        return left.equals(right);
    }
}
