package com.pr0gramm.crawler.config;

import com.pr0gramm.crawler.mapping.Mappings;
import ma.glasnost.orika.MapperFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MappingsConfig {

    @Bean
    public MapperFacade mapperFacade() {
        return Mappings.INSTANCE;
    }
}
