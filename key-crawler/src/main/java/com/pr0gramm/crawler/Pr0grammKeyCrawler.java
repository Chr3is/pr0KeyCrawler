package com.pr0gramm.crawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.telegram.telegrambots.ApiContextInitializer;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableR2dbcRepositories
public class Pr0grammKeyCrawler {

    public static void main(String[] args) {
        SpringApplication.run(Pr0grammKeyCrawler.class, args);
    }

    @PostConstruct
    public void init() {
        ApiContextInitializer.init();
    }

}
