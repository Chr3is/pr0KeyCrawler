package com.pr0gramm.keycrawler.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Validated
@ConfigurationProperties(prefix = "database")
@Data
public class DatabaseProperties {

    private boolean inMemory = false;

    @NotEmpty
    private String host;

    @NotEmpty
    private String name;

    @NotEmpty
    private String user;

    @NotNull
    private String password;

}
