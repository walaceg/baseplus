package com.baseplus.core.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    StoredFile saveImage(MultipartFile file, String subdirectory, long maxSizeBytes);

    void deleteByUrl(String url);
}
