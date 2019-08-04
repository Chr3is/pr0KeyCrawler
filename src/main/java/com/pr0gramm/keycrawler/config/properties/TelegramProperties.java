package com.pr0gramm.keycrawler.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "telegram")
@Data
public class TelegramProperties {

    private boolean enabled = false;

    @NotEmpty
    private String userName;

    @NotEmpty
    private String token;

    @NotNull
    private Integer creatorId;

}
