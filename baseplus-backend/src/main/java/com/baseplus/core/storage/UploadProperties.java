package com.baseplus.core.storage;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "baseplus.upload")
public class UploadProperties {

    private String directory = "uploads";

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public Path resolvedDirectory() {
        if (directory == null || directory.isBlank()) {
            throw new IllegalStateException("O diretorio de uploads deve ser informado.");
        }
        return Paths.get(directory).toAbsolutePath().normalize();
    }
}
