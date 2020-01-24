package com.pr0gramm.keycrawler.config;

import com.pr0gramm.keycrawler.config.properties.RegistrationProperties;
import com.pr0gramm.keycrawler.repository.UserRepository;
import com.pr0gramm.keycrawler.service.Pr0grammMessageService;
import com.pr0gramm.keycrawler.service.RegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@ConditionalOnProperty(prefix = "pr0gramm.registration", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(RegistrationProperties.class)
@Configuration
@Slf4j
public class RegistrationServiceConfig {

    @Bean
    public RegistrationService registrationService(RegistrationProperties properties,
                                                   UserRepository userRepository,
                                                   Pr0grammMessageService messageService) {
        log.info("Registration is enabled");
        return new RegistrationService(properties, userRepository, messageService);
    }

}
