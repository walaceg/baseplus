package com.baseplus.core.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baseplus.core.exception.BusinessException;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final Path ROOT_DIR = Paths.get("uploads").toAbsolutePath().normalize();

    @Override
    public StoredFile saveImage(MultipartFile file, String subdirectory, long maxSizeBytes) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo invalido.", HttpStatus.BAD_REQUEST, java.util.List.of("O arquivo e obrigatorio."));
        }

        if (file.getSize() > maxSizeBytes) {
            throw new BusinessException("Arquivo invalido.", HttpStatus.BAD_REQUEST, java.util.List.of("O arquivo excede o tamanho maximo permitido."));
        }

        String contentType = normalizeContentType(file.getContentType());
        if (!isSupportedImageContentType(contentType)) {
            throw new BusinessException("Arquivo invalido.", HttpStatus.BAD_REQUEST, java.util.List.of("O arquivo deve ser PNG, JPG, JPEG, SVG ou ICO."));
        }

        String extension = resolveExtension(file.getOriginalFilename(), contentType);
        Path directory = ROOT_DIR.resolve(normalizeSubdirectory(subdirectory)).normalize();
        ensureWithinRoot(directory);

        try {
            Files.createDirectories(directory);
            String filename = UUID.randomUUID() + extension;
            Path destination = directory.resolve(filename).normalize();
            ensureWithinRoot(destination);
            file.transferTo(destination);
            return new StoredFile(buildUrl(subdirectory, filename));
        } catch (IOException exception) {
            throw new BusinessException("Nao foi possivel salvar arquivo.", HttpStatus.INTERNAL_SERVER_ERROR, java.util.List.of("Falha ao gravar arquivo local."));
        }
    }

    @Override
    public void deleteByUrl(String url) {
        if (url == null || !url.startsWith("/uploads/")) {
            return;
        }

        Path file = ROOT_DIR.resolve(url.substring("/uploads/".length())).normalize();
        ensureWithinRoot(file);

        try {
            Files.deleteIfExists(file);
        } catch (IOException exception) {
            throw new BusinessException("Nao foi possivel remover arquivo.", HttpStatus.INTERNAL_SERVER_ERROR, java.util.List.of("Falha ao remover arquivo local."));
        }
    }

    private String buildUrl(String subdirectory, String filename) {
        return "/uploads/" + normalizeSubdirectory(subdirectory) + "/" + filename;
    }

    private String normalizeSubdirectory(String subdirectory) {
        return subdirectory == null ? "" : subdirectory.replace("\\", "/").replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isSupportedImageContentType(String contentType) {
        return "image/png".equals(contentType)
                || "image/jpeg".equals(contentType)
                || "image/jpg".equals(contentType)
                || "image/svg+xml".equals(contentType)
                || "image/svg".equals(contentType)
                || "image/x-icon".equals(contentType)
                || "image/vnd.microsoft.icon".equals(contentType);
    }

    private String resolveExtension(String originalFilename, String contentType) {
        String filenameExtension = resolveExtensionFromFilename(originalFilename);
        if (filenameExtension != null) {
            return filenameExtension;
        }

        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/svg+xml", "image/svg" -> ".svg";
            case "image/x-icon", "image/vnd.microsoft.icon" -> ".ico";
            default -> throw new BusinessException("Arquivo invalido.", HttpStatus.BAD_REQUEST, java.util.List.of("O arquivo deve ser PNG, JPG, JPEG, SVG ou ICO."));
        };
    }

    private String resolveExtensionFromFilename(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return null;
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.')).trim().toLowerCase(Locale.ROOT);
        return switch (extension) {
            case ".png", ".jpg", ".jpeg", ".svg" -> extension.equals(".jpeg") ? ".jpg" : extension;
            case ".ico" -> extension;
            default -> null;
        };
    }

    private void ensureWithinRoot(Path path) {
        if (!path.normalize().startsWith(ROOT_DIR)) {
            throw new BusinessException("Arquivo invalido.", HttpStatus.BAD_REQUEST, java.util.List.of("Caminho de armazenamento invalido."));
        }
    }
}
