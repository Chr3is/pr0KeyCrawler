package com.pr0gramm.keycrawler.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.net.URI;

@Validated
@ConfigurationProperties(prefix = "pr0gramm.notifications")
@Data
public class NotificationProperties {

    private boolean enabled = false;

    private URI howToPost = URI.create("https://pr0gramm.com/new/3462430");

}
