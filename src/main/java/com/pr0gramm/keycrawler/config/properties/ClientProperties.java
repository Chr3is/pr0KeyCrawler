package com.pr0gramm.keycrawler.config.properties;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Validated
@Data
public abstract class ClientProperties {

    @NotEmpty
    protected String url;

}
