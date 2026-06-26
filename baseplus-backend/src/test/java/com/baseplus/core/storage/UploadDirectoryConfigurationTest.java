package com.baseplus.core.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class UploadDirectoryConfigurationTest {

    @TempDir
    static Path uploadDirectory;

    @DynamicPropertySource
    static void configureUploadDirectory(DynamicPropertyRegistry registry) {
        registry.add("baseplus.upload.directory", uploadDirectory::toString);
    }

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldStoreAndServeFileFromConfiguredDirectory() throws Exception {
        byte[] image = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                MediaType.IMAGE_PNG_VALUE,
                image
        );

        StoredFile storedFile = fileStorageService.saveImage(file, "configuration-test", 1024);
        Path storedPath = uploadDirectory.resolve(storedFile.url().substring("/uploads/".length()));

        assertThat(storedPath).exists();
        assertThat(Files.readAllBytes(storedPath)).isEqualTo(image);
        mockMvc.perform(get(storedFile.url()))
                .andExpect(status().isOk())
                .andExpect(content().bytes(image));
    }
}
