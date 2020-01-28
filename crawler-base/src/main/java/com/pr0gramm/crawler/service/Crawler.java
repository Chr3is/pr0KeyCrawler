package com.pr0gramm.crawler.service;

import com.pr0gramm.crawler.client.Pr0grammClient;
import com.pr0gramm.crawler.handler.PostHandler;
import com.pr0gramm.crawler.model.client.Pr0Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
@Service
@Slf4j
public class Crawler {

    private final Pr0grammClient apiClient;

    private final PostHandler postHandler;

    private final AtomicLong dateTimeOfLastAnalyzedPost = new AtomicLong(0);

    public Mono<Void> init() {
        return apiClient.fetchNewContent()
                .onErrorResume(throwable -> Mono.empty())
                .doOnNext(content -> updateTime(content.getLatestPost()))
                .then();
    }

    public Mono<Void> checkForNewPosts() {
        return apiClient
                .fetchNewContent()
                .map(content1 -> content1.getPostsAfter(dateTimeOfLastAnalyzedPost.get()))
                .doOnNext(posts -> updateTime(posts.isEmpty() ? null : posts.get(0)))
                .doOnNext(postHandler::process).then();
    }

    private void updateTime(Pr0Post post) {
        if (post != null && post.getCreated() > dateTimeOfLastAnalyzedPost.get()) {
            this.dateTimeOfLastAnalyzedPost.set(post.getCreated());
        }
        log.debug("Time of last analyzed post: {}", this.dateTimeOfLastAnalyzedPost);
    }

}
