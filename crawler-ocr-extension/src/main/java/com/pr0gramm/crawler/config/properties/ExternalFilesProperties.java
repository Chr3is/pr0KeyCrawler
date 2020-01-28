package com.pr0gramm.crawler.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@ConfigurationProperties(prefix = "external-files")
public class ExternalFilesProperties {

    private FileSystemResource destination;

}
