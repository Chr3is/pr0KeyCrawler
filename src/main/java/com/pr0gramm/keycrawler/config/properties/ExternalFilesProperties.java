package com.pr0gramm.keycrawler.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Data
@Validated
@ConfigurationProperties(prefix = "external-files")
public class ExternalFilesProperties {

    @NotNull
    private FileSystemResource eastDataLocation = new FileSystemResource("target/resources-external/east/frozen_east_text_detection.pb");

    @NotNull
    private FileSystemResource tessDataLocation = new FileSystemResource("target/resources-external/tessdata/");

}
