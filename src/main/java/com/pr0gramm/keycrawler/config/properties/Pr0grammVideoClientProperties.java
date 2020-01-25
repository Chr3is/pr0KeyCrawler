package com.pr0gramm.keycrawler.config.properties;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "pr0gramm.video-client")
@EqualsAndHashCode(callSuper = true)
@Data
public class Pr0grammVideoClientProperties extends ClientProperties {
}