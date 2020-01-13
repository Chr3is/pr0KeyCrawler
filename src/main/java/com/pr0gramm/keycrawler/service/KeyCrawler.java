package com.pr0gramm.keycrawler.service;

import com.pr0gramm.keycrawler.api.Post;
import com.pr0gramm.keycrawler.client.Pr0grammClient;
import com.pr0gramm.keycrawler.client.Pr0grammImageClient;
import com.pr0gramm.keycrawler.model.KeyResult;
import com.pr0gramm.keycrawler.service.tesseract.TesseractService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
@Service
@Slf4j
public class KeyCrawler {

    private static final Long DELAY_IN_MILLIS = 500L;

    private final Pr0grammClient apiClient;

    private final Pr0grammImageClient imageClient;

    private final TesseractService tesseractService;

    private final ImagePreprocessingService imagePreprocessingService;

    private final AtomicLong dateTimeOfLastAnalyzedPost = new AtomicLong(0);

    public Mono<Void> init() {
        return apiClient.fetchNewContent()
                .onErrorResume(throwable -> Mono.empty())
                .doOnNext(content -> updateTime(content.getLatestPost()))
                .then();
    }

    public Mono<List<KeyResult>> checkForNewKeys() {
        return apiClient
                .fetchNewContent()
                .map(content1 -> content1.getPostsAfter(dateTimeOfLastAnalyzedPost.get()))
                .doOnNext(posts -> updateTime(posts.isEmpty() ? null : posts.get(0)))
                .flatMapIterable(posts -> posts)
                .filter(Post::isSupported)
                .delayElements(Duration.of(DELAY_IN_MILLIS, ChronoUnit.MILLIS))
                .flatMap(imageClient::getImage)
                .parallel()
                .runOn(Schedulers.parallel())
                .flatMap(imagePreprocessingService::process)
                .flatMap(tesseractService::extractTextFromImage)
                .map(KeyResult::new)
                .filter(KeyResult::isKey)
                .sequential()
                .collectList();
    }

    private void updateTime(Post post) {
        if (post != null && post.getCreated() > dateTimeOfLastAnalyzedPost.get()) {
            this.dateTimeOfLastAnalyzedPost.set(post.getCreated());
        }
        log.debug("Time of last analyzed post: {}", this.dateTimeOfLastAnalyzedPost);
    }

}
