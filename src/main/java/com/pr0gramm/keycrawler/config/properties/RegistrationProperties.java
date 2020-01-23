package com.pr0gramm.keycrawler.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotEmpty;

@ConfigurationProperties(prefix = "pr0gramm.registration")
@Data
public class RegistrationProperties {

    private boolean enabled = false;

    @NotEmpty
    private String keyWord = "pr0keycrawler";
}
