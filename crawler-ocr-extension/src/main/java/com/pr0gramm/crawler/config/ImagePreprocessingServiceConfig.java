package com.pr0gramm.crawler.config;

import com.pr0gramm.crawler.config.properties.ExternalFilesProperties;
import com.pr0gramm.crawler.service.ImagePreprocessingService;
import com.pr0gramm.crawler.util.ClasspathFileExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@EnableConfigurationProperties(ExternalFilesProperties.class)
@RequiredArgsConstructor
public class ImagePreprocessingServiceConfig {

    private final ExternalFilesProperties externalFilesProperties;

    @Bean
    public ImagePreprocessingService imagePreprocessingService() {
        Path eastDataLocation = ClasspathFileExtractor.extractFile("east/frozen_east_text_detection.pb",
                externalFilesProperties.getDestination());
        return new ImagePreprocessingService(eastDataLocation);
    }
}
