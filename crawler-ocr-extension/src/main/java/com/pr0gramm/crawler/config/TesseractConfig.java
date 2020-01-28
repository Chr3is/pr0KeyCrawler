package com.pr0gramm.crawler.config;

import com.pr0gramm.crawler.config.properties.ExternalFilesProperties;
import com.pr0gramm.crawler.service.tesseract.TesseractPool;
import com.pr0gramm.crawler.service.tesseract.TesseractService;
import com.pr0gramm.crawler.util.ClasspathFileExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@EnableConfigurationProperties(ExternalFilesProperties.class)
@RequiredArgsConstructor
public class TesseractConfig {

    private final ExternalFilesProperties externalFilesProperties;

    @Bean
    public TesseractService tesseractService() {
        return new TesseractService(createTesseractPool());
    }

    private TesseractPool createTesseractPool() {
        Path tessDataLocation = ClasspathFileExtractor.extractFile("tessdata/eng.traineddata",
                externalFilesProperties.getDestination());
        return new TesseractPool(tessDataLocation);
    }

}
