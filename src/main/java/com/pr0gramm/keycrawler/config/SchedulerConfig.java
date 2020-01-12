package com.pr0gramm.keycrawler.config;

import com.pr0gramm.keycrawler.config.properties.SchedulerProperties;
import com.pr0gramm.keycrawler.service.KeyCrawler;
import com.pr0gramm.keycrawler.service.Pr0grammCommentService;
import com.pr0gramm.keycrawler.service.Scheduler;
import com.pr0gramm.keycrawler.service.UserService;
import com.pr0gramm.keycrawler.service.telegram.TelegramBot;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Optional;

@ConditionalOnProperty(value = "scheduler.enabled", havingValue = "true")
@Configuration
@EnableScheduling
public class SchedulerConfig {

    @Bean
    public SchedulerProperties schedulerProperties() {
        return new SchedulerProperties();
    }

    @Bean
    public Scheduler scheduler(KeyCrawler keyCrawler, UserService userService, Optional<TelegramBot> telegramBot,
                               Optional<Pr0grammCommentService> commentService) {
        return new Scheduler(keyCrawler, userService, telegramBot, commentService);
    }

}
