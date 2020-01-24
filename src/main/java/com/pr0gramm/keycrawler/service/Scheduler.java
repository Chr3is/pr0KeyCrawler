package com.pr0gramm.keycrawler.service;

import com.pr0gramm.keycrawler.service.telegram.TelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.util.Optional;

import static com.pr0gramm.keycrawler.util.OptionalUtils.execute;

@RequiredArgsConstructor
@Slf4j
public class Scheduler {

    private final KeyCrawler keyCrawler;

    private final Optional<RegistrationService> registrationService;

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
        execute(registrationService.map(RegistrationService::handleNewRegistrations)).subscribe();
    }

    @Scheduled(fixedRateString = "#{schedulerProperties.crawlRefreshInterval}")
    public void checkForNewKeys() {
        log.debug("Starting to crawl new content");
        keyCrawler
                .checkForNewKeys()
                .filter(keyResults -> {
                    if (!keyResults.isEmpty()) {
                        log.info("Found new keys: {}", keyResults);
                        return true;
                    }
                    return false;
                })
                .flatMap(keyResults -> Mono.zip(
                        execute(telegramBot.map(bot -> bot.sendMessage(keyResults))),
                        execute(commentService.map(service -> service.sendNewComment(keyResults)))))
                .subscribe();
    }
}
