package com.baseplus.core.config;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import com.baseplus.core.storage.UploadProperties;

@Configuration
@EnableConfigurationProperties(UploadProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private final Path uploadsPath;

    public WebConfig(UploadProperties uploadProperties) {
        this.uploadsPath = uploadProperties.resolvedDirectory();
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsPath.toUri().toString())
                .resourceChain(false)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        if (resourcePath.toLowerCase(Locale.ROOT).endsWith(".svg")) {
                            return null;
                        }
                        return super.getResource(resourcePath, location);
                    }
                });
    }
}
