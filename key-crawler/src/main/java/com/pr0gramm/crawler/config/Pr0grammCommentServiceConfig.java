package com.pr0gramm.crawler.config;

import com.pr0gramm.crawler.client.ExtendedPr0grammClient;
import com.pr0gramm.crawler.config.properties.NotificationProperties;
import com.pr0gramm.crawler.service.Pr0grammCommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix = "pr0gramm.notifications", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(NotificationProperties.class)
@Configuration
@Slf4j
public class Pr0grammCommentServiceConfig {

    @Bean
    public Pr0grammCommentService pr0grammCommentService(NotificationProperties properties, ExtendedPr0grammClient client) {
        log.info("Pr0gramm notifications are enabled");
        return new Pr0grammCommentService(properties, client);
    }

}
