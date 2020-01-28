package com.pr0gramm.crawler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;

@RequiredArgsConstructor
@Slf4j
public class Scheduler {

    private final Crawler crawler;

    private final Pr0grammMessageService pr0grammMessageService;

    @PostConstruct
    private void initialFetch() {
        log.debug("Executing initialisation");
        crawler.init().block();
    }

    @Scheduled(cron = "#{schedulerProperties.checkRegistrationCron}")
    public void checkForNewRegistrations() {
        log.debug("Checking registrations");
        pr0grammMessageService.checkForNewPendingMessages().subscribe();
    }

    @Scheduled(fixedRateString = "#{schedulerProperties.crawlRefreshInterval}")
    public void checkForNewPosts() {
        log.info("Starting to crawl new content");
        crawler.checkForNewPosts().subscribe();
    }
}
