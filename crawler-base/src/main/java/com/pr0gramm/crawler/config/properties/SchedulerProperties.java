package com.pr0gramm.crawler.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Validated
@ConfigurationProperties(prefix = "scheduler")
@Data
public class SchedulerProperties {

    private boolean enabled = false;

    @NotEmpty
    private String crawlRefreshInterval = "PT15S";

    @NotEmpty
    private String checkRegistrationCron = "0 */5 * * * *";
}
