package com.pr0gramm.crawler.service;

import com.pr0gramm.crawler.api.listeners.images.ImageTextListener;
import com.pr0gramm.crawler.model.KeyResult;
import com.pr0gramm.crawler.model.client.Pr0Post;
import com.pr0gramm.crawler.service.telegram.TelegramBot;
import com.pr0gramm.crawler.util.OptionalUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class KeyCrawlerExtension implements ImageTextListener {

    private final Optional<TelegramBot> telegramBot;

    private final Optional<Pr0grammCommentService> commentService;

    @Override
    public void processImageText(List<Tuple2<Pr0Post, String>> imageTextsByPosts) {
        log.info("New textsByImages={}", imageTextsByPosts);
        Flux.just(imageTextsByPosts)
                .flatMapIterable(texts -> texts)
                .map(KeyResult::new)
                .filter(KeyResult::isKey)
                .collectList()
                .filter(keyResults -> {
                    if (!keyResults.isEmpty()) {
                        log.info("Found new keys: {}", keyResults);
                        return true;
                    }
                    return false;
                })
                .flatMap(keyResults -> Mono.zip(
                        OptionalUtils.execute(telegramBot.map(bot -> bot.sendMessage(keyResults))),
                        OptionalUtils.execute(commentService.map(service -> service.sendNewComment(keyResults)))))
                .subscribe();
    }
}
