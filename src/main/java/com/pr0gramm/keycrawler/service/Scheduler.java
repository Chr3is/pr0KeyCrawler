package com.pr0gramm.keycrawler.service;

import com.pr0gramm.keycrawler.model.KeyResult;
import com.pr0gramm.keycrawler.service.telegram.TelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class Scheduler {

    private final KeyCrawler keyCrawler;

    private final UserService userService;

    private final Optional<TelegramBot> telegramBot;

    private final Optional<Pr0grammCommentService> commentService;

    @PostConstruct
    private void initialFetch() {
        log.debug("Executing initialisation");
        keyCrawler.init().block();
    }

    @Scheduled(cron = "#{schedulerProperties.checkRegistrationCron}")
    public void checkForNewRegistrations() {
        log.debug("Checking registrations");
        userService
                .handleNewRegistrations()
                .subscribe();
    }

    @Scheduled(fixedRateString = "#{schedulerProperties.crawlRefreshInterval}")
    public void checkForNewKeys() {
        log.debug("Starting to crawl new content");
        keyCrawler
                .checkForNewKeys()
                .filter(keyResults -> !keyResults.isEmpty())
                .flatMap(keyResults -> Mono.zip(sendTelegramMessage(keyResults), commentCrawledPost(keyResults)))
                .subscribe();
    }

    private Mono<Void> sendTelegramMessage(List<KeyResult> results) {
        if (telegramBot.isPresent()) {
            return telegramBot.get().sendMessage(results);
        }
        return Mono.empty();
    }

    private Mono<Void> commentCrawledPost(List<KeyResult> results) {
        if (commentService.isPresent()) {
            return commentService.get().sendNewComment(results);
        }
        return Mono.empty();
    }

}
