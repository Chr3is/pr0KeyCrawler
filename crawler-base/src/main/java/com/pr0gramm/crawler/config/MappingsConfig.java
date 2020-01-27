package com.pr0gramm.crawler.config;

import com.pr0gramm.crawler.config.properties.Pr0grammApiClientProperties;
import com.pr0gramm.crawler.mapping.Mappings;
import ma.glasnost.orika.MapperFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MappingsConfig {

    @Bean
    public MapperFacade mapperFacade(Pr0grammApiClientProperties properties) {
        return new Mappings(properties);
    }
}
