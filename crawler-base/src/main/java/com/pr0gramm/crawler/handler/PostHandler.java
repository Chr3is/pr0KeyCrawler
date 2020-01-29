package com.pr0gramm.crawler.handler;

import com.pr0gramm.crawler.api.listeners.PostListener;
import com.pr0gramm.crawler.model.client.Pr0Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class PostHandler implements Handler<List<Pr0Post>> {

    private final List<Handler<Mono<List<Pr0Post>>>> handlers;

    private final List<PostListener> postListeners;

    @Override
    public Mono<Void> process(List<Pr0Post> input) {
        return Mono.just(input)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnNext(pr0Posts -> log.info("Received new posts={}", input))
                .doOnNext(posts -> postListeners.forEach(listener -> listener.processPost(posts)))
                .flatMapIterable(posts -> posts)
                .groupBy(Pr0Post::getType)
                .flatMap(postsByType -> Flux.concat(handlers.stream()
                        .filter(handler -> handler.supports(postsByType.key()))
                        .map(monoHandler -> monoHandler.process(postsByType.collectList()))
                        .collect(Collectors.toList())))
                .collectList()
                .then();
    }
}
