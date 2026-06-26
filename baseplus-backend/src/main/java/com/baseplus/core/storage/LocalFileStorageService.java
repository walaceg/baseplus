package com.baseplus.core.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.baseplus.core.exception.BusinessException;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path rootDirectory;

    public LocalFileStorageService(UploadProperties uploadProperties) {
        this.rootDirectory = uploadProperties.resolvedDirectory();
    }

    @Override
    public StoredFile saveImage(MultipartFile file, String subdirectory, long maxSizeBytes) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo invalido.", HttpStatus.BAD_REQUEST, java.util.List.of("O arquivo e obrigatorio."));
        }

        if (file.getSize() > maxSizeBytes) {
            throw new BusinessException("Arquivo invalido.", HttpStatus.BAD_REQUEST, java.util.List.of("O arquivo excede o tamanho maximo permitido."));
        }

        String contentType = normalizeContentType(file.getContentType());
        String extension = resolveValidatedExtension(file, contentType);
        Path directory = rootDirectory.resolve(normalizeSubdirectory(subdirectory)).normalize();
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

        Path file = rootDirectory.resolve(url.substring("/uploads/".length())).normalize();
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

    private String resolveValidatedExtension(MultipartFile file, String contentType) {
        byte[] header;
        try (InputStream inputStream = file.getInputStream()) {
            header = inputStream.readNBytes(12);
        } catch (IOException exception) {
            throw invalidImageFile();
        }

        if ("image/png".equals(contentType) && startsWith(header, new int[] {0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A})) {
            return ".png";
        }

        if (("image/jpeg".equals(contentType) || "image/jpg".equals(contentType))
                && startsWith(header, new int[] {0xFF, 0xD8, 0xFF})) {
            return ".jpg";
        }

        if (("image/x-icon".equals(contentType) || "image/vnd.microsoft.icon".equals(contentType))
                && startsWith(header, new int[] {0x00, 0x00, 0x01, 0x00})) {
            return ".ico";
        }

        throw invalidImageFile();
    }

    private boolean startsWith(byte[] bytes, int[] signature) {
        if (bytes.length < signature.length) {
            return false;
        }

        for (int index = 0; index < signature.length; index++) {
            if ((bytes[index] & 0xFF) != signature[index]) {
                return false;
            }
        }
        return true;
    }

    private BusinessException invalidImageFile() {
        return new BusinessException(
                "Arquivo invalido.",
                HttpStatus.BAD_REQUEST,
                java.util.List.of("O arquivo deve ser uma imagem PNG, JPG, JPEG ou ICO valida.")
        );
    }

    private void ensureWithinRoot(Path path) {
        if (!path.normalize().startsWith(rootDirectory)) {
            throw new BusinessException("Arquivo invalido.", HttpStatus.BAD_REQUEST, java.util.List.of("Caminho de armazenamento invalido."));
        }
    }
}
