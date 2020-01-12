package com.pr0gramm.keycrawler.config;

import com.pr0gramm.keycrawler.config.properties.TelegramProperties;
import com.pr0gramm.keycrawler.service.UserService;
import com.pr0gramm.keycrawler.service.telegram.TelegramBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix = "telegram", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(TelegramProperties.class)
@Configuration
@Slf4j
public class TelegramConfig {

    @Bean
    public TelegramBot telegramClient(UserService userService, TelegramProperties properties) {
        log.info("Telegram is enabled");
        return new TelegramBot(userService, properties);
    }

}
