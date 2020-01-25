package com.pr0gramm.keycrawler.config;

import com.pr0gramm.keycrawler.config.properties.SchedulerProperties;
import com.pr0gramm.keycrawler.service.Crawler;
import com.pr0gramm.keycrawler.service.Pr0grammMessageService;
import com.pr0gramm.keycrawler.service.Scheduler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true")
@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Bean
    public SchedulerProperties schedulerProperties() {
        return new SchedulerProperties();
    }

    @Bean
    public Scheduler scheduler(Crawler crawler,
                               Pr0grammMessageService pr0grammMessageService) {
        return new Scheduler(crawler, pr0grammMessageService);
    }

}
