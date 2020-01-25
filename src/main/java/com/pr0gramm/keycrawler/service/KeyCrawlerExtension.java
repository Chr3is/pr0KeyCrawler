package com.pr0gramm.keycrawler.service;

import com.pr0gramm.keycrawler.api.Post;
import com.pr0gramm.keycrawler.model.KeyResult;
import com.pr0gramm.keycrawler.service.listeners.images.ImageTextListener;
import com.pr0gramm.keycrawler.service.telegram.TelegramBot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
import java.util.Optional;

import static com.pr0gramm.keycrawler.util.OptionalUtils.execute;

@Slf4j
@RequiredArgsConstructor
@Service
public class KeyCrawlerExtension implements ImageTextListener {

    private final Optional<TelegramBot> telegramBot;

    private final Optional<Pr0grammCommentService> commentService;

    @Override
    public void processImageText(List<Tuple2<Post, String>> imageTextsByPosts) {
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
                        execute(telegramBot.map(bot -> bot.sendMessage(keyResults))),
                        execute(commentService.map(service -> service.sendNewComment(keyResults)))))
                .subscribe();
    }
}
